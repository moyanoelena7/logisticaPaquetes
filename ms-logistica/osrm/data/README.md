# Datos de OSRM

Esta carpeta contiene los archivos de mapa que consume el servicio `osrm` de
`docker-compose.yml` (se monta en `/data` del contenedor).

No se versiona nada de acá. Se genera ejecutando `prepare.ps1`:

```powershell
cd ms-logistica\osrm
.\prepare.ps1
```

Resultado esperado:

| Archivo | Origen |
|---|---|
| `cordoba.osm.pbf` | descarga del extracto de OSM (provincia de Córdoba, ~40 MB) |
| `cordoba.osrm`, `cordoba.osrm.*` | generados por `osrm-extract`, `osrm-partition` y `osrm-customize` |

Las extensiones `*.osm.pbf` y `*.osrm*` están en el `.gitignore` raíz, así que
Git las ignora aunque estén acá. `docker compose down -v` **no** las borra
(son un bind mount, no un volumen con nombre).

Si falta `cordoba.osrm`, el contenedor `osrm` termina enseguida y los logs
muestran un error de archivo inválido. En ese caso, correr `prepare.ps1`.
