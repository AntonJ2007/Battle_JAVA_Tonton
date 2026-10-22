import javax.swing.*;
import java.awt.*;

/** Health bar that animates smoothly towards its target value. */
public class HealthBar extends JComponent {
	private int max = 100;
	private int target = 0;
	private double shown = 0;
	private final Timer timer;

	HealthBar() {
		setPreferredSize(new Dimension(240, 22));
		timer = new Timer(15, e -> {
			double diff = target - shown;
			if (Math.abs(diff) < 0.6) {
				shown = target;
				((Timer) e.getSource()).stop();
			} else {
				shown += diff * 0.15;
			}
			repaint();
		});
	}

	void setValues(int current, int max, boolean animate) {
		this.max = Math.max(1, max);
		this.target = current;
		if (animate) {
			timer.start();
		} else {
			shown = current;
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth(), h = getHeight();
		g.setColor(Theme.FIELD);
		g.fillRoundRect(0, 0, w, h, h, h);
		double ratio = Math.max(0, Math.min(1, shown / max));
		Color c = ratio > 0.5 ? Theme.GOOD : ratio > 0.25 ? Theme.WARN : Theme.BAD;
		int fw = (int) Math.round(w * ratio);
		if (fw > 0) {
			g.setPaint(new GradientPaint(0, 0, c.brighter(), 0, h, c.darker()));
			g.fillRoundRect(0, 0, Math.max(fw, h), h, h, h);
		}
		g.setColor(Color.WHITE);
		g.setFont(new Font("SansSerif", Font.BOLD, 12));
		String s = Math.round(shown) + " / " + max;
		FontMetrics fm = g.getFontMetrics();
		g.drawString(s, (w - fm.stringWidth(s)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
		g.dispose();
	}
}
