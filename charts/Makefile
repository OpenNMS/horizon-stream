.PHONY: package

all: package

package:
	helm package opennms-operator-dependencies -d ./packaged
	helm repo index --url https://opennms.github.io/horizon-stream/charts/packaged --merge packaged/index.yaml ./packaged