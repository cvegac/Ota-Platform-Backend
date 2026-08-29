-- ---------------------------------------------------------------------------
-- OTA Platform - bootstrap del esquema para la RDS PostgreSQL
-- Fuente: embedded/postgres_setup/backup_complete.sql (pg_dump custom -> plano)
--
-- El backend corre con spring.jpa.hibernate.ddl-auto=update, asi que Hibernate
-- crea/actualiza las TABLAS solo. Lo unico que DEBE existir antes de arrancar
-- son los SCHEMAS. El resto queda documentado aca como referencia / DR.
--
-- Uso:
--   psql "host=db.ota.internal dbname=app_db user=<user> password=<pass>" -f schema.sql
-- ---------------------------------------------------------------------------

CREATE SCHEMA IF NOT EXISTS admin_users;
CREATE SCHEMA IF NOT EXISTS platform_project;

-- ------------------------- admin_users -------------------------
CREATE TABLE IF NOT EXISTS admin_users."user" (
    id                     uuid NOT NULL,
    account_no_expired     boolean,
    account_no_locked      boolean,
    credential_no_expired  boolean,
    email                  varchar(40)  NOT NULL,
    is_enabled             boolean,
    name                   varchar(40)  NOT NULL,
    password               varchar(255) NOT NULL,
    username               varchar(40)  NOT NULL,
    CONSTRAINT user_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS admin_users."user-project" (
    id         uuid NOT NULL,
    project_id uuid NOT NULL,
    user_id    uuid NOT NULL,
    CONSTRAINT "user-project_pkey" PRIMARY KEY (id)
);

-- ------------------------- platform_project -------------------------
CREATE TABLE IF NOT EXISTS platform_project.project (
    id          uuid NOT NULL,
    description varchar(255) NOT NULL,
    name        varchar(40)  NOT NULL,
    CONSTRAINT project_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS platform_project.device_type (
    id          uuid NOT NULL,
    description varchar(40) NOT NULL,
    name        varchar(20) NOT NULL,
    project_id  uuid NOT NULL,
    CONSTRAINT device_type_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS platform_project.device (
    id             uuid NOT NULL,
    description    varchar(255),
    name           varchar(40) NOT NULL,
    device_type_id uuid NOT NULL,
    CONSTRAINT device_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS platform_project.download (
    id        uuid NOT NULL,
    date      timestamp without time zone,
    version   varchar,
    device_id uuid,
    CONSTRAINT download_pkey PRIMARY KEY (id)
);

-- ------------------------- foreign keys -------------------------
ALTER TABLE admin_users."user-project"
    ADD CONSTRAINT fk_userproject_user    FOREIGN KEY (user_id)    REFERENCES admin_users."user"(id)          ON DELETE CASCADE;
ALTER TABLE admin_users."user-project"
    ADD CONSTRAINT fk_userproject_project FOREIGN KEY (project_id) REFERENCES platform_project.project(id)     ON DELETE CASCADE;
ALTER TABLE platform_project.device
    ADD CONSTRAINT fk_device_devicetype   FOREIGN KEY (device_type_id) REFERENCES platform_project.device_type(id) ON DELETE CASCADE;
ALTER TABLE platform_project.device_type
    ADD CONSTRAINT fk_devicetype_project  FOREIGN KEY (project_id) REFERENCES platform_project.project(id)     ON DELETE CASCADE;
ALTER TABLE platform_project.download
    ADD CONSTRAINT fk_download_device     FOREIGN KEY (device_id)  REFERENCES platform_project.device(id);
