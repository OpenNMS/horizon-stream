#!/bin/bash

printf "\n# Init\n"
printf "################################################################################\n\n"

printf "\n# Clear and remake tmp/ dir"
# Contains files not to be committed to github.

rm -r tmp/
mkdir tmp/

printf "\n# Pull in env vars.\n"

source ./local-sample/config-run

echo "Domain: $DOMAIN"

printf "\n# Create Kind cluster\n"
printf "################################################################################\n\n"

kind create cluster --config=./local-sample/config-kind.yaml

printf "\n\n# Confirm connection\n"

kubectl config use-context kind-kind
kubectl config get-contexts

printf "\n# Add Dependencies\n"
printf "################################################################################\n\n"

# Add Dependency - Ingress Nginx
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

# Wait for the ingress to get some items started.
sleep 60

kubectl -n ingress-nginx wait --for=condition=ready pod --timeout=60s -l app.kubernetes.io/component=controller

printf "\n\n# Install OpenNMS Operator\n"
printf "################################################################################\n\n"

cd ./operator/
bash scripts/install-local.sh
bash scripts/create-instance.sh
cd ../local-sample/

kubectl -n opennms wait --for=condition=ready pod --timeout=120s -l name=opennms-operator
kubectl -n opennms get all
kubectl get ns

# Wait for the operator to get some items started.
sleep 120

kubectl config get-contexts
kubectl get ns

printf "\n\n# Add TLS Secret\n"
printf "################################################################################\n\n"

openssl req -subj "/CN=$DOMAIN/O=Test Keycloak./C=US" -newkey rsa:2048 -nodes -keyout tmp/key.pem -x509 -days 365 -out tmp/certificate.pem
kubectl -n local-instance create secret tls tls-cert-wildcard --cert tmp/certificate.pem --key tmp/key.pem

printf "\n\n# Keycloak\n"
printf "################################################################################\n\n"

kubectl -n local-instance apply -f secrets.yaml
kubectl -n local-instance apply -f postgres.yaml

kubectl -n local-instance wait --for=condition=ready pod --timeout=600s -l app=postgresql-db

kubectl -n local-instance apply -f kc-deployment.yaml
kubectl -n local-instance apply -f kc-service.yaml
kubectl -n local-instance apply -f kc-ingress.yaml

printf "\n\n# Grafana\n"
printf "################################################################################\n\n"

# TODO: Publish this image.
#kind load docker-image opennms/horizon-stream-grafana:latest 

kubectl -n local-instance apply -f grafana-a-configmap.yaml
kubectl -n local-instance apply -f grafana-b-secrets.yaml
kubectl -n local-instance apply -f grafana-c-deployment.yaml
kubectl -n local-instance apply -f grafana-d-service.yaml
kubectl -n local-instance apply -f grafana-e-ingress.yaml

printf "\n\n# HS Ingress\n"
printf "################################################################################\n\n"

kubectl -n local-instance apply -f hs-ingress.yaml

printf "\n\n# Update Configs Based on Env\n"
printf "################################################################################\n\n"

