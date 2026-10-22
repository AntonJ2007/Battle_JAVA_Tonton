import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public class GameFrame extends JFrame {
	// ----- ADTs
	private final List<GameCharacter> roster = new ArrayList<>();       // List ADT
	private final List<GameCharacter> view = new ArrayList<>();         // filtered list shown in table
	private final Queue<GameCharacter> turnOrder = new ArrayDeque<>();  // Queue ADT
	private final Deque<Snapshot> history = new ArrayDeque<>();         // Stack ADT (push/pop)

	// ----- roster widgets
	private final DefaultTableModel tableModel = new DefaultTableModel(
			new String[] { "Name", "Type", "Health", "Attack", "Special" }, 0) {
		@Override
		public boolean isCellEditable(int r, int c) {
			return false;
		}
	};
	private final JTable table = new JTable(tableModel);
	private final JTextField nameField = new JTextField();
	private final JComboBox<String> typeBox = new JComboBox<>(CharacterFactory.TYPES);
	private final JSpinner hpSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 500, 5));
	private final JLabel abilityLabel = new JLabel(" ");
	private final JTextField searchField = new JTextField();
	private final JLabel statusLabel = new JLabel(" ");

	// ----- arena widgets
	private final JComboBox<GameCharacter> boxA = new JComboBox<>();
	private final JComboBox<GameCharacter> boxB = new JComboBox<>();
	private final FighterPanel panelA = new FighterPanel();
	private final FighterPanel panelB = new FighterPanel();
	private final JLabel turnLabel = new JLabel("Pick two fighters and press START", SwingConstants.CENTER);
	private final JTextArea logArea = new JTextArea();
	private final RoundButton attackBtn = new RoundButton("Attack", new Color(0xE5, 0x39, 0x35));
	private final RoundButton specialBtn = new RoundButton("Special", Theme.ACCENT);
	private final RoundButton undoBtn = new RoundButton("Undo", new Color(0x45, 0x5A, 0x8A));
	private GameCharacter fighterA, fighterB;
	private boolean battleActive = false;

	GameFrame() {
		super("Game Character Manager & Battle Arena");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1040, 700);
		setMinimumSize(new Dimension(900, 620));
		setLocationRelativeTo(null);
		getContentPane().setBackground(Theme.BG);
		setLayout(new BorderLayout());

		add(buildHeader(), BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();
		tabs.setBackground(Theme.PANEL);
		tabs.setForeground(Theme.TEXT);
		tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
		tabs.addTab("  Roster  ", buildRosterTab());
		tabs.addTab("  Arena  ", buildArenaTab());
		tabs.addChangeListener(e -> refreshCombos());
		add(tabs, BorderLayout.CENTER);

		roster.add(new Warrior("Chou", 100));
		roster.add(new Mage("Harith", 100));
		roster.add(new Archer("Miya", 90));
		roster.add(new Healer("Estes", 120));
		refreshTable();
		refreshCombos();
		updateAbilityLabel();
		refreshBattleUI();
	}

	// ------------------------------------------------------------- HEADER
	private JComponent buildHeader() {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g0) {
				Graphics2D g = (Graphics2D) g0.create();
				g.setPaint(new GradientPaint(0, 0, new Color(0x3B, 0x1F, 0x8F), getWidth(), 0, new Color(0xB2, 0x2A, 0x5B)));
				g.fillRect(0, 0, getWidth(), getHeight());
				g.dispose();
			}
		};
		p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
		JLabel title = new JLabel("GAME CHARACTER MANAGER");
		title.setFont(new Font("SansSerif", Font.BOLD, 26));
		title.setForeground(Color.WHITE);
		JLabel sub = new JLabel("Create heroes, manage your roster, then settle it in the Arena");
		sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
		sub.setForeground(new Color(255, 255, 255, 190));
		JPanel box = new JPanel(new GridLayout(2, 1));
		box.setOpaque(false);
		box.add(title);
		box.add(sub);
		p.add(box, BorderLayout.WEST);
		return p;
	}

	// --------------------------------------------------------- ROSTER TAB
	private JComponent buildRosterTab() {
		JPanel root = new JPanel(new BorderLayout(14, 0));
		root.setBackground(Theme.BG);
		root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

		// ---- form (left)
		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(Theme.PANEL);
		form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		form.setPreferredSize(new Dimension(300, 0));

		styleField(nameField);
		styleCombo(typeBox);
		styleSpinner(hpSpinner);
		abilityLabel.setForeground(Theme.MUTED);
		abilityLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
		typeBox.addActionListener(e -> updateAbilityLabel());

		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1;
		gc.insets = new Insets(4, 0, 4, 0);

		JLabel formTitle = new JLabel("Character Details");
		formTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
		formTitle.setForeground(Theme.TEXT);
		gc.gridy = 0;
		form.add(formTitle, gc);
		gc.gridy = 1;
		form.add(label("Name (letters/numbers, max 15)"), gc);
		gc.gridy = 2;
		form.add(nameField, gc);
		gc.gridy = 3;
		form.add(label("Character type"), gc);
		gc.gridy = 4;
		form.add(typeBox, gc);
		gc.gridy = 5;
		form.add(abilityLabel, gc);
		gc.gridy = 6;
		form.add(label("Health (1 - 500)"), gc);
		gc.gridy = 7;
		form.add(hpSpinner, gc);

		RoundButton addBtn = new RoundButton("Add Character", Theme.GOOD.darker());
		RoundButton updateBtn = new RoundButton("Update Selected", Theme.ACCENT);
		RoundButton removeBtn = new RoundButton("Remove Selected", Theme.BAD.darker());
		RoundButton clearBtn = new RoundButton("Clear Form", new Color(0x45, 0x5A, 0x8A));
		RoundButton healBtn = new RoundButton("Heal All", new Color(0x00, 0x8B, 0x8B));
		addBtn.addActionListener(e -> addCharacter());
		updateBtn.addActionListener(e -> updateCharacter());
		removeBtn.addActionListener(e -> removeCharacter());
		clearBtn.addActionListener(e -> clearForm());
		healBtn.addActionListener(e -> {
			for (GameCharacter c : roster) {
				c.restoreFull();
			}
			refreshTable();
			status("All characters restored to full health.", true);
		});

		gc.insets = new Insets(14, 0, 4, 0);
		gc.gridy = 8;
		form.add(addBtn, gc);
		gc.insets = new Insets(4, 0, 4, 0);
		gc.gridy = 9;
		form.add(updateBtn, gc);
		gc.gridy = 10;
		form.add(removeBtn, gc);
		gc.gridy = 11;
		form.add(clearBtn, gc);
		gc.gridy = 12;
		form.add(healBtn, gc);
		gc.gridy = 13;
		gc.weighty = 1;
		form.add(Box.createVerticalGlue(), gc);

		// ---- table + search (center)
		JPanel center = new JPanel(new BorderLayout(0, 10));
		center.setBackground(Theme.BG);

		JPanel searchRow = new JPanel(new BorderLayout(8, 0));
		searchRow.setBackground(Theme.PANEL);
		searchRow.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
		JLabel sl = new JLabel("Search by name:");
		sl.setForeground(Theme.TEXT);
		styleField(searchField);
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { refreshTable(); }
			public void removeUpdate(DocumentEvent e) { refreshTable(); }
			public void changedUpdate(DocumentEvent e) { refreshTable(); }
		});
		searchRow.add(sl, BorderLayout.WEST);
		searchRow.add(searchField, BorderLayout.CENTER);

		styleTable();
		JScrollPane sp = new JScrollPane(table);
		sp.getViewport().setBackground(Theme.FIELD);
		sp.setBorder(BorderFactory.createLineBorder(Theme.PANEL, 2));

		statusLabel.setForeground(Theme.MUTED);
		statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 4));

		center.add(searchRow, BorderLayout.NORTH);
		center.add(sp, BorderLayout.CENTER);
		center.add(statusLabel, BorderLayout.SOUTH);

		root.add(form, BorderLayout.WEST);
		root.add(center, BorderLayout.CENTER);
		return root;
	}

	private void styleTable() {
		table.setBackground(Theme.FIELD);
		table.setForeground(Theme.TEXT);
		table.setGridColor(Theme.PANEL);
		table.setRowHeight(30);
		table.setFont(new Font("SansSerif", Font.PLAIN, 14));
		table.setSelectionBackground(Theme.ACCENT);
		table.setSelectionForeground(Color.WHITE);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowVerticalLines(false);

		DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
					int col) {
				JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
				l.setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
				l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
				if (!sel) {
					l.setBackground(row % 2 == 0 ? Theme.FIELD : new Color(0x25, 0x29, 0x44));
					l.setForeground(Theme.TEXT);
					if (col == 1 && row < view.size()) {
						l.setForeground(view.get(row).getColor().brighter());
					}
					if (col == 2 && row < view.size() && !view.get(row).isAlive()) {
						l.setForeground(Theme.BAD);
					}
				}
				return l;
			}
		};
		table.setDefaultRenderer(Object.class, cell);

		DefaultTableCellRenderer header = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
					int col) {
				JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
				l.setBackground(Theme.PANEL);
				l.setForeground(Theme.ACCENT.brighter());
				l.setFont(new Font("SansSerif", Font.BOLD, 13));
				l.setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
				l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
				return l;
			}
		};
		table.getTableHeader().setDefaultRenderer(header);
		table.getTableHeader().setReorderingAllowed(false);

		table.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}
			GameCharacter c = getSelected();
			if (c != null) {
				nameField.setText(c.getName());
				typeBox.setSelectedItem(c.getType());
				hpSpinner.setValue(c.getMaxHealth());
			}
		});
	}

	// ---------------------------------------------------------- ARENA TAB
	private JComponent buildArenaTab() {
		JPanel root = new JPanel(new BorderLayout(0, 12));
		root.setBackground(Theme.BG);
		root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

		// selection row
		JPanel pick = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
		pick.setBackground(Theme.PANEL);
		styleCombo(boxA);
		styleCombo(boxB);
		boxA.setPreferredSize(new Dimension(190, 30));
		boxB.setPreferredSize(new Dimension(190, 30));
		RoundButton startBtn = new RoundButton("START BATTLE", Theme.GOOD.darker());
		startBtn.addActionListener(e -> startBattle());
		JLabel vs = new JLabel("VS");
		vs.setForeground(Theme.WARN);
		vs.setFont(new Font("SansSerif", Font.BOLD, 18));
		pick.add(boxA);
		pick.add(vs);
		pick.add(boxB);
		pick.add(startBtn);

		// fighters
		JPanel fighters = new JPanel(new GridLayout(1, 2, 12, 0));
		fighters.setBackground(Theme.BG);
		fighters.add(panelA);
		fighters.add(panelB);

		// bottom: controls + log
		turnLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		turnLabel.setForeground(Theme.WARN);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
		buttons.setBackground(Theme.PANEL);
		attackBtn.addActionListener(e -> doAction(false));
		specialBtn.addActionListener(e -> doAction(true));
		undoBtn.addActionListener(e -> undo());
		buttons.add(attackBtn);
		buttons.add(specialBtn);
		buttons.add(undoBtn);

		logArea.setEditable(false);
		logArea.setLineWrap(true);
		logArea.setWrapStyleWord(true);
		logArea.setBackground(Theme.FIELD);
		logArea.setForeground(Theme.TEXT);
		logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
		logArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		JScrollPane logScroll = new JScrollPane(logArea);
		logScroll.setPreferredSize(new Dimension(0, 120));
		logScroll.setBorder(BorderFactory.createLineBorder(Theme.PANEL, 2));

		JPanel bottom = new JPanel(new BorderLayout(0, 6));
		bottom.setBackground(Theme.PANEL);
		bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JPanel ctl = new JPanel(new GridLayout(2, 1));
		ctl.setOpaque(false);
		ctl.add(turnLabel);
		ctl.add(buttons);
		bottom.add(ctl, BorderLayout.NORTH);
		bottom.add(logScroll, BorderLayout.CENTER);

		root.add(pick, BorderLayout.NORTH);
		root.add(fighters, BorderLayout.CENTER);
		root.add(bottom, BorderLayout.SOUTH);
		return root;
	}

	// ------------------------------------------------- ROSTER OPERATIONS
	private GameCharacter getSelected() {
		int row = table.getSelectedRow();
		return (row >= 0 && row < view.size()) ? view.get(row) : null;
	}

	/** Returns an error message, or null if the name is valid. */
	private String validateName(String raw, GameCharacter ignore) {
		String n = raw.trim();
		if (n.isEmpty()) {
			return "Name cannot be empty.";
		}
		if (n.length() > 15) {
			return "Name must be 15 characters or fewer.";
		}
		if (!n.matches("[A-Za-z0-9 ]+")) {
			return "Name may only contain letters, numbers and spaces.";
		}
		for (GameCharacter c : roster) {
			if (c != ignore && c.getName().equalsIgnoreCase(n)) {
				return "A character named \"" + n + "\" already exists.";
			}
		}
		return null;
	}

	/** Returns the health value or -1 if the spinner text is invalid. */
	private int readHealth() {
		try {
			hpSpinner.commitEdit();
		} catch (ParseException ex) {
			return -1;
		}
		return ((Number) hpSpinner.getValue()).intValue();
	}

	private void addCharacter() {
		String err = validateName(nameField.getText(), null);
		if (err != null) {
			error(err);
			return;
		}
		int hp = readHealth();
		if (hp < 1 || hp > 500) {
			error("Health must be a whole number between 1 and 500.");
			return;
		}
		GameCharacter c = CharacterFactory.create((String) typeBox.getSelectedItem(), nameField.getText().trim(), hp);
		roster.add(c);
		refreshTable();
		refreshCombos();
		clearForm();
		status("Added " + c + ".", true);
	}

	private void updateCharacter() {
		GameCharacter sel = getSelected();
		if (sel == null) {
			error("Select a character in the table first.");
			return;
		}
		String err = validateName(nameField.getText(), sel);
		if (err != null) {
			error(err);
			return;
		}
		int hp = readHealth();
		if (hp < 1 || hp > 500) {
			error("Health must be a whole number between 1 and 500.");
			return;
		}
		GameCharacter updated = CharacterFactory.create((String) typeBox.getSelectedItem(), nameField.getText().trim(), hp);
		roster.set(roster.indexOf(sel), updated);
		if (sel == fighterA || sel == fighterB) {
			resetBattle("Battle reset because a fighter was updated.");
		}
		refreshTable();
		refreshCombos();
		status("Updated " + updated + ".", true);
	}

	private void removeCharacter() {
		GameCharacter sel = getSelected();
		if (sel == null) {
			error("Select a character in the table first.");
			return;
		}
		int ok = JOptionPane.showConfirmDialog(this, "Remove " + sel + " from the roster?", "Confirm remove",
				JOptionPane.YES_NO_OPTION);
		if (ok != JOptionPane.YES_OPTION) {
			return;
		}
		roster.remove(sel);
		if (sel == fighterA || sel == fighterB) {
			resetBattle("Battle reset because a fighter was removed.");
		}
		refreshTable();
		refreshCombos();
		clearForm();
		status("Removed " + sel + ".", true);
	}

	private void clearForm() {
		nameField.setText("");
		typeBox.setSelectedIndex(0);
		hpSpinner.setValue(100);
		table.clearSelection();
	}

	private void refreshTable() {
		String q = searchField.getText().trim().toLowerCase();
		view.clear();
		tableModel.setRowCount(0);
		for (GameCharacter c : roster) {
			if (q.isEmpty() || c.getName().toLowerCase().contains(q)) {
				view.add(c);
				tableModel.addRow(new Object[] { c.getName(), c.getType(),
						c.getHealth() + " / " + c.getMaxHealth(), c.getAttackName(), c.getSpecialName() });
			}
		}
		if (!q.isEmpty()) {
			status(view.size() + " result(s) for \"" + searchField.getText().trim() + "\".", true);
		}
	}

	private void updateAbilityLabel() {
		GameCharacter sample = CharacterFactory.create((String) typeBox.getSelectedItem(), "x", 1);
		abilityLabel.setText("<html><body style='width:230px'>" + sample.getDescription() + "</body></html>");
	}

	// -------------------------------------------------- BATTLE OPERATIONS
	private void refreshCombos() {
		GameCharacter selA = (GameCharacter) boxA.getSelectedItem();
		GameCharacter selB = (GameCharacter) boxB.getSelectedItem();
		boxA.removeAllItems();
		boxB.removeAllItems();
		for (GameCharacter c : roster) {
			boxA.addItem(c);
			boxB.addItem(c);
		}
		if (selA != null && roster.contains(selA)) {
			boxA.setSelectedItem(selA);
		}
		if (selB != null && roster.contains(selB)) {
			boxB.setSelectedItem(selB);
		} else if (roster.size() > 1) {
			boxB.setSelectedIndex(1);
		}
	}

	private void startBattle() {
		GameCharacter a = (GameCharacter) boxA.getSelectedItem();
		GameCharacter b = (GameCharacter) boxB.getSelectedItem();
		if (a == null || b == null) {
			error("You need at least two characters in the roster to battle.");
			return;
		}
		if (a == b) {
			error("Choose two different fighters.");
			return;
		}
		a.restoreFull();
		b.restoreFull();
		fighterA = a;
		fighterB = b;
		turnOrder.clear();
		turnOrder.add(a);
		turnOrder.add(b);
		history.clear();
		logArea.setText("");
		battleActive = true;
		append("=== " + a.getName() + " vs " + b.getName() + " ===");
		panelA.show(a, false);
		panelB.show(b, false);
		refreshTable();
		refreshBattleUI();
	}

	private void resetBattle(String reason) {
		battleActive = false;
		fighterA = null;
		fighterB = null;
		turnOrder.clear();
		history.clear();
		panelA.show(null, false);
		panelB.show(null, false);
		logArea.setText(reason + "\n");
		refreshBattleUI();
	}

	private void doAction(boolean special) {
		if (!battleActive || turnOrder.isEmpty()) {
			return;
		}
		GameCharacter cur = turnOrder.peek();
		GameCharacter tgt = (cur == fighterA) ? fighterB : fighterA;
		history.push(new Snapshot(cur, fighterA, fighterB, logArea.getText().length()));

		int tgtBefore = tgt.getHealth();
		String msg;
		if (special) {
			msg = cur.performSpecial(tgt);
		} else {
			msg = cur.attack(tgt);
			cur.tickCooldown();
		}
		append(msg);

		panelA.show(fighterA, true);
		panelB.show(fighterB, true);
		if (tgt.getHealth() < tgtBefore) {
			(tgt == fighterA ? panelA : panelB).hit();
		}

		if (!tgt.isAlive()) {
			append(">>> " + cur.getName() + " wins the battle! <<<");
			battleActive = false;
		} else {
			turnOrder.add(turnOrder.poll()); // rotate turn
		}
		refreshTable();
		refreshBattleUI();
	}

	private void undo() {
		if (history.isEmpty() || fighterA == null) {
			return;
		}
		Snapshot s = history.pop();
		fighterA.restoreState(s.hpA, s.cdA);
		fighterB.restoreState(s.hpB, s.cdB);
		GameCharacter other = (s.current == fighterA) ? fighterB : fighterA;
		turnOrder.clear();
		turnOrder.add(s.current);
		turnOrder.add(other);
		battleActive = true;
		String text = logArea.getText();
		logArea.setText(text.substring(0, Math.min(s.logLength, text.length())));
		panelA.show(fighterA, true);
		panelB.show(fighterB, true);
		refreshTable();
		refreshBattleUI();
	}

	private void refreshBattleUI() {
		GameCharacter cur = turnOrder.peek();
		attackBtn.setEnabled(battleActive);
		specialBtn.setEnabled(battleActive && cur != null && cur.specialReady());
		undoBtn.setEnabled(!history.isEmpty() && fighterA != null);
		if (battleActive && cur != null) {
			attackBtn.setText("Attack: " + cur.getAttackName());
			specialBtn.setText(cur.specialReady() ? "Special: " + cur.getSpecialName()
					: "Special (" + cur.getSpecialCooldown() + " turns)");
			turnLabel.setText("Turn: " + cur.getName());
		} else {
			attackBtn.setText("Attack");
			specialBtn.setText("Special");
			turnLabel.setText(fighterA == null ? "Pick two fighters and press START BATTLE" : "Battle over!");
		}
	}

	// ------------------------------------------------------------ HELPERS
	private void append(String line) {
		logArea.append(line + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	private void error(String msg) {
		status(msg, false);
		JOptionPane.showMessageDialog(this, msg, "Invalid input", JOptionPane.WARNING_MESSAGE);
	}

	private void status(String msg, boolean ok) {
		statusLabel.setForeground(ok ? Theme.GOOD : Theme.BAD);
		statusLabel.setText(msg);
	}

	private JLabel label(String t) {
		JLabel l = new JLabel(t);
		l.setForeground(Theme.MUTED);
		l.setFont(new Font("SansSerif", Font.PLAIN, 12));
		return l;
	}

	private void styleField(JTextField f) {
		f.setBackground(Theme.FIELD);
		f.setForeground(Theme.TEXT);
		f.setCaretColor(Theme.TEXT);
		f.setFont(new Font("SansSerif", Font.PLAIN, 14));
		f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.ACCENT.darker(), 1),
				BorderFactory.createEmptyBorder(6, 8, 6, 8)));
	}

	private void styleCombo(JComboBox<?> c) {
		c.setBackground(Theme.FIELD);
		c.setForeground(Theme.TEXT);
		c.setFont(new Font("SansSerif", Font.PLAIN, 14));
	}

	private void styleSpinner(JSpinner s) {
		JTextField tf = ((JSpinner.DefaultEditor) s.getEditor()).getTextField();
		styleField(tf);
		s.setBorder(BorderFactory.createEmptyBorder());
	}
}
