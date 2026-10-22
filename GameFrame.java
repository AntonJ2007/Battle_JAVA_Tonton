import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public class GameFrame extends JFrame {
	// ----- red & black colors
	static final Color BLACK = new Color(0x0D, 0x0D, 0x0D);
	static final Color DARK = new Color(0x1C, 0x1C, 0x1C);
	static final Color RED = new Color(0xC6, 0x28, 0x28);
	static final Color DARK_RED = new Color(0x8B, 0x10, 0x10);
	static final Color LIGHT = new Color(0xF2, 0xF2, 0xF2);

	static {
		Object[] colors = {
			"Panel.background", BLACK, "Label.foreground", LIGHT,
			"OptionPane.background", BLACK, "OptionPane.messageForeground", LIGHT,
			"TabbedPane.background", DARK, "TabbedPane.foreground", LIGHT, "TabbedPane.selected", RED,
			"TabbedPane.contentAreaColor", BLACK, "TabbedPane.focus", RED,
			"TextField.background", DARK, "TextField.foreground", LIGHT, "TextField.caretForeground", LIGHT,
			"FormattedTextField.background", DARK, "FormattedTextField.foreground", LIGHT,
			"FormattedTextField.caretForeground", LIGHT,
			"TextArea.background", DARK, "TextArea.foreground", LIGHT,
			"ComboBox.background", DARK, "ComboBox.foreground", LIGHT,
			"ComboBox.selectionBackground", RED, "ComboBox.selectionForeground", Color.WHITE,
			"Spinner.background", DARK, "Spinner.foreground", LIGHT,
			"Table.background", DARK, "Table.foreground", LIGHT, "Table.gridColor", BLACK,
			"Table.selectionBackground", RED, "Table.selectionForeground", Color.WHITE,
			"TableHeader.background", DARK_RED, "TableHeader.foreground", Color.WHITE,
			"Viewport.background", DARK, "Button.disabledText", Color.GRAY
		};
		for (int i = 0; i < colors.length; i += 2) {
			UIManager.put(colors[i], colors[i + 1]);
		}
	}

	// ----- ADTs
	private final List<GameCharacter> roster = new ArrayList<>();       // List ADT
	private final List<GameCharacter> view = new ArrayList<>();         // filtered list shown in table
	private final Queue<GameCharacter> turnOrder = new ArrayDeque<>();  // Queue ADT
	private final Deque<Snapshot> history = new ArrayDeque<>();         // Stack ADT (push/pop)

	// ----- roster widgets
	private final DefaultTableModel tableModel = new DefaultTableModel(
			new String[] { "Name", "Type", "Health", "Attack", "Special" }, 0) {
		public boolean isCellEditable(int r, int c) { return false; }
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
	private final JLabel turnLabel = new JLabel("", SwingConstants.CENTER);
	private final JTextArea logArea = new JTextArea(6, 20);
	private final JButton attackBtn = styled(new JButton("Attack"));
	private final JButton specialBtn = styled(new JButton("Special"));
	private final JButton undoBtn = styled(new JButton("Undo"));
	private GameCharacter fighterA, fighterB;
	private boolean battleActive = false;

	GameFrame() {
		super("Battle Arena League");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1000, 680);
		setLocationRelativeTo(null);

		JLabel title = new JLabel("  BATTLE ARENA LEAGUE", SwingConstants.LEFT);
		title.setFont(new Font("SansSerif", Font.BOLD, 24));
		title.setForeground(Color.WHITE);
		title.setOpaque(true);
		title.setBackground(RED);
		title.setPreferredSize(new Dimension(0, 50));
		add(title, BorderLayout.NORTH);
		panelA.setBackground(DARK);
		panelB.setBackground(DARK);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Roster", buildRosterTab());
		tabs.addTab("Arena", buildArenaTab());
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

	// --------------------------------------------------------- ROSTER TAB
	private JComponent buildRosterTab() {
		JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
		form.setPreferredSize(new Dimension(280, 0));
		form.add(new JLabel("Name (letters/numbers, max 15)"));
		form.add(nameField);
		form.add(new JLabel("Character type"));
		form.add(typeBox);
		form.add(abilityLabel);
		form.add(new JLabel("Health (1 - 500)"));
		form.add(hpSpinner);
		form.add(button("Add Character", e -> addCharacter()));
		form.add(button("Update Selected", e -> updateCharacter()));
		form.add(button("Remove Selected", e -> removeCharacter()));
		form.add(button("Clear Form", e -> clearForm()));
		form.add(button("Heal All", e -> {
			for (GameCharacter c : roster) {
				c.restoreFull();
			}
			refreshTable();
			statusLabel.setText("All characters restored to full health.");
		}));
		typeBox.addActionListener(e -> updateAbilityLabel());

		searchField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { refreshTable(); }
			public void removeUpdate(DocumentEvent e) { refreshTable(); }
			public void changedUpdate(DocumentEvent e) { refreshTable(); }
		});
		JPanel searchRow = new JPanel(new BorderLayout(8, 0));
		searchRow.add(new JLabel("Search by name:"), BorderLayout.WEST);
		searchRow.add(searchField, BorderLayout.CENTER);

		table.setRowHeight(26);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(e -> {
			GameCharacter c = getSelected();
			if (!e.getValueIsAdjusting() && c != null) {
				nameField.setText(c.getName());
				typeBox.setSelectedItem(c.getType());
				hpSpinner.setValue(c.getMaxHealth());
			}
		});

		JPanel center = new JPanel(new BorderLayout(0, 8));
		center.add(searchRow, BorderLayout.NORTH);
		center.add(new JScrollPane(table), BorderLayout.CENTER);
		center.add(statusLabel, BorderLayout.SOUTH);

		JPanel root = new JPanel(new BorderLayout(12, 0));
		root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		root.add(form, BorderLayout.WEST);
		root.add(center, BorderLayout.CENTER);
		return root;
	}

	// ---------------------------------------------------------- ARENA TAB
	private JComponent buildArenaTab() {
		JPanel pick = new JPanel();
		boxA.setPreferredSize(new Dimension(190, 28));
		boxB.setPreferredSize(new Dimension(190, 28));
		pick.add(boxA);
		pick.add(new JLabel("VS"));
		pick.add(boxB);
		pick.add(button("START BATTLE", e -> startBattle()));

		JPanel fighters = new JPanel(new GridLayout(1, 2, 12, 0));
		fighters.add(panelA);
		fighters.add(panelB);

		attackBtn.addActionListener(e -> doAction(false));
		specialBtn.addActionListener(e -> doAction(true));
		undoBtn.addActionListener(e -> undo());
		JPanel buttons = new JPanel();
		buttons.add(attackBtn);
		buttons.add(specialBtn);
		buttons.add(undoBtn);

		logArea.setEditable(false);
		JPanel bottom = new JPanel(new BorderLayout(0, 6));
		bottom.add(turnLabel, BorderLayout.NORTH);
		bottom.add(buttons, BorderLayout.CENTER);
		bottom.add(new JScrollPane(logArea), BorderLayout.SOUTH);

		JPanel root = new JPanel(new BorderLayout(0, 10));
		root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
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

	/** Validates the form and returns a new character, or null after showing an error. */
	private GameCharacter readForm(GameCharacter ignore) {
		String name = nameField.getText().trim();
		String err = null;
		if (name.isEmpty()) {
			err = "Name cannot be empty.";
		} else if (name.length() > 15) {
			err = "Name must be 15 characters or fewer.";
		} else if (!name.matches("[A-Za-z0-9 ]+")) {
			err = "Name may only contain letters, numbers and spaces.";
		} else {
			for (GameCharacter c : roster) {
				if (c != ignore && c.getName().equalsIgnoreCase(name)) {
					err = "A character named \"" + name + "\" already exists.";
				}
			}
		}
		int hp = -1;
		try {
			hpSpinner.commitEdit();
			hp = ((Number) hpSpinner.getValue()).intValue();
		} catch (ParseException ex) {
			// hp stays -1
		}
		if (err == null && (hp < 1 || hp > 500)) {
			err = "Health must be a whole number between 1 and 500.";
		}
		if (err != null) {
			statusLabel.setText(err);
			JOptionPane.showMessageDialog(this, err, "Invalid input", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		return CharacterFactory.create((String) typeBox.getSelectedItem(), name, hp);
	}

	private void addCharacter() {
		GameCharacter c = readForm(null);
		if (c == null) {
			return;
		}
		roster.add(c);
		refreshTable();
		refreshCombos();
		clearForm();
		statusLabel.setText("Added " + c + ".");
	}

	private void updateCharacter() {
		GameCharacter sel = getSelected();
		if (sel == null) {
			JOptionPane.showMessageDialog(this, "Select a character in the table first.");
			return;
		}
		GameCharacter updated = readForm(sel);
		if (updated == null) {
			return;
		}
		roster.set(roster.indexOf(sel), updated);
		if (sel == fighterA || sel == fighterB) {
			resetBattle("Battle reset because a fighter was updated.");
		}
		refreshTable();
		refreshCombos();
		statusLabel.setText("Updated " + updated + ".");
	}

	private void removeCharacter() {
		GameCharacter sel = getSelected();
		if (sel == null) {
			JOptionPane.showMessageDialog(this, "Select a character in the table first.");
			return;
		}
		if (JOptionPane.showConfirmDialog(this, "Remove " + sel + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}
		roster.remove(sel);
		if (sel == fighterA || sel == fighterB) {
			resetBattle("Battle reset because a fighter was removed.");
		}
		refreshTable();
		refreshCombos();
		clearForm();
		statusLabel.setText("Removed " + sel + ".");
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
	}

	private void updateAbilityLabel() {
		abilityLabel.setText("<html><body style='width:200px'>"
				+ CharacterFactory.create((String) typeBox.getSelectedItem(), "x", 1).getDescription());
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
		if (a == null || b == null || a == b) {
			JOptionPane.showMessageDialog(this, "Choose two different fighters.");
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
		logArea.setText("=== " + a.getName() + " vs " + b.getName() + " ===\n");
		battleActive = true;
		showFighters();
		refreshTable();
		refreshBattleUI();
	}

	private void resetBattle(String reason) {
		battleActive = false;
		fighterA = null;
		fighterB = null;
		turnOrder.clear();
		history.clear();
		showFighters();
		logArea.setText(reason + "\n");
		refreshBattleUI();
	}

	private void showFighters() {
		panelA.show(fighterA, false);
		panelB.show(fighterB, false);
	}

	private void doAction(boolean special) {
		if (!battleActive || turnOrder.isEmpty()) {
			return;
		}
		GameCharacter cur = turnOrder.peek();
		GameCharacter tgt = (cur == fighterA) ? fighterB : fighterA;
		history.push(new Snapshot(cur, fighterA, fighterB, logArea.getText().length()));

		if (special) {
			logArea.append(cur.performSpecial(tgt) + "\n");
		} else {
			logArea.append(cur.attack(tgt) + "\n");
			cur.tickCooldown();
		}
		showFighters();

		if (!tgt.isAlive()) {
			logArea.append(">>> " + cur.getName() + " wins the battle! <<<\n");
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
		turnOrder.clear();
		turnOrder.add(s.current);
		turnOrder.add(s.current == fighterA ? fighterB : fighterA);
		battleActive = true;
		String text = logArea.getText();
		logArea.setText(text.substring(0, Math.min(s.logLength, text.length())));
		showFighters();
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

	private JButton button(String text, java.awt.event.ActionListener action) {
		JButton b = styled(new JButton(text));
		b.addActionListener(action);
		return b;
	}

	private static JButton styled(JButton b) {
		b.setBackground(DARK_RED);
		b.setForeground(Color.WHITE);
		b.setFocusPainted(false);
		return b;
	}
}
