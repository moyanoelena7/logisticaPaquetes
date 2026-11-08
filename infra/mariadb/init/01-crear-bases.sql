CREATE DATABASE IF NOT EXISTS bd_paquetes  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_logistica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_keycloak  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON bd_paquetes.*  TO 'logistica'@'%';
GRANT ALL PRIVILEGES ON bd_logistica.* TO 'logistica'@'%';
GRANT ALL PRIVILEGES ON bd_keycloak.*  TO 'logistica'@'%';
FLUSH PRIVILEGES;