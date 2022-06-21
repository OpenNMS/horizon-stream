# After testing, version 18 and 17.0.1 works on M1 MacBook, for more information https://github.com/keycloak/keycloak/issues/8825
FROM quay.io/keycloak/keycloak:18.0.0

USER 0

RUN microdnf install tar # allows for Skaffold file sync

USER 1000

WORKDIR /opt/keycloak

COPY --chown=1000 themes themes
