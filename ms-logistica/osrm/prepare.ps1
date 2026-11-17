# Descarga y preprocesa el extracto de OSM que usa el servicio osrm.
# Ejecutar una sola vez (o cuando se quiera actualizar el mapa):
#   cd ms-logistica\osrm
#   .\prepare.ps1
#
# Genera en .\data\ los archivos cordoba.osm.pbf y cordoba.osrm.* que consume
# el contenedor osrm (docker compose --profile osrm up -d).
# Los archivos generados NO se versionan (ver .gitignore).

param(
  [string]$Url  = "https://download.openstreetmap.fr/extracts/south-america/argentina/cordoba-latest.osm.pbf",
  [string]$Name = "cordoba"
)
$ErrorActionPreference = "Stop"

# Misma etiqueta que el servicio osrm de docker-compose.yml: los archivos
# .osrm.* son binarios y dependen de la version. Si cambia una, cambiar la otra.
$image = "ghcr.io/project-osrm/osrm-backend:v6.0.0"
$data  = Join-Path $PSScriptRoot "data"
New-Item -ItemType Directory -Force -Path $data | Out-Null

$pbf = Join-Path $data "$Name.osm.pbf"
if ((Test-Path $pbf) -and (Get-Item $pbf).Length -lt 1MB) {
  Write-Host "Descarga incompleta detectada, se vuelve a bajar"
  Remove-Item $pbf -Force
}
if (-not (Test-Path $pbf)) {
  Write-Host "Descargando $Url"
  Invoke-WebRequest $Url -OutFile $pbf
}

# --mount en lugar de -v: en Windows la ruta C:\... rompe el parseo con ":"
$mount = @("--rm", "--mount", "type=bind,source=$data,target=/data")

docker run @mount $image osrm-extract   -p /opt/car.lua "/data/$Name.osm.pbf"; if ($LASTEXITCODE) { throw "osrm-extract fallo" }
docker run @mount $image osrm-partition "/data/$Name.osrm";                  if ($LASTEXITCODE) { throw "osrm-partition fallo" }
docker run @mount $image osrm-customize "/data/$Name.osrm";                  if ($LASTEXITCODE) { throw "osrm-customize fallo" }

Write-Host "Listo. Levantar OSRM con: docker compose --profile osrm up -d"
