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

package image

import (
	"context"
	"github.com/OpenNMS/opennms-operator/api/v1alpha1"
	"github.com/go-co-op/gocron"
	"github.com/go-logr/logr"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/fields"
	"k8s.io/apimachinery/pkg/labels"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"time"
)

const (
	DefaultFrequency = 60
)

type ImageUpdater struct {
	client.Client
	Log              logr.Logger
	Frequency        int
	RunningInstances map[string][]client.Object
	Scheduler        *gocron.Scheduler
}

func NewImageUpdater(k8sClient client.Client, freq int) ImageUpdater {
	if freq == 0 {
		freq = DefaultFrequency
	}
	return ImageUpdater{
		Client:           k8sClient,
		Log:              ctrl.Log.WithName("imageCheck").WithName("OpenNMS"),
		Frequency:        freq,
		RunningInstances: map[string][]client.Object{},
	}
}

func (iu *ImageUpdater) InitScheduler() {
	iu.Scheduler = gocron.NewScheduler(time.UTC)
	iu.Scheduler.SetMaxConcurrentJobs(1, gocron.WaitMode)
	iu.Scheduler.StartAsync()
	iu.Scheduler.Every(iu.Frequency).Minute().Do(iu.checkAll)
	//TODO capture job returned by Do() and stop it gracefully when the operator shuts down
}

//StartImageCheckerForInstance - start the recurrent image check for the given instance and services
func (iu *ImageUpdater) StartImageCheckerForInstance(instance v1alpha1.OpenNMS, services []client.Object) {
	if iu.Scheduler == nil { //lazy scheduler start
		iu.InitScheduler()
	}
	iu.RunningInstances[instance.Name] = services
}

//StopImageCheckerForInstance - stop the recurrent image check for the given instance and services
func (iu *ImageUpdater) StopImageCheckerForInstance(instance v1alpha1.OpenNMS) {
	delete(iu.RunningInstances, instance.Name)
}

//ImageCheckerForInstanceRunning - check if an image check is running for the given instance
func (iu *ImageUpdater) ImageCheckerForInstanceRunning(instance v1alpha1.OpenNMS) bool {
	_, ok := iu.RunningInstances[instance.Name]
	return ok
}

//ServiceMarkedForImageCheck - check if a given service is marked for auto updating
func (iu *ImageUpdater) ServiceMarkedForImageCheck(service client.Object) bool {
	if autoupdate, ok := service.GetAnnotations()["autoupdate"]; ok {
		if autoupdate == "true" {
			return true
		}
	}
	return false
}

//checkAll - check all registered instances for image updates
func (iu *ImageUpdater) checkAll() {
	for instance, services := range iu.RunningInstances {
		//instanceUpdate := false
		for _, service := range services {
			ctx := context.Background()
			imageName, currentId := iu.getImageForService(ctx, instance, service)
			latestId, err := iu.getLatestImageDigest(ctx, imageName, currentId)
			if err != nil { //if there's an error getting the image digest, skip this service
				continue
			}
			iu.markInstanceServiceForUpdate(ctx, instance, service.GetName(), currentId, latestId)
		}
	}
}

//getImageForService - get the Image and ImageID for a given service in a given instance
func (iu *ImageUpdater) getImageForService(ctx context.Context, instanceName string, service client.Object) (string, string) {
	pod := iu.getPodFromCluster(ctx, instanceName, service)
	if pod == nil {
		return "", ""
	}
	//should only be one running container
	return pod.Status.ContainerStatuses[0].Image, pod.Status.ContainerStatuses[0].ImageID
}

//getPodFromCluster - get the first running pod for the given service from the given instance namespace
func (iu *ImageUpdater) getPodFromCluster(ctx context.Context, instanceName string, service client.Object) *corev1.Pod {
	serviceName := service.GetLabels()["app.kubernetes.io/name"]
	labelMap := map[string]string{
		"app.kubernetes.io/name": serviceName,
	}
	labelsSelector := labels.SelectorFromSet(labelMap)

	fieldSelector := fields.OneTermEqualSelector("status.phase", "Running")

	var pods corev1.PodList
	err := iu.Client.List(ctx, &pods, &client.ListOptions{LabelSelector: labelsSelector, FieldSelector: fieldSelector, Namespace: instanceName})
	if err != nil {
		iu.Log.Error(err, "could not get pod from cluster", "namespace", instanceName, "service", serviceName)
	}
	if len(pods.Items) == 0 {
		return nil
	}
	//only return the first pod of the list - all replicas of a service should be running the same image
	return &pods.Items[0]
}
