
def init():
  return "sh -c ./local-sample/run.sh"

k8s_context() 

init_complete = os.getenv( "INIT_COMPLETE" , default = "FALSE" ) 

if init_complete == "FALSE":
  os.putenv( "INIT_COMPLETE" , "TRUE" ) 
  local(init(), quiet=False)
  k8s_yaml('./operator/local-instance.yaml')

if init_complete == "TRUE":
  print("Test var: ", init_complete) 

# See for more info https://docs.tilt.dev/api.html#api.k8s_kind
# This is to do with the resource kind, not the Kind cluster manager. This
# updates the image field on the opennms crd for the api resource, will have to
# add one for each resource (ui, core, etc). May be better to just update the
# image directly. 
# TODO: The operator has a bug where it does not update yet the deployments and
# redeploys when this is changed, but it should and will be implemented when
# the fix is in place (FIXED, merged into develop, need to test). See the change below.
#k8s_yaml('./operator/local-instance.yaml')
#docker_build('...', '.') # Need to run this first. Instead, use custom_build().
#k8s_kind('opennms', image_json_path='{.spec.api.image}')
# Just apply the change directly to the 

# For importing images into kind cluster, see
# https://github.com/tilt-dev/kind-local for optimizing this process.

# Note: For testing the
# https://github.com/tilt-dev/tilt-example-java/tree/master/101-jib, I had to
# update the record-start-time.sh with the following:
#    
#    # Needed to install 'brew install coreutils' on Mac. And change the date to 
#    # gdate, had to remove the leading zero on startTimeNanos.
#    cat src/main/java/dev/tilt/example/IndexController.java | \
#        sed -e "s/startTimeSecs = .*;/startTimeSecs = $(gdate +%-s);/" | \
#        sed -e "s/startTimeNanos = .*;/startTimeNanos = $(gdate +%-N);/" > \
#        $tmpfile

# This temporary until the opennms operator can automatically update the
# deployment from a CRD change.
local("kubectl -n opennms rollout restart deployment.apps/opennms-operator", quiet=False)

# Go example:
#docker_build('example-go-image', '.', dockerfile='deployments/Dockerfile')
#k8s_yaml('deployments/kubernetes.yaml')
# TODO: Need a way to update the image directly possibly.

# Should not need this, seeing that we have ingresses installed. They update
# automatically.
#k8s_resource('example-go', port_forwards=8000)

