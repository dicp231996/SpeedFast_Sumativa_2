# SpeedFast 🚀

## Descripción de la Aplicación

**SpeedFast** es un sistema de gestión de logística y despachos enfocado en la administración ágil de envíos. La plataforma coordina eficientemente a los **repartidores** y clasifica los requerimientos operativos en tres tipos principales: **Pedidos de Comida, Encomiendas y Pedidos Express**.

El proyecto está desarrollado bajo los principios de la Programación Orientada a Objetos, implementando una arquitectura estructurada que separa la lógica del dominio (entidades, modelos principales y objetos de valor) de la persistencia de datos y utilidades. La carga de información inicial se realiza dinámicamente mediante lectura de archivos de texto sin formato y **reflexión** (`GestorInstancias`).

Además del uso de interfaces comunes entre las distintas jerarquías de clases, el sistema incorpora **concurrencia real**: cada repartidor se ejecuta en su propio hilo (administrados mediante un `ExecutorService`), y la entrega de pedidos entre la Fase de asignación y la Fase de ejecución se coordina a través de una **Zona de Carga** protegida con `synchronized` y respaldada por un `BlockingQueue`, garantizando que cada pedido sea retirado una única vez.

Objetivo de la actividad: trabajar con interfaces comunes para las diferentes clases y aplicar mecanismos de concurrencia (hilos, `ExecutorService`, colecciones concurrentes) sobre un modelo de dominio orientado a objetos.

---

## Ciclo de vida de un pedido

Cada `Pedido` avanza a través de los siguientes estados (`EstadoPedido`), controlados desde el propio pedido mediante el método `nuevoEstado(...)`:

```text
PENDIENTE  →  CONFIRMADO  →  EN_REPARTO  →  ENTREGADO
                  ↓
              CANCELADO (si se cancela antes de ser retirado, o en ruta para PedidoComida)
```

- **PENDIENTE**: el pedido fue creado y espera ser evaluado en la Fase de asignación.
- **CONFIRMADO**: un repartidor cumplió `validarRequisitos(...)` y quedó asignado; el pedido ingresa a la **Zona de Carga**, disponible para ser retirado.
- **EN_REPARTO**: el repartidor asignado retiró el pedido de la Zona de Carga y comenzó su entrega.
- **ENTREGADO**: la simulación de entrega (`HiloEntrega`) finalizó exitosamente.
- **CANCELADO**: el pedido fue cancelado (antes de despacho, o en ruta mediante la excepción de `PedidoComida`); es un estado terminal, no vuelve a ningún pool de espera.

---

## Estructura del Proyecto

```text
├── resources/
│   ├── pedidos.txt
│   └── repartidores.txt
└── src/
    ├── app/
    │   ├── SpeedFast.java
    │   └── SpeedFastGUI.java
    ├── data/
    │   ├── enumerate/
    │   │   ├── TipoServicio.java
    │   │   ├── EstadoPedido.java
    │   │   └── TipoPedido.java
    │   └── util/
    │       ├── ControladorEnvios.java
    │       ├── GestorArchivoPedidos.java
    │       ├── GestorFases.java
    │       ├── GestorInstancias.java
    │       └── LectorDatos.java
    ├── model/
    │   ├── core/
    │   │   ├── Pedido.java
    │   │   └── Persona.java
    │   ├── entities/
    │   │   ├── business/
    │   │   │   └── ZonaCarga.java
    │   │   ├── dealer/
    │   │   │   └── Repartidor.java
    │   │   └── order/
    │   │       ├── PedidoComida.java
    │   │       ├── PedidoEncomienda.java
    │   │       └── PedidoExpress.java
    │   ├── interfaces/
    │   │   ├── IDespachable.java
    │   │   ├── ICancelable.java
    │   │   ├── IRastreable.java
    │   │   └── IRunnable.java
    │   └── valueobjects/
    │       └── HiloEntrega.java
    └── ui/
        ├── Navegador.java
        ├── VentanaPrincipal.java
        ├── PanelMenuPrincipal.java
        ├── PanelAgregarPedido.java
        ├── PanelZonaCarga.java
        ├── PanelAsignacion.java
        └── PanelAsignacionManual.java
```

### Convención de paquetes

- **`model.core`**: superclases del dominio (`Pedido`, `Persona`) — atributos y comportamientos comunes a todas sus jerarquías de hijos.
- **`model.entities`**: clases concretas, hijas de las superclases de `core`, agrupadas por su rol en el negocio:
  - `entities.order`: los distintos tipos de pedido (`PedidoComida`, `PedidoEncomienda`, `PedidoExpress`).
  - `entities.dealer`: los repartidores (`Repartidor`).
  - `entities.business`: entidades de negocio que coordinan la interacción entre otras entidades, sin pertenecer a ninguna jerarquía de herencia propia (`ZonaCarga`).
