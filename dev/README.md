This dir is for building local dev envs.

Pre-requisites:
* Install Kind. Mac, there is brew install. 
* ```$ run.sh``` - Builds some components in dir.
* Make sure Docker is running. 
* Install Skaffold: https://skaffold.dev/docs/install/

Process:
1. ``` kind create cluster``` 
    * There are options to create multiple clusters with different names and switch between them. 
2. Confirm connection to cluster:
    * ``` kubectl config get-contexts```
    * ``` kubectl get all```
2. ``` git clone https://github.com/GoogleContainerTools/skaffold.git tmp/skaffold```
3. ``` cd tmp/skaffold/examples/dev-journey-buildpacks/``` 
4. ``` skaffold dev --port-forward``` 
5. Go to http://localhost:8080/ in web browser. 
6. Open another tab in terminal to perform the following steps. 
7. ``` vi src/main/java/hello/HelloController.java```  
    * Make a change to the text. And wait until the skaffold dev tab has finished uploading change.
8. Go to http://localhost:8080/ in web browser. 
9. Ctrl-C to finish and cleanup.
