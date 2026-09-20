package ui;

// Contrato mínimo que necesita cada panel para navegar sin conocer los
// detalles de CardLayout ni el historial de navegación: solo pide "ir a tal
// panel" o "volver al anterior".
public interface Navegador {
    void irA(String nombrePanel);
    void volver();
}