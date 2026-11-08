## Diagrama BPMN

1. Recepción y Registro: Llegada de paquete a la sucursal, el administrador ingresa dimensiones, peso, cliente y dirección. El sistema asigna sector y ID.
2. Agrupación y Control de Lote: El paquete queda en estado PENDIENTE. El sistema evalúa si el sector alcanzó la cantidad mínima.
3. Validación de Recursos y Capacidad: Se busca vehículo y repartidor. Se valida que el volumen y peso del lote no excedan la capacidad máxima.
4. Optimización de la Ruta: El motor de cálculo de OSRM toma las direcciones del lote y genera la secuencia óptima de paradas.
5. Viaje y despacho: El repartidor inicia la hoja de ruta. El estado del envío cambia a EN_CAMINO.
6. Ejecución y Registro de Entrega: El repartidor entrega el paquete al cliente o registra la imposibilidad de entrega (ausente/dirección errónea) y queda como pendiente.

```mermaid
flowchart TD
    subgraph Operación ["Administrador"]
        A([Inicio: Paquete llega a sucursal]) --> B[Ingresar peso, dimensiones, cliente y dirección]
    end


    subgraph S_Envios ["Servicio de Envíos y Tracking"]
        B --> C[Generar ID único y asignar a Sector]
        C --> D[Estado: PENDIENTE]
        D --> E{¿Sector reúne >= 6 paquetes?}
        E -- No --> F[Mantener en espera de lote]
        E -- Sí --> G[Crear Lote de Envío]
    end


    subgraph S_Flota ["Servicio de Flota y Recursos"]
        G --> H{¿Vehículo y Repartidor disponibles?}
        H -- No --> I[Alerta: Recurso no disponible]
        H -- Sí --> J{¿Peso y volumen <= Capacidad Vehículo?}
        J -- No --> K[Dividir lote / Reasignar vehículo]
        J -- Sí --> L[Asignar Vehículo y Repartidor al Envío]
    end


    subgraph S_Rutas ["Servicio de Optimización de Rutas"]
        L --> M[Calcular matriz de distancias y tiempos]
        M --> N[Generar secuencia óptima incluyendo TODAS las paradas]
    end


    subgraph Repartidor ["Repartidor"]
        N --> O[Cargar paquetes en vehículo]
        O --> P[Cambiar estado a EN_TRANSITO]
        P --> Q[Navegar según orden de paradas asignado]
        Q --> R{¿Entrega exitosa en domicilio?}
        R -- No --> S[Registrar INCIDENCIA / Cliente ausente]
        R -- Sí --> T[Registrar confirmación de entrega]
    end


    subgraph Cierre ["Resultado del Proceso"]
        S --> V([Estado Final: REPROGRAMADO / RETORNO])
        T --> U([Estado Final: ENTREGADO])
    end


    style A fill:#4CAF50,stroke:#333,stroke-width:2px,color:#fff
    style U fill:#2196F3,stroke:#333,stroke-width:2px,color:#fff
    style V fill:#FF9800,stroke:#333,stroke-width:2px,color:#fff
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
        boolean disponible
    }
```

### Diagrama entidad relación de microservicio logística

```mermaid
 erDiagram
    ENVIO ||--|| ASIGNACION : "posee"
    ENVIO ||--|| RUTA_PLANIFICADA : "genera"
    RUTA_PLANIFICADA ||--|{ PARADA_RUTA : "contiene"


    ENVIO {
        bigint id PK
        string sector
        string estado
        timestamp fecha_creacion
        timestamp fecha_inicio
        timestamp fecha_fin
    }


    ASIGNACION {
        bigint id PK
        bigint envio_id FK, UK
        bigint vehiculo_id
        bigint repartidor_id
        decimal peso_total_kg
        decimal volumen_total_m3
        timestamp fecha_asignacion
    }


    RUTA_PLANIFICADA {
        bigint id PK
        bigint envio_id FK, UK
        decimal distancia_total_km
        decimal tiempo_estimado_min
        text geometria_osrm
        timestamp fecha_calculo
    }


    PARADA_RUTA {
        bigint id PK
        bigint ruta_id FK
        bigint paquete_id
        int orden_visita
        decimal latitud_destino
        decimal longitud_destino
        string estado_entrega
        timestamp fecha_intento
        string observacion_incidencia
    }
```