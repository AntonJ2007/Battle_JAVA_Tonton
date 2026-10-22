import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/** Painted character portrait with a shake animation when hit. */
public class Portrait extends JComponent {
	private GameCharacter ch;
	private int shakeX = 0;

	Portrait() {
		setPreferredSize(new Dimension(180, 180));
	}

	void setCharacter(GameCharacter c) {
		this.ch = c;
		repaint();
	}

	void shake() {
		final int[] ticks = { 0 };
		Timer t = new Timer(30, null);
		t.addActionListener(e -> {
			ticks[0]++;
			shakeX = (ticks[0] % 2 == 0 ? 1 : -1) * (12 - ticks[0]);
			if (ticks[0] >= 10) {
				shakeX = 0;
				t.stop();
			}
			repaint();
		});
		t.start();
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth(), h = getHeight();
		int size = Math.min(w, h) - 20;
		int cx = w / 2 + shakeX, cy = h / 2;

		if (ch == null) {
			g.setColor(Theme.FIELD);
			g.fillOval(cx - size / 2, cy - size / 2, size, size);
			g.setColor(Theme.MUTED);
			g.setFont(new Font("SansSerif", Font.BOLD, size / 2));
			FontMetrics fm = g.getFontMetrics();
			g.drawString("?", cx - fm.stringWidth("?") / 2, cy + fm.getAscent() / 3);
			g.dispose();
			return;
		}

		Color c = ch.getColor();
		g.setPaint(new java.awt.RadialGradientPaint(new Point2D.Float(cx, cy - size / 6f), size / 1.6f,
				new float[] { 0f, 1f }, new Color[] { c.brighter(), c.darker().darker() }));
		g.fillOval(cx - size / 2, cy - size / 2, size, size);
		g.setColor(new Color(255, 255, 255, 60));
		g.setStroke(new BasicStroke(4f));
		g.drawOval(cx - size / 2, cy - size / 2, size, size);

		g.setColor(new Color(255, 255, 255, 235));
		g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		int s = size / 4;
		if (ch instanceof Warrior) {
			g.drawLine(cx - s, cy + s, cx + s, cy - s);
			int gx = cx - s / 2, gy = cy + s / 2;
			g.drawLine(gx - s / 3, gy - s / 3, gx + s / 3, gy + s / 3);
		} else if (ch instanceof Mage) {
			int[] xs = new int[10];
			int[] ys = new int[10];
			for (int i = 0; i < 10; i++) {
				double ang = -Math.PI / 2 + i * Math.PI / 5;
				double r = (i % 2 == 0) ? s * 1.3 : s * 0.55;
				xs[i] = cx + (int) (Math.cos(ang) * r);
				ys[i] = cy + (int) (Math.sin(ang) * r);
			}
			g.fillPolygon(xs, ys, 10);
		} else if (ch instanceof Archer) {
			g.drawArc(cx - s, cy - s, s * 2, s * 2, 270, 180);
			g.drawLine(cx, cy - s, cx, cy + s);
			g.drawLine(cx - s, cy, cx + s, cy);
		} else {
			g.drawLine(cx - s, cy, cx + s, cy);
			g.drawLine(cx, cy - s, cx, cy + s);
		}

		if (!ch.isAlive()) {
			g.setColor(new Color(0, 0, 0, 160));
			g.fillOval(cx - size / 2, cy - size / 2, size, size);
			g.setColor(Theme.BAD);
			g.setFont(new Font("SansSerif", Font.BOLD, size / 3));
			FontMetrics fm = g.getFontMetrics();
			g.drawString("KO", cx - fm.stringWidth("KO") / 2, cy + fm.getAscent() / 3);
		}
		g.dispose();
	}
}
