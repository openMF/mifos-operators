# PHEE Operator Local Setup

The PHEE Operator is designed to manage deployments under the PaymentHub project, automating resource creation, updates, and management. The operator supports creating deployments, RBACs, Ingress, and Services.

### Note

This is a Kubernetes (K8s) Operator setup built on top of the [Mifos-Gazelle script](https://github.com/openMF/mifos-gazelle). The operator is configured to deploy paymenthub deployments (with their ingress and service if needed).

### Links to Other Docs

- Refer to this documentation for a quick overview of the architecture and repository structure: [Architecture Overview](ARCHITECTURE.md)
- Refer to this documentation for an in-depth understanding of each file's purpose: [Developer Guide](DEVELOPER_GUIDE.md)

## Table of Contents

- [Prerequisites](#prerequisites)
- [Setup and Deployment](#setup-and-deployment)
  - [Using the Automated Script](#using-the-automated-script)
  - [Using the Manual Setup](#using-the-manual-setup)
- [How to Edit Deployments](#how-to-edit-deployments)
- [How to Add New Configurations to Deployments](#how-to-add-new-configurations-to-deployments)
- [Note](#note)

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

To apply updates to the Custom Resource (CR):

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
mvn clean package
```

#### 2. Create a Local Docker Image Using Maven Jib

```
docker build -t ph-ee-operator:latest .
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
 

## How to Edit Deployments

To make configuration changes to the deployments managed by the operator, you need to modify the values in the Custom Resource (CR) and reapply it. You can either use the provided script or apply the changes directly using `kubectl`.

To reapply the CR using the script, run:

`./deploy-operator.sh -u cr`

Alternatively, apply changes directly using `kubectl`:

`kubectl apply -f deploy/cr/ph-ee-CustomResource.yaml`

For example, if you want to change the resource requests and limits of a deployment, simply edit the corresponding values in the CR for resource limits and requests, then reapply the CR using one of the above methods.

Note: 
Some configuration values that are not used by many deployments are hardcoded in the operator's code. If you need to change these hardcoded values, you must modify the operator code itself and redeploy the operator.
  - RBACs related configurations cannot be changed using Custom Resource (CR), only enable/disable flag present.
  - configmap path for volmount is hardcoded, and configmap itself has hardcoded values.
  - initcontainer (waith-db) related configurations are also hardcoded.
  

## How to Add New Configurations to Deployments

If you want to add new configurations to the deployment, follow these steps:

1. **Update the CRD**: First, add a section or definition for the new configuration in the Custom Resource Definition (CRD). This will ensure that the new field is recognized as part of the CR schema.

2. **Create Getters and Setters**: In the corresponding `Spec` file for the Custom Resource (`PaymentHubDeploymentSpec.java`), create a class and define getters and setters for the new configuration field. This will allow the operator to access the value from the CR.

3. **Modify Operator Logic**: Update the operator's logic to handle the new configuration. Use the getters to retrieve the values from the CR (an instance of the CRD) and integrate them into the necessary resource creation or reconciliation processes.

By following these steps, you can extend the functionality of the operator to manage additional configurations dynamically using CR.

## Note 

- Ensure the script is executable. If not, run `chmod +x deploy-operator.sh` to make it executable.
- The script should be run from the directory where it is located and the operator repo should be in the same directory as `mifos-gazelle`.
- The `deploy` mode will upgrade the Helm chart, build the Docker image, deploy the operator with its CRD and CR, and verify its status in the k3s cluster.
- The `cleanup` mode will remove the operator and all its related resources, allowing for a fresh setup if needed.
- The `CR` present in the repo is created and tested in 16Gb 4vcpus system, so the resource usage for deployments are set according to that.
- Probes have not been set up or added to the deployment configurations yet (the logic is present in the code but not currently used).