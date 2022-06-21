#!/bin/bash

printf "\n# Init\n"
printf "################################################################################\n\n"

printf "\n# Clear and remake tmp/ dir"
# Contains files not to be committed to github.

rm -r tmp/
mkdir tmp/

printf "\n# Pull in env vars.\n"

source config-run

echo "$VERSION_KEY_CLOAK"
echo "$DOMAIN_CORE"
echo "$DOMAIN_KARAF"
echo "$DOMAIN_UI"
echo "$DOMAIN_KEYCLOAK"
echo "$DOMAIN_API"

printf "\n# Create Kind cluster\n"
printf "################################################################################\n\n"

kind create cluster --config=config-kind.yaml

printf "\n\n# Confirm connection\n"

kubectl config use-context kind-kind
kubectl config get-contexts

printf "\n# Add Dependencies\n"
printf "################################################################################\n\n"

# Add Dependency - Keycloak
kubectl apply -f https://raw.githubusercontent.com/keycloak/keycloak-k8s-resources/$VERSION_KEY_CLOAK/kubernetes/keycloaks.k8s.keycloak.org-v1.yml
kubectl apply -f https://raw.githubusercontent.com/keycloak/keycloak-k8s-resources/$VERSION_KEY_CLOAK/kubernetes/keycloakrealmimports.k8s.keycloak.org-v1.yml
kubectl apply -f https://raw.githubusercontent.com/keycloak/keycloak-k8s-resources/$VERSION_KEY_CLOAK/kubernetes/kubernetes.yml

# Verify (should get keycloaks and keycloakrealmiiimports)
kubectl api-resources | grep keycloak

# Add Dependency - Ingress Nginx
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

printf "\n\n# Update and Deploy yaml files\n"
printf "################################################################################\n\n"

# TODO: Remove the /.m2 persistent volume and volume claim from the manifest
# yaml file. This is for skaffold and will cause this to fail.
# Just remove the following volume mount:
#            - name: maven-repo-volume
#              mountPath: "/opt/horizon-stream/.m2"
#              readOnly: true

printf "# Update yaml files\n"
cat ../dev/kubernetes.kafka.yaml | \
  sed "s/opennms\/horizon-stream-core/opennms\/horizon-stream-core\:$IMAGE_TAG/" | \
  sed "s/opennms\/horizon-stream-api/opennms\/horizon-stream-rest-server\:$IMAGE_TAG/" | \
  sed "s/opennms\/horizon-stream-ui-dev/opennms\/horizon-stream-ui\:$IMAGE_TAG/" | \
  sed "s/frontendUrl: \"http:\/\/localhost:28080\"/frontendUrl: \"http:\/\/$DOMAIN_KEYCLOAK\"/" | \
  sed "s/localhost:28080/$DOMAIN_KEYCLOAK/" | \
  sed "s/localhost:9090/$DOMAIN_API/" | \
  sed "s/imagePullPolicy: Never/imagePullPolicy: Always/" > tmp/hs.yaml

cat ingress.TEMPLATE.yaml | \
  sed "s/\[\[LOCALHOSTUI\]\]/$DOMAIN_UI/" | \
  sed "s/\[\[LOCALHOSTKEYCLOAK\]\]/$DOMAIN_KEYCLOAK/" | \
  sed "s/\[\[LOCALHOSTKARAF\]\]/$DOMAIN_KARAF/" | \
  sed "s/\[\[LOCALHOSTCORE\]\]/$DOMAIN_CORE/" | \
  sed "s/\[\[LOCALHOSTAPI\]\]/$DOMAIN_API/" > tmp/ingress.yaml

printf "\n\n# Deploy yaml files\n"

kubectl apply -f tmp/hs.yaml

# This is only for CI-CD pipeline
if [[ $CI_CD_RUN == true ]]; then
printf "\n\n# Import Images for Testing \n"
kind load docker-image opennms/horizon-stream-ui:local
kind load docker-image opennms/horizon-stream-core:local
kind load docker-image opennms/horizon-stream-rest-server:local
kubectl patch deployments my-horizon-stream-ui -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-ui", "imagePullPolicy":"Never"}]}}}}'
kubectl patch deployments my-horizon-stream-core -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-core", "imagePullPolicy":"Never"}]}}}}'
kubectl patch deployments my-horizon-stream-api -p '{"spec": {"template": {"spec":{"containers":[{"name": "horizon-stream-api", "imagePullPolicy":"Never"}]}}}}'
fi

printf "\nWaiting for startup of pods, could take a few minutes\n"

# Let creation of pods
sleep 30

# Wait until services are running.
kubectl wait --for=condition=ready pod --timeout=600s -l run=my-horizon-stream-core
kubectl wait --for=condition=ready pod --timeout=600s -l run=my-horizon-stream-api
kubectl wait --for=condition=ready pod --timeout=600s -l run=my-kafka
kubectl wait --for=condition=ready pod --timeout=600s -l run=my-postgres
kubectl wait --for=condition=ready pod --timeout=600s -l run=my-keycloak
kubectl wait --for=condition=ready pod --timeout=600s -l run=my-zookeeper
kubectl wait --for=condition=ready pod --timeout=600s -l app=my-horizon-stream-ui
kubectl wait --for=condition=ready pod --timeout=600s -l app.kubernetes.io/name=keycloak-operator
kubectl -n ingress-nginx wait --for=condition=ready pod --timeout=60s -l app.kubernetes.io/component=controller
 
# Wait...
sleep 60

kubectl apply -f services.yaml
kubectl apply -f tmp/ingress.yaml

# Wait...
sleep 60
 
printf "\n\n# Create user through keycloak.\n"
printf "################################################################################\n\n"

cd ../tools/
./KC.login -H http://localhostkey/keycloak -u user001 -p passw0rd -R opennms
./events.list -H http://$DOMAIN_CORE -t "$(< data/ACCESS_TOKEN.txt)"
./events.publish -H http://$DOMAIN_CORE -t "$(< data/ACCESS_TOKEN.txt)"
./events.list -H http://$DOMAIN_CORE -t "$(< data/ACCESS_TOKEN.txt)"

printf "\n\n# Output\n"
printf "################################################################################\n\n"

printf "\n\nDone\n\nGo to http://$DOMAIN_UI\n\n"
