#!/bin/bash

cd ../keycloak-ui/

docker build -t opennms/horizon-stream-keycloak:local -f ./Dockerfile . 2> ../local-sample/tmp/HS_KEYCLOAK_UI.log

echo 1 > ../local-sample/tmp/HS_KEYCLOAK_UI
