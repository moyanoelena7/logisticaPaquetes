## Diagrama BPMN

1. El administrador registra el paquete en `ms-paquetes` con su sucursal, cliente, dimensiones, peso y destino. El paquete queda `PENDIENTE`.
2. El administrador selecciona entre 6 y 99 paquetes pendientes distintos de la misma sucursal y sector. `ms-logistica` guarda el envío como `CREADO`.
3. El administrador elige vehículo y repartidor. `ms-paquetes` comprueba que sean de la sucursal, estén disponibles y que la carga respete ambos límites del vehículo. Al confirmar la reserva de paquetes y flota, `ms-logistica` pasa el envío a `ASIGNADO`.
4. OSRM Trip recibe las coordenadas de la sucursal y de todos los destinos. Devuelve el recorrido abierto, desde la sucursal hasta la última entrega que elige Trip. `ms-logistica` guarda la ruta y pasa el envío a `PLANIFICADO`.
5. El repartidor asignado inicia el reparto. El envío y sus paquetes pasan a `EN_CAMINO`. Cada intento se registra como `ENTREGADO` o `INCIDENCIA`, con motivo en caso de incidencia.
6. El último intento deja el envío en `PENDIENTE_RETORNO`. Cuando el repartidor confirma que volvió a la sucursal y está disponible, se liberan el vehículo y el repartidor y el envío queda `CERRADO`. El administrador puede reprogramar los paquetes con incidencia de ese envío.

El administrador puede cancelar el envío antes de iniciar el reparto. Si tiene reserva, se libera la flota y los paquetes vuelven a `PENDIENTE`.

```mermaid
flowchart TD
    A(["El paquete llega a la sucursal"]) --> B["Administrador registra el paquete en ms-paquetes"]
    B --> C["Paquete PENDIENTE"]
    C --> D["Administrador selecciona paquetes de la misma sucursal y sector"]
    D --> E{"¿Entre 6 y 99 paquetes pendientes distintos?"}
    E -- No --> F["Revisar la selección"] --> D
    E -- Sí --> G["ms-logistica guarda el envío como CREADO"]
    G --> H["Administrador elige vehículo y repartidor"]
    H --> I{"¿Paquetes y flota disponibles y carga dentro de los límites?"}
    I -- No --> J["Informar el motivo del rechazo de la reserva"]
    I -- Sí --> K["ms-paquetes reserva los paquetes y la flota"]
    K --> L["ms-logistica confirma el envío ASIGNADO"]
    L --> M["OSRM Trip calcula el recorrido abierto desde la sucursal"]
    M --> N["ms-logistica guarda la ruta y pasa a PLANIFICADO"]
    N --> O["Repartidor inicia el envío y pasa a EN_CAMINO"]
    O --> P{"¿Se entregó el paquete?"}
    P -- Sí --> Q["Registrar ENTREGADO"]
    P -- No --> R["Registrar INCIDENCIA con motivo"]
    Q --> S{"¿Quedan entregas por intentar?"}
    R --> S
    S -- Sí --> P
    S -- No --> T["Envío PENDIENTE_RETORNO"]
    T --> U["Repartidor confirma regreso y disponibilidad"]
    U --> V(["Liberar flota y confirmar envío CERRADO"])
    V -. Incidencia .-> W["Administrador puede devolver el paquete a PENDIENTE"]
```

## Arquitectura Persistencia

### Diagrama entidad relación de microservicio paquetes

``` mermaid
erDiagram
SUCURSAL ||--o{ PAQUETE : "recibe"
SUCURSAL ||--o{ VEHICULO : "alberga"
SUCURSAL ||--o{ REPARTIDOR : "asigna"
CLIENTE ||--o{ PAQUETE : "solicita"


    SUCURSAL {
        bigint id PK
        string nombre
        string direccion
        decimal latitud
        decimal longitud
        boolean activo
    }


    CLIENTE {
        bigint id PK
        string nombre
        string telefono
        string email
    }


    PAQUETE {
        bigint id PK
        bigint cliente_id FK
        bigint sucursal_id FK
        string sector
        string direccion_envio
        decimal latitud
        decimal longitud
        decimal alto_cm
        decimal ancho_cm
        decimal largo_cm
        decimal peso_kg
        string estado
        timestamp fecha_registro
    }


    VEHICULO {
        bigint id PK
        bigint sucursal_id FK
        string patente UK
        string modelo
        decimal capacidad_peso_kg
        decimal capacidad_volumen_m3
        boolean disponible
    }


    REPARTIDOR {
        bigint id PK
        bigint sucursal_id FK
        string nombre
        string telefono
        string username UK
        boolean disponible
    }
```

### Diagrama entidad relación de microservicio logística

Modelo previsto para `ms-logistica`. `sucursal_id`, `paquete_id`, `vehiculo_id` y `repartidor_id` son referencias externas a `ms-paquetes`.

```mermaid
erDiagram
    ENVIO ||--|{ ENVIO_PAQUETE : "incluye"
    ENVIO ||--o| ASIGNACION : "posee"
    ENVIO ||--o| RUTA_PLANIFICADA : "genera"
    RUTA_PLANIFICADA ||--|{ PARADA_RUTA : "contiene"
    ENVIO_PAQUETE ||--o| PARADA_RUTA : "se visita en"


    ENVIO {
        bigint id PK
        bigint sucursal_id
        string sector
        string estado
        timestamp fecha_creacion
        timestamp fecha_inicio
        timestamp fecha_fin
    }

    ENVIO_PAQUETE {
        bigint id PK
        bigint envio_id FK
        bigint paquete_id
        string direccion_destino
        decimal latitud_destino
        decimal longitud_destino
        string resultado
        timestamp fecha_intento
        string motivo_incidencia
    }


    ASIGNACION {
        bigint id PK
        bigint envio_id FK, UK
        bigint vehiculo_id
        bigint repartidor_id
        string repartidor_sub
        decimal peso_total_kg
        decimal volumen_total_m3
        decimal latitud_salida
        decimal longitud_salida
        timestamp fecha_asignacion
    }


    RUTA_PLANIFICADA {
        bigint id PK
        bigint envio_id FK, UK
        decimal distancia_metros
        decimal duracion_segundos
        text geometria_geojson
        timestamp fecha_calculo
    }


    PARADA_RUTA {
        bigint id PK
        bigint ruta_id FK
        bigint envio_paquete_id FK, UK
        int orden_visita
    }
```