# This is only for CI-CD pipeline
if [[ $CI_CD_RUN == true ]]; then
  
  # Remove this ingress, forces http->https, we do not have that ready yet for
  # automated testing in the ci-cd pipeline.
  kubectl -n local-instance delete deployment.apps/ingress-nginx-controller 

  printf "\n\n# Import Images for Testing \n"
  kind load docker-image opennms/horizon-stream-grafana:local
  kind load docker-image opennms/horizon-stream-ui:local
  kind load docker-image opennms/horizon-stream-keycloak:local 
  kind load docker-image opennms/horizon-stream-core:local
  kind load docker-image opennms/horizon-stream-rest-server:local

  sleep 120
  
  kubectl -n local-instance patch deployments opennms-ui   -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-ui",  "image":"opennms/horizon-stream-ui:local"}]}}}}'
  kubectl -n local-instance patch deployments opennms-core -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-core","image":"opennms/horizon-stream-core:local"}]}}}}'
  kubectl -n local-instance patch deployments opennms-rest-server  -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-api", "image":"opennms/horizon-stream-rest-server:local"}]}}}}'
  kubectl -n local-instance patch deployments keycloak     -p '{"spec": {"template": {"spec":{"containers":[{"name": "keycloak",           "image":"opennms/horizon-stream-keycloak:local"}]}}}}'
  kubectl -n local-instance patch deployments grafana      -p '{"spec": {"template": {"spec":{"containers":[{"name": "grafana",           "image":"opennms/horizon-stream-grafana:local"}]}}}}'
  
  kubectl -n local-instance patch deployments opennms-ui   -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-ui", "imagePullPolicy":"Never"}]}}}}'
  kubectl -n local-instance patch deployments opennms-core -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-core","imagePullPolicy":"Never"}]}}}}'
  kubectl -n local-instance patch deployments opennms-rest-server  -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-api", "imagePullPolicy":"Never"}]}}}}'
  kubectl -n local-instance patch deployments keycloak     -p '{"spec": {"template": {"spec":{"containers":[{"name": "keycloak",           "imagePullPolicy":"Never"}]}}}}'
  kubectl -n local-instance patch deployments grafana      -p '{"spec": {"template": {"spec":{"containers":[{"name": "grafana",           "imagePullPolicy":"Never"}]}}}}'

  kubectl -n local-instance patch deployments opennms-ui -p "{\"spec\": {\"template\": {\"spec\":{\"containers\":[{\"name\": \"opennms-ui\", \"env\":[{\"name\": \"DOMAIN_KEYCLOAK\", \"value\":\"https://$DOMAIN/auth\"}]}]}}}}"

  sleep 120

  # The ingress is not working, just use the default installed one.
  # TODO: This fails if the opennms operator has not created this object yet.
  kubectl -n local-instance patch ingress opennms-ingress -p '{"spec":{"ingressClassName":"nginx"}}'

else

  # TODO: Publish this image.
  kind load docker-image opennms/horizon-stream-keycloak:latest 
  kubectl -n local-instance patch deployments keycloak -p '{"spec": {"template": {"spec":{"containers":[{"name": "keycloak", "imagePullPolicy":"Never"}]}}}}'
  
  kind load docker-image opennms/horizon-stream-grafana:latest
  kubectl -n local-instance patch deployments grafana -p '{"spec": {"template": {"spec":{"containers":[{"name": "grafana", "imagePullPolicy":"Never"}]}}}}'

  #kubectl -n local-instance wait --for=condition=ready pod --timeout=120s -l app=opennms-ui
  sleep 120

  echo # IMPORTANT: If the following do not catch, then update them manually until we can get these integrated.
  # TODO: Publish this image.
  kind load docker-image opennms/horizon-stream-ui:0.0.14
  kubectl -n local-instance patch deployments opennms-ui -p '{"spec": {"template": {"spec":{"containers":[{"name": "opennms-ui", "image":"opennms/horizon-stream-ui:0.0.14"}]}}}}'
  kubectl -n local-instance patch deployments opennms-ui -p '{"spec": {"template": {"spec":{"containers":[{"name": "opennms-ui", "imagePullPolicy":"Never"}]}}}}'
  kubectl -n local-instance patch deployments opennms-ui -p "{\"spec\": {\"template\": {\"spec\":{\"containers\":[{\"name\": \"opennms-ui\", \"env\":[{\"name\": \"DOMAIN_KEYCLOAK\", \"value\":\"https://$DOMAIN/auth\"}]}]}}}}"

  sleep 120

  # The ingress is not working, just use the default installed one.
  # TODO: This fails if the opennms operator has not created this object yet.
  kubectl -n local-instance patch ingress opennms-ingress -p '{"spec":{"ingressClassName":"nginx"}}'

fi

printf "\n\n# Create realm, user, and role mappings through keycloak api.\n"
printf "################################################################################\n\n"

./local-sample/run.kc-config.sh

printf "\n\n# Output\n"
printf "################################################################################\n\n"

printf "\n\nDone\n\nGo to https://$DOMAIN\n\n"

printf "\n\n# TODO\n"
printf "################################################################################\n\n"

printf "\nUpdate the ingress from operator to use the nginx className.\n"
