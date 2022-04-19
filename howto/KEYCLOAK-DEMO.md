# Demo Application with Keycloak Security

## Overview

Here is the list of servers that are started, with their host ports listed:

* Keycloak: port 9000 (HTTP), port 9443 (HTTPS)
* PostgreSQL: port 5432
* Zookeeper: port 2181
* Zafka: port 9092
* Horizon Stream: port 8181 (HTTP), port 8101 (SSH)

## Pre-Requisites

* Java 11+
* Docker
* jq

## Steps

    $ docker container create \
            --name opennms-keycloak \
            -p 9000:8080 \
            -p 9443:8443 \
            -e KEYCLOAK_CREATE_ADMIN_USER=true \
            -e KEYCLOAK_ADMIN=keycloak-admin \
            -e KEYCLOAK_ADMIN_PASSWORD=admin \
            quay.io/keycloak/keycloak:17.0.0 \
            start-dev

    $ docker start opennms-keycloak

    $ docker container create --name opennms-postgres -p 5432:5432 -e "POSTGRES_HOST_AUTH_METHOD=trust" postgres:13.3-alpine
    $ docker start opennms-postgres

    $ docker container create --name opennms-zookeeper -p 2181:2181 -e ALLOW_ANONYMOUS_LOGIN=yes docker.io/bitnami/zookeeper:3.7
    $ docker container start opennms-zookeeper

    $ docker container create \
            --name opennms-kafka \
            -e KAFKA_BROKER_ID=1 \
            -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092 \
            -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://172.17.0.1:9092 \
            -e KAFKA_CFG_ZOOKEEPER_CONNECT=172.17.0.1:2181 \
            -e ALLOW_PLAINTEXT_LISTENER=yes \
            -e KAFKA_HOSTNAME=localhost \
            -e KAFKA_ADVERTISED_HOST_NAME=172.17.0.1 \
            -p 9092:9092 \
            docker.io/bitnami/kafka:3
    $ docker start opennms-kafka

    $ docker container create \
            --name opennms-horizon-stream \
            -p 8181:8181 \
            -p 8101:8101 \
            -e PGSQL_SERVICE_NAME=172.17.0.1 \
            -e PGSQL_ADMIN_USERNAME=postgres \
            -e PGSQL_ADMIN_PASSWORD=unused \
            -e KAFKA_BROKER_HOST=172.17.0.1 \
            -e KAFKA_BROKER_PORT=9092 \
            -e ACTIVEMQ_BROKER_URL="failover://(tcp://172.17.0.1:61616)" \
            -e KEYCLOAK_BASE_URL="http://172.17.0.1:9000" \
            -e KEYCLOAK_ADMIN_USERNAME="keycloak-admin" \
            -e KEYCLOAK_ADMIN_PASSWORD="admin" \
            -e JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Dkeycloak.base-url=http://172.17.0.1:9000" \
            opennms/horizon-stream-core:local
    $ docker start opennms-horizon-stream

    Starting from the project root:
    $ cd tools
    $ ./KC.login -u keycloak-admin -p admin -R master
    $ ./KC.add-realm -t "$(< data/ACCESS_TOKEN.txt)" -R opennms
    $ ./KC.create-user -t "$(< data/ACCESS_TOKEN.txt)" -u user001 -p passw0rd -R opennms
    $ ./KC.get-user-by-username -t "$(< data/ACCESS_TOKEN.txt)" -u user001 -R opennms | tee user.out
    $ jq -r '.[] | .id' user.out  | tee user-id.txt
    $ ./KC.create-role -t "$(< data/ACCESS_TOKEN.txt)" -R opennms -r admin
    $ ./KC.get-realm-roles -t "$(< data/ACCESS_TOKEN.txt)" -R opennms | tee role.out
    $ jq -r '.[] | select(.["name"] == "admin") | .id' role.out | tee role-id.txt
    $ ./KC.assign-user-role -r admin -i "$(< role-id.txt)" -t "$(< data/ACCESS_TOKEN.txt)" -U "$(< user-id.txt)" -R opennms
    $ ./KC.login -u user001 -p passw0rd -R opennms
    $ ./events.list -t "$(< data/ACCESS_TOKEN.txt)"
