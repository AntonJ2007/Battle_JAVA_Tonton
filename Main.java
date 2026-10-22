import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point of the Game Character Manager + Battle Arena. */
public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception ignored) {
		}
		UIManager.put("TabbedPane.contentAreaColor", Theme.BG);
		UIManager.put("TabbedPane.selected", Theme.ACCENT);
		UIManager.put("TabbedPane.background", Theme.PANEL);
		UIManager.put("TabbedPane.foreground", Theme.TEXT);
		UIManager.put("TabbedPane.focus", Theme.ACCENT);
		UIManager.put("OptionPane.background", Theme.PANEL);
		UIManager.put("Panel.background", Theme.PANEL);
		UIManager.put("OptionPane.messageForeground", Theme.TEXT);

		SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
	}
}
