FROM postgres:16-alpine

# Copy initialization script to create schemas
COPY ./init.sql /docker-entrypoint-initdb.d/
