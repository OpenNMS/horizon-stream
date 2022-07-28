# Requirements

Install the following if not installed:
* Kind
* Kubectl
* Operator-sdk (https://sdk.operatorframework.io/docs/installation/)
* Helm3

Running on Mac or Linux.

# Process

IMPORTANT: Run from repo root dir.

```
sudo vi /etc/hosts
```
Add with the following to /etc/hosts:
```
127.0.0.1 onmshs
```

Change the dns to the above dns entries in config-run to match the /etc/hosts file: ```vi config-run```

Run the following: ```./local-sample/run.sh local```. Takes a while.

Confirm that ingresses have been updated (pending issue), there should be 5 ingresses: ```kubectl get ingress```

Go to https://onmshs in the web browser. Login with user ```admin``` with pw ```admin```.

# Cleanup

```
# Delete cluster.
kind delete clusters kind

# Confirm all port-forwarding background processes are killed.
ps -axf | grep kubectl
```

# Testing Images Not Published But Built Locally

Run the following: ```./local-sample/run.sh dev```. Takes a while.


