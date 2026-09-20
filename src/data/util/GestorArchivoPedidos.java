package data.util;

import data.enumerate.TipoPedido;
import model.core.Pedido;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Utilitario de soporte para la interfaz gráfica: calcula el próximo ID
// correlativo disponible para un tipo de pedido, y agrega nuevas líneas al
// archivo de pedidos sin alterar las que ya existen.
public class GestorArchivoPedidos {

    // Recorre TODOS los pedidos actualmente en memoria, filtra los que
    // comparten la sigla del tipo solicitado (COM/ENC/EXP) calificando cada
    // ID existente, y calcula el correlativo siguiente al máximo encontrado.
    // Si no existe ningún pedido de ese tipo todavía, comienza en 001.
    public static String siguienteId(TipoPedido tipo, ArrayList<Pedido> listaPedidos) {
        String sigla = tipo.getSigla();
        int maxCorrelativo = 0;

        for (Pedido pedido : listaPedidos) {
            String idPedido = pedido.getIdPedido(); // ej: "COM-020"
            String[] partes = idPedido.split("-");
            if (partes.length == 2 && partes[0].equalsIgnoreCase(sigla)) {
                try {
                    int numero = Integer.parseInt(partes[1]);
                    if (numero > maxCorrelativo) {
                        maxCorrelativo = numero;
                    }
                } catch (NumberFormatException ignored) {
                    // Línea con formato de ID inesperado: se ignora para el cálculo.
                }
            }
        }

        return sigla + "-" + String.format("%03d", maxCorrelativo + 1);
    }

    // Agrega una línea nueva al final del archivo de pedidos, sin tocar el
    // contenido existente.
    public static void agregarLinea(String lineaPedido, String rutaArchivo) {
        try (FileWriter fw = new FileWriter(rutaArchivo, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println(lineaPedido);
        } catch (IOException e) {
            System.err.println("Error al guardar el pedido en el archivo: " + e.getMessage());
        }
    }
}