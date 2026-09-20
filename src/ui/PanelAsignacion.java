package ui;

import data.enumerate.EstadoPedido;
import model.core.Pedido;
import model.entities.business.ZonaCarga;
import model.entities.dealer.Repartidor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PanelAsignacion extends JPanel {

    private final ArrayList<Pedido> listaPedidos;
    private final ArrayList<Repartidor> listaRepartidores;
    private final ZonaCarga zonaCarga;

    public PanelAsignacion(Navegador navegador, ArrayList<Pedido> listaPedidos, ArrayList<Repartidor> listaRepartidores,
                           ZonaCarga zonaCarga) {
        this.listaPedidos = listaPedidos;
        this.listaRepartidores = listaRepartidores;
        this.zonaCarga = zonaCarga;
        construirInterfaz(navegador);
    }

    private void construirInterfaz(Navegador navegador) {
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Asignación de Pedidos", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JPanel panelCentro = new JPanel(new GridLayout(3, 1, 15, 15));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JLabel etiqueta = new JLabel("Seleccione el método de asignación:", SwingConstants.CENTER);
        JButton botonAutomatica = new JButton("Automática");
        JButton botonManual = new JButton("Manual");

        botonAutomatica.addActionListener(e -> ejecutarAsignacionAutomatica());
        botonManual.addActionListener(e -> navegador.irA(VentanaPrincipal.ASIGNACION_MANUAL));

        panelCentro.add(etiqueta);
        panelCentro.add(botonAutomatica);
        panelCentro.add(botonManual);

        JButton botonVolver = new JButton("⬅ Volver al menú");
        botonVolver.addActionListener(e -> navegador.volver());
        JPanel panelBoton = new JPanel();
        panelBoton.add(botonVolver);

        add(titulo, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);
    }

    // Reproduce la lógica de GestorFases.asignarAutomatico, adaptada para no
    // depender de Scanner/consola: recorre cada pedido PENDIENTE y le asigna
    // el primer repartidor elegible según validarRequisitos. Al terminar, se
    // permanece en este mismo panel (no se navega automáticamente) por si el
    // usuario quiere revisar el resultado en la Zona de Carga o probar
    // también la asignación Manual con lo que haya quedado pendiente.
    private void ejecutarAsignacionAutomatica() {
        int confirmados = 0;
        int sinRepartidor = 0;

        for (Pedido pedido : listaPedidos) {
            if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
                continue; // Ya fue procesado antes (confirmado o cancelado)
            }

            boolean asignado = false;
            for (Repartidor candidato : listaRepartidores) {
                pedido.asignarRepartidor(candidato);
                if (pedido.getRepartidorAsignado() != null) {
                    candidato.agregarPedido(pedido);
                    zonaCarga.agregarPedido(pedido); // ahora CONFIRMADO: se habilita para retiro
                    confirmados++;
                    asignado = true;
                    break;
                }
            }

            if (!asignado) {
                sinRepartidor++;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Asignación automática completada.\n\n"
                        + "Pedidos confirmados: " + confirmados + "\n"
                        + "Pedidos sin repartidor elegible: " + sinRepartidor,
                "Resultado de la asignación", JOptionPane.INFORMATION_MESSAGE);
    }
}