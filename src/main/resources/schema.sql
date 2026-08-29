-- Se ejecuta automaticamente al arrancar (spring.sql.init.mode=always),
-- ANTES de que Hibernate haga su ddl-auto=update.
-- Hibernate crea TABLAS pero NO crea los schemas/namespaces: hay que hacerlo aca.
CREATE SCHEMA IF NOT EXISTS admin_users;
CREATE SCHEMA IF NOT EXISTS platform_project;
