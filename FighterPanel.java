import javax.swing.*;
import java.awt.*;

/** One side of the arena: portrait, name, type and health bar. */
public class FighterPanel extends JPanel {
	private final Portrait portrait = new Portrait();
	private final JLabel nameLabel = new JLabel("-", SwingConstants.CENTER);
	private final JLabel infoLabel = new JLabel(" ", SwingConstants.CENTER);
	private final HealthBar bar = new HealthBar();

	FighterPanel() {
		setLayout(new BorderLayout(0, 8));
		setBackground(Theme.PANEL);
		setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
		nameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		nameLabel.setForeground(Theme.TEXT);
		infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		infoLabel.setForeground(Theme.MUTED);
		JPanel top = new JPanel(new GridLayout(2, 1));
		top.setOpaque(false);
		top.add(nameLabel);
		top.add(infoLabel);
		add(top, BorderLayout.NORTH);
		add(portrait, BorderLayout.CENTER);
		add(bar, BorderLayout.SOUTH);
	}

	void show(GameCharacter c, boolean animate) {
		portrait.setCharacter(c);
		if (c == null) {
			nameLabel.setText("-");
			infoLabel.setText(" ");
			bar.setValues(0, 1, false);
			return;
		}
		nameLabel.setText(c.getName());
		infoLabel.setText(c.getType() + "  |  " + c.getAttackName() + " / " + c.getSpecialName());
		bar.setValues(c.getHealth(), c.getMaxHealth(), animate);
	}

	void hit() {
		portrait.shake();
	}
}
