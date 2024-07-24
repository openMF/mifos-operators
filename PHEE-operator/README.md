# Local Setup
 
Prerequisite
- Kubernetes cluster setup
- maven, jdk, kubectl and docker installed

**Install the maven dependencies**
`mvn clean install`

**Creating local docker image using maven jib**
`mvn compile jib:dockerBuild -Dimage=ph-ee-importer-rdbms-operator:latest`

**Save the Docker image to a tar file**
`docker save ph-ee-importer-rdbms-operator:latest -o ph-ee-importer-rdbms-operator.tar`

**Load the tar file into k3s**
`sudo k3s ctr images import ph-ee-importer-rdbms-operator.tar`

**Apply CRD, Operator and then Custom Resource**
`kubectl apply -f deploy/crds/ph-ee-importer-rdbms-crd.yaml`
`kubectl apply -f deploy/operator-deployment.yaml`
`kubectl apply -f deploy/ph-ee-importer-rdbms-cr.yaml`


## Note
This file is still in progress will be updated as the project progresses.
