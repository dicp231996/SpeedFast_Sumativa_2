package ui;

import model.core.Pedido;
import model.entities.business.ZonaCarga;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class PanelZonaCarga extends JPanel {

    private static final String FILTRO_TODOS = "Todos";
    private static final String[] TIPOS = {"Comida", "Encomienda", "Express"};

    private final ZonaCarga zonaCarga;
    private ArrayList<Pedido> pedidosOrdenados;
    private DefaultTableModel modeloTabla;
    private JLabel etiquetaResumen;
    private JComboBox<String> comboFiltro;

    public PanelZonaCarga(ZonaCarga zonaCarga, Navegador navegador) {
        this.zonaCarga = zonaCarga;
        construirInterfaz(navegador);
    }

    private void construirInterfaz(Navegador navegador) {
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Zona de Carga - Pedidos Registrados", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        etiquetaResumen = new JLabel();
        etiquetaResumen.setFont(etiquetaResumen.getFont().deriveFont(Font.BOLD));

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFiltro.add(new JLabel("Filtrar por tipo:"));

        String[] opcionesFiltro = new String[TIPOS.length + 1];
        opcionesFiltro[0] = FILTRO_TODOS;
        System.arraycopy(TIPOS, 0, opcionesFiltro, 1, TIPOS.length);

        comboFiltro = new JComboBox<>(opcionesFiltro);
        comboFiltro.addActionListener(e -> aplicarFiltro((String) comboFiltro.getSelectedItem()));
        panelFiltro.add(comboFiltro);

        panelSuperior.add(etiquetaResumen, BorderLayout.NORTH);
        panelSuperior.add(panelFiltro, BorderLayout.SOUTH);

        String[] columnas = {"N°", "ID Pedido", "Tipo", "Dirección de Entrega", "Distancia (km)", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // vista de solo lectura
            }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(tabla);

        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.add(panelSuperior, BorderLayout.NORTH);
        panelCentro.add(scroll, BorderLayout.CENTER);

        JButton botonVolver = new JButton("⬅ Volver al menú");
        botonVolver.addActionListener(e -> navegador.volver());
        JPanel panelBoton = new JPanel();
        panelBoton.add(botonVolver);

        add(titulo, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);

        actualizarDatos();
    }

    // Se invoca cada vez que se vuelve a mostrar este panel (ver
    // VentanaPrincipal.refrescarPanel): recarga la copia de pedidos desde la
    // Zona de Carga (por si se agregaron pedidos o se hicieron asignaciones
    // desde la última vez que se vio esta pantalla), la reordena por ID
    // correlativo y reaplica el filtro "Todos" por defecto.
    public void actualizarDatos() {
        this.pedidosOrdenados = zonaCarga.listarRegistrados();
        this.pedidosOrdenados.sort(Comparator.comparingInt(Pedido::getId));
        actualizarResumen();
        comboFiltro.setSelectedItem(FILTRO_TODOS);
        aplicarFiltro(FILTRO_TODOS);
    }

    private void actualizarResumen() {
        int comida = 0;
        int encomienda = 0;
        int express = 0;

        for (Pedido pedido : pedidosOrdenados) {
            switch (pedido.getTipoPedido()) {
                case "Comida":
                    comida++;
                    break;
                case "Encomienda":
                    encomienda++;
                    break;
                case "Express":
                    express++;
                    break;
                default:
                    break;
            }
        }

        etiquetaResumen.setText(String.format("Comida: %d   |   Encomienda: %d   |   Express: %d   |   Total: %d",
                comida, encomienda, express, pedidosOrdenados.size()));
    }

    private void aplicarFiltro(String filtro) {
        modeloTabla.setRowCount(0);

        for (Pedido pedido : pedidosOrdenados) {
            if (FILTRO_TODOS.equals(filtro) || pedido.getTipoPedido().equals(filtro)) {
                modeloTabla.addRow(new Object[]{
                        pedido.getId(),
                        pedido.getIdPedido(),
                        pedido.getTipoPedido(),
                        pedido.getDireccionEntrega(),
                        pedido.getDistanciaKm(),
                        pedido.getEstado().name()
                });
            }
        }
    }
}