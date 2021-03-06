/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package handlers

import (
	"github.com/OpenNMS/opennms-operator/internal/model/values"
	"github.com/OpenNMS/opennms-operator/internal/util/yaml"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type StunnelHandler struct {
	ServiceHandlerObject
}

func (h *StunnelHandler) ProvideConfig(values values.TemplateValues) []client.Object {
	var configMap corev1.ConfigMap
	var service corev1.Service
	var deployment appsv1.Deployment

	yaml.LoadYaml(filepath("stunnel/stunnel-configmap.yaml"), values, &configMap)
	yaml.LoadYaml(filepath("stunnel/stunnel-service.yaml"), values, &service)
	yaml.LoadYaml(filepath("stunnel/stunnel-deployment.yaml"), values, &deployment)

	h.Config = []client.Object{
		&configMap,
		&service,
		&deployment,
	}

	return h.Config
}
