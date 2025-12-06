package EchoNote.App;

public class AppMain {

    public static void main(String[] args) {
        AppConfig config = new AppConfig();

        javax.swing.SwingUtilities.invokeLater(() -> {
            SwingUI ui = new SwingUI(config);
            ui.setVisible(true);
        });
    }
}
