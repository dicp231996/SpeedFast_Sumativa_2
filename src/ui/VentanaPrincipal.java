package ui;

import data.util.ControladorEnvios;
import data.util.GestorInstancias;
import model.core.Pedido;
import model.entities.business.ZonaCarga;
import model.entities.dealer.Repartidor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class VentanaPrincipal extends JFrame implements Navegador {

    public static final String MENU = "menu";
    public static final String AGREGAR = "agregar";
    public static final String ZONA_CARGA = "zonaCarga";
    public static final String ASIGNACION = "asignacion";
    public static final String ASIGNACION_MANUAL = "asignacionManual";

    private final String rutaPedidos = "resources/pedidos.txt";
    private final String rutaRepartidores = "resources/repartidores.txt";

    private final ArrayList<Pedido> listaPedidos;
    private final ArrayList<Repartidor> listaRepartidores;
    private final ZonaCarga zonaCarga;
    private final ControladorEnvios controlador;

    private final CardLayout cardLayout;
    private final JPanel panelContenedor;
    private final Deque<String> historial;

    private PanelAgregarPedido panelAgregarPedido;
    private PanelZonaCarga panelZonaCarga;
    private PanelAsignacionManual panelAsignacionManual;

    public VentanaPrincipal() {
        super("SpeedFast - Sistema de Gestión de Despachos");

        this.listaPedidos = GestorInstancias.cargarPedidos(rutaPedidos);
        this.listaRepartidores = GestorInstancias.cargarRepartidores(rutaRepartidores);
        this.zonaCarga = new ZonaCarga();
        this.controlador = new ControladorEnvios();

        // Registramos en la Zona de Carga todos los pedidos ya existentes en
        // el archivo (quedan PENDIENTE hasta que se ejecute la asignación).
        for (Pedido pedido : listaPedidos) {
            zonaCarga.agregarPedido(pedido);
        }

        this.cardLayout = new CardLayout();
        this.panelContenedor = new JPanel(cardLayout);
        this.historial = new ArrayDeque<>();

        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 520);
        setLocationRelativeTo(null);
        setResizable(true);

        PanelMenuPrincipal panelMenu = new PanelMenuPrincipal(this);
        panelAgregarPedido = new PanelAgregarPedido(this, listaPedidos, zonaCarga, rutaPedidos);
        panelZonaCarga = new PanelZonaCarga(zonaCarga, this);
        panelAsignacionManual = new PanelAsignacionManual(this, listaPedidos, listaRepartidores, controlador, zonaCarga);
        PanelAsignacion panelAsignacion = new PanelAsignacion(this, listaPedidos, listaRepartidores, zonaCarga);

        panelContenedor.add(panelMenu, MENU);
        panelContenedor.add(panelAgregarPedido, AGREGAR);
        panelContenedor.add(panelZonaCarga, ZONA_CARGA);
        panelContenedor.add(panelAsignacion, ASIGNACION);
        panelContenedor.add(panelAsignacionManual, ASIGNACION_MANUAL);

        setContentPane(panelContenedor);

        historial.push(MENU);
        cardLayout.show(panelContenedor, MENU);
    }

    // Navega hacia adelante: apila el panel actual antes de mostrar el nuevo,
    // para que "volver()" sepa a cuál regresar.
    @Override
    public void irA(String nombrePanel) {
        refrescarPanel(nombrePanel);
        historial.push(nombrePanel);
        cardLayout.show(panelContenedor, nombrePanel);
    }

    // Descarta el panel actual de la pila y muestra el que quedó como tope
    // (el que se estaba viendo justo antes). Si ya estamos en el menú
    // principal (base de la pila), no hace nada.
    @Override
    public void volver() {
        if (historial.size() <= 1) {
            return;
        }
        historial.pop();
        String anterior = historial.peek();
        refrescarPanel(anterior);
        cardLayout.show(panelContenedor, anterior);
    }

    // Antes de mostrar ciertos paneles, refrescamos su contenido para que
    // reflejen cualquier cambio hecho desde la última vez que se vieron
    // (nuevos pedidos agregados, asignaciones realizadas, etc.), ya que al
    // usar CardLayout cada panel se crea una sola vez y se reutiliza.
    private void refrescarPanel(String nombrePanel) {
        if (ZONA_CARGA.equals(nombrePanel)) {
            panelZonaCarga.actualizarDatos();
        } else if (AGREGAR.equals(nombrePanel)) {
            panelAgregarPedido.actualizarFormulario();
        } else if (ASIGNACION_MANUAL.equals(nombrePanel)) {
            panelAsignacionManual.actualizarListaPendientes();
        }
    }
}