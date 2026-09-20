package data.enumerate;

// Enumerador de los tipos de pedido disponibles para el formulario de
// registro en la interfaz gráfica. Mantiene 3 representaciones distintas:
//   1) descripcionVisible -> lo que aparece en el JComboBox del formulario.
//   2) el propio nombre del enum (COMIDA / ENCOMIENDA / EXPRESS) -> la clave
//      interna, usada para lógica de programa (switch, comparaciones).
//   3) nombreClase -> el valor EXACTO que se escribe en pedidos.txt, ya que
//      GestorInstancias.cargarPedidos() lo usa tal cual para ubicar la
//      clase por reflexión (Class.forName("model.entities.order." + nombreClase)).
public enum TipoPedido {
    COMIDA("Pedido de Comida", "PedidoComida", "COM"),
    ENCOMIENDA("Pedido de Encomienda", "PedidoEncomienda", "ENC"),
    EXPRESS("Pedido Express", "PedidoExpress", "EXP");

    private final String descripcionVisible;
    private final String nombreClase;
    private final String sigla;

    TipoPedido(String descripcionVisible, String nombreClase, String sigla) {
        this.descripcionVisible = descripcionVisible;
        this.nombreClase = nombreClase;
        this.sigla = sigla;
    }

    public String getDescripcionVisible() {
        return descripcionVisible;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    // Sigla usada en el ID correlativo del pedido (ej: "COM" en "COM-021").
    public String getSigla() {
        return sigla;
    }

    // Esto es lo que Swing muestra por defecto en el JComboBox.
    @Override
    public String toString() {
        return descripcionVisible;
    }
}