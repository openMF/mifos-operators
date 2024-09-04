# PHEE Operator Local Setup

This guide provides instructions to set up the PHEE Operator in a k3s cluster using a provided script. The script simplifies the process of deploying, cleaning up, and updating the operator and its resources.

### Note
This is a Kubernetes (K8s) Operator setup built on top of the [Mifos-Gazelle script](https://github.com/openMF/mifos-gazelle). The operator is currently configured to deploy twelve deployments (with their ingress and service if needed) under the paymenthub deployment. The repository is actively being developed and tested to support more Mifos artifacts using the operator.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Setup and Deployment](#setup-and-deployment)
  - [Using the Automated Script](#using-the-automated-script) 
  - [Manual Setup](#Using-the-Manual-Setup) 
- [Usage Information](#usage-information)
  - [Note](#note-1)


## Prerequisites

- Mifos-Gazelle script environment setup [here](https://github.com/openMF/mifos-gazelle).
- Maven, JDK, kubectl, and Docker installed. The script covers the installation of these, but you may still want to check out the official documentation if needed.
- Operator script file (`deploy-operator.sh`).

## Setup and Deployment

You can set up and manage the PHEE Operator using either the provided script for an automated approach or manual steps for more control.

### Using the Automated Script

The `deploy-operator.sh` script supports various modes of operation:

- `-m Flag`: Specifies the mode, such as `deploy` or `cleanup`.
- `-u Flag`: Specifies the update mode, such as updating the CR or the operator deployment.

#### 1. Deploy the Operator

To build, deploy, and verify the operator and its required deployments:

```
./deploy-operator.sh -m deploy
```

#### 2. Clean Up the Operator

To remove the operator and related resources from the k3s cluster:

```
./deploy-operator.sh -m cleanup
```

#### 3. Update the Custom Resource (CR)

If you need to apply updates to the Custom Resource (CR):

```
./deploy-operator.sh -u cr
``` 

#### 4. Update the Operator Deployment

To apply updates to the operator deployment:

```
./deploy-operator.sh -u operator
```

### Using the Manual Setup

If you prefer to manually set up the operator without using the script, follow these steps:

#### 1. Install Maven Dependencies
```
mvn clean install
```

#### 2. Create a Local Docker Image Using Maven Jib
```
mvn compile jib:dockerBuild -Dimage=ph-ee-operator:latest
```

#### 3. Save the Docker Image to a Tar File
```
docker save ph-ee-operator:latest -o ph-ee-operator.tar
```

#### 4. Load the Tar File into k3s
```
sudo k3s ctr images import ph-ee-operator.tar
```

#### 5. Apply CRD, Operator, and Custom Resource
```
kubectl apply -f deploy/crds/ph-ee-CustomResourceDefinition.yaml
kubectl apply -f deploy/operator/operator_deployment_manifests.yaml
kubectl apply -f deploy/cr/ph-ee-CustomResource.yaml
```

## Usage Information

- Ensure the script is executable. If not, run `chmod +x deploy-operator.sh` to make it executable.
- The script should be run from the directory where it is located and the operator repo should be in the same directory as `mifos-gazelle`.
- The `deploy` mode will upgrade the Helm chart, build the Docker image, deploy the operator with its CRD and CR, and verify its status in the k3s cluster.
- The `cleanup` mode will remove the operator and all its related resources, allowing for a fresh setup if needed.
- The `CR` and `operator` update modes allow you to apply updates specifically to the CR or the operator deployment, respectively, without a full redeployment.

## Note
- The repository is actively being developed and tested to support more Mifos artifacts using the operator.
- The operator is currently configured to deploy twelve specific deployments only.
- This documentation will be updated as the project progresses.
