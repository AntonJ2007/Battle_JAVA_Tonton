import javax.swing.*;
import java.awt.*;

public class RoundButton extends JButton {
	private final Color base;

	RoundButton(String text, Color base) {
		super(text);
		this.base = base;
		setForeground(Color.WHITE);
		setFont(new Font("SansSerif", Font.BOLD, 13));
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusPainted(false);
		setOpaque(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color c = !isEnabled() ? new Color(0x3A, 0x3E, 0x5C)
				: getModel().isPressed() ? base.darker()
				: getModel().isRollover() ? base.brighter() : base;
		g.setColor(c);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
		g.dispose();
		super.paintComponent(g0);
	}
}