- **`model.interfaces`**: contratos comunes implementados por múltiples líneas de herencia (`IDespachable`, `ICancelable`, `IRastreable`, `IRunnable`).
- **`model.valueobjects`**: objetos que encapsulan un proceso o validación puntual, usados desde varias clases/subclases sin tener estado de negocio propio (`HiloEntrega`).
- **`data.enumerate`**: enumeradores del dominio (`TipoServicio`, `EstadoPedido`) y de soporte a la interfaz gráfica (`TipoPedido`, con sus 3 representaciones: texto visible en el combo, clave del enum, y nombre de clase para persistencia).
- **`data.util`**: utilitarios de orquestación y carga de datos, sin representar entidades del negocio (`GestorFases`, `GestorInstancias`, `LectorDatos`, `ControladorEnvios`, `GestorArchivoPedidos`).
- **`ui`**: interfaz gráfica de escritorio (Swing). Un único `JFrame` (`VentanaPrincipal`) con `CardLayout`, donde cada pantalla es un `JPanel` intercambiable en vez de una ventana aparte, coordinados mediante la interfaz `Navegador`.

---

## Interfaz Gráfica (GUI)

Punto de entrada: **`app.SpeedFastGUI`** (independiente de `SpeedFast.java`, que sigue siendo la versión de consola).

`VentanaPrincipal` mantiene el estado compartido de la sesión (`listaPedidos`, `listaRepartidores`, `ZonaCarga`, `ControladorEnvios`) y aloja los siguientes paneles dentro de un mismo `CardLayout`, navegables **sin cerrar ninguna ventana**:

- **`PanelMenuPrincipal`**: menú con 3 botones — Añadir Pedido, Ver Zona de Carga, Asignar Pedidos.
- **`PanelAgregarPedido`**: formulario de registro. El combo de tipo (`TipoPedido`) determina dinámicamente qué constructor se usa (`PedidoComida`/`PedidoExpress` con 3 parámetros, `PedidoEncomienda` con 4, mostrando el campo Peso solo en ese caso) y el ID correlativo se autogenera calificando cada pedido existente por su sigla (`COM`/`ENC`/`EXP`), nunca editable a mano.
- **`PanelZonaCarga`**: tabla de solo lectura con todos los pedidos registrados, ordenados por ID correlativo (orden en memoria, no afecta `pedidos.txt`), con filtro por tipo y un resumen de conteos (`Comida | Encomienda | Express | Total`).
- **`PanelAsignacion`**: elige entre asignación Automática (ejecuta la misma regla de `GestorFases.asignarAutomatico`) o Manual.
- **`PanelAsignacionManual`**: por cada pedido pendiente, muestra los repartidores elegibles (`validarRequisitos`) para elegir uno.

La navegación usa la interfaz **`Navegador`** (`irA(nombrePanel)` / `volver()`), implementada por `VentanaPrincipal` con una pila de historial: "Volver" siempre regresa a la pantalla anterior real (por ejemplo, desde Asignación Manual vuelve a Asignación, no directo al menú). Como los paneles se crean una sola vez y se reutilizan, cada uno expone un método público de refresco (`actualizarDatos()`, `actualizarFormulario()`, `actualizarListaPendientes()`) que se invoca justo antes de mostrarlo, para reflejar cambios hechos en otras pantallas.

---

## Componentes clave

| Clase | Responsabilidad |
|---|---|
| `SpeedFast` | Punto de entrada de consola; orquesta el orden de las 5 fases del sistema. |
| `SpeedFastGUI` | Punto de entrada de la interfaz gráfica; lanza `VentanaPrincipal`. |
| `VentanaPrincipal` | Único `JFrame` de la GUI; mantiene el estado de sesión y administra la navegación entre paneles vía `CardLayout`. |
| `GestorFases` | Contiene la lógica de cada fase (asignación, despacho, cancelaciones, ejecución de rutas, reportes) para la versión de consola. |
| `GestorInstancias` | Carga `pedidos.txt` (por reflexión) y `repartidores.txt` desde disco. |
| `GestorArchivoPedidos` | Soporte de la GUI: calcula el próximo ID correlativo por tipo y agrega nuevas líneas a `pedidos.txt`. |
| `ControladorEnvios` | Filtra repartidores elegibles y lleva el historial de entregas exitosas. |
| `ZonaCarga` | Pool compartido y sincronizado (`ArrayList` + `BlockingQueue`) desde donde cada repartidor retira, en una sola visita, toda su carga de pedidos `CONFIRMADO`s. |
| `Repartidor` | Corre en su propio hilo; retira su carga de la `ZonaCarga`, sale a ruta, entrega cada pedido y vuelve por una carga nueva hasta que no quede nada disponible. Mochila de capacidad fija: máximo 5 pedidos (`Pedido[5]`). |
| `HiloEntrega` | Simula, en un hilo real, las etapas de una entrega individual con tiempos de espera aleatorios. |

---

## Ejecución (versión de consola)

Para la versión gráfica, ver la sección "Interfaz Gráfica (GUI)" más arriba.

1. `SpeedFast` carga los pedidos y repartidores desde `resources/`.
2. **Fase 1**: se asignan repartidores (automática, manual o nominal) — los pedidos pasan a `CONFIRMADO` e ingresan a la `ZonaCarga`.
3. **Fase 2**: se muestra el resumen y se despachan los pedidos con repartidor asignado.
4. **Fase 3**: ventana de cancelaciones tardías (excepción exclusiva de `PedidoComida`).
5. **Fase 4**: un `ExecutorService` lanza un hilo por repartidor; cada uno retira su carga de la `ZonaCarga`, entrega sus pedidos (`EN_REPARTO` → `ENTREGADO`) y vuelve por más hasta agotar el pool.
6. **Fase 5**: se registra el historial de entregas exitosas y se muestra el total de pedidos entregados correctamente.