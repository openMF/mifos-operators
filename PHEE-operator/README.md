# Local Setup

This guide provides steps to set up the PHEE Operator in a k3s cluster. The current setup does not contain a Dockerfile. We use the Maven Jib plugin to build a Docker image, save that image to a .tar file, load it into the k3s cluster, and then apply the CRD, Operator, and CR.

## Prerequisites

- k3s cluster setup
- Maven, JDK, kubectl, and Docker installed

## Steps

### 1. Install the Maven dependencies

```
mvn clean install
```

### 2. Creating local docker image using maven jib 

```
mvn compile jib:dockerBuild -Dimage=ph-ee-importer-rdbms-operator:latest
```

### 3. Save the Docker image to a tar file

```
docker save ph-ee-importer-rdbms-operator:latest -o ph-ee-importer-rdbms-operator.tar
```

### 4. Load the tar file into k3s

```
sudo k3s ctr images import ph-ee-importer-rdbms-operator.tar
```

### 5. Apply CRD, Operator and Custom Resource

```
kubectl apply -f deploy/crds/ph-ee-importer-rdbms-crd.yaml
kubectl apply -f deploy/operator/operator.yaml
kubectl apply -f deploy/cr/ph-ee-importer-rdbms-cr.yaml
```

### Note
This file is still in progress will be updated as the project progresses.
Also, currently operator is configured for deploying importer-rdbms only
