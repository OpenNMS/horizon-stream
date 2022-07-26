#!/bin/bash

cd ../grafana/

docker build -t opennms/horizon-stream-grafana:local -f ./Dockerfile . 2> ../local-sample/tmp/HS_GRAFANA.log

echo 1 > ../local-sample/tmp/HS_GRAFANA
