CREATE DATABASE IF NOT EXISTS bd_keycloak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON bd_keycloak.* TO 'logistica_app'@'%';
FLUSH PRIVILEGES;
