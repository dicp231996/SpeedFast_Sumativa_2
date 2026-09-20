package app;

import ui.VentanaPrincipal;

import javax.swing.SwingUtilities;

public class SpeedFastGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}