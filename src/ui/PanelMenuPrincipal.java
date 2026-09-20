package ui;

import javax.swing.*;
import java.awt.*;

public class PanelMenuPrincipal extends JPanel {

    public PanelMenuPrincipal(Navegador navegador) {
        setLayout(new GridLayout(4, 1, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JLabel titulo = new JLabel("SpeedFast \uD83D\uDE80", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));

        JButton btnAgregarPedido = new JButton("Añadir Pedido");
        JButton btnVerZonaCarga = new JButton("Ver Zona de Carga");
        JButton btnAsignacion = new JButton("Asignar Pedidos");

        btnAgregarPedido.addActionListener(e -> navegador.irA(VentanaPrincipal.AGREGAR));
        btnVerZonaCarga.addActionListener(e -> navegador.irA(VentanaPrincipal.ZONA_CARGA));
        btnAsignacion.addActionListener(e -> navegador.irA(VentanaPrincipal.ASIGNACION));

        add(titulo);
        add(btnAgregarPedido);
        add(btnVerZonaCarga);
        add(btnAsignacion);
    }
}