# DEVELOPER GUIDE

## Table of Contents

### How the Operator Works
1. [Key Components](#key-components)
   - [Custom Resource Definition (CRD) and Custom Resource (CR)](#custom-resource-definition-crd-and-custom-resource-cr) 
   - [Controller File](#controller-file) 
   - [Kind and Group](#kind-and-group) 

### Explanation of Files
1. [Deployment files](#Deployment-files)
   - [ph-ee-CustomResourceDefinition.yaml](#ph-ee-CustomResourceDefinitionyaml) 
   - [operator_deployment_manifests.yaml](#operator_deployment_manifestsyaml)
2. [Custom Resource Files](#custom-resource-files)
   - [PaymentHubDeployment.java File](#PaymentHubDeploymentjava-file)
   - [PaymentHubDeploymentSpec.java File](#PaymentHubDeploymentspecjava-file)
   - [PaymentHubDeploymentStatus.java File](#PaymentHubDeploymentStatusjava-file)
3. [SRC Files](#SRC-Files)
   - [OperatorMain.java File](#operatormainjava-file)
   - [PaymentHubDeploymentController.java File](#PaymentHubDeploymentcontrollerjava-file)
   - [Utility Classes](#utility-classes)
     - [DeletionUtil.java File](#deletionutiljava-file)
     - [DeploymentUtils.java File](#deploymentutilsjava-file)
     - [LoggingUtil.java File](#loggingutiljava-file)
     - [NetworkingUtils.java File](#networkingutilsjava-file)
     - [OwnerReferenceUtils.java File](#ownerreferenceutilsjava-file)
     - [RbacUtils.java File](#rbacutilsjava-file)
     - [ResourceUtils.java File](#resourceutilsjava-file)
     - [StatusUpdateUtil.java File](#statusupdateutiljava-file)
4. [deploy-operator.sh](#deploy-operatorsh)

# How the Operator Works

To start making changes to the PHEE Operator, it's crucial to understand several key components that define the architecture of the operator and how they interact. You can refer to the official documentation for further clarity, but here’s a brief explanation:

### Key Components

#### Custom Resource Definition (CRD) and Custom Resource (CR)

- **Custom Resource Definition (CRD):** 
  The CRD is a schema that defines the structure and validation rules for custom resources. It acts as a blueprint specifying how custom resources should be formatted and what fields they should include.

- **Custom Resource (CR):** 
  The CR is an instance of the CRD, representing a specific configuration of resources. Think of the CRD as a template or switchboard and the CR as the plug that fits into this switchboard. The CR must adhere to the schema defined by the CRD to ensure proper functionality. Essentially, the CR defines the desired state of resources that the operator should manage.

#### Controller File

- **Purpose:** 
  The controller file is a Kubernetes component that watches for changes to custom resources and ensures that the cluster's state matches the desired state specified by the CR. It uses the values defined in the CR to create, update, or delete resources as needed.

- **Function:** 
  The controller continuously monitors custom resources and triggers reconciliation processes to align the cluster's actual state with the desired state defined in the CR. If there are any changes in the CR, the controller invokes reconciliation methods to update the cluster accordingly.

#### Kind and Group

- **Kind:** 
  The `kind` defines the type of resource, and it plays a crucial role in linking the CRD, CR, and controller. The CRD defines a kind, which must be used in the CR to establish a connection between the CR and the CRD. The controller uses this kind to identify and manage the custom resource.

- **Group:** 
  The `group` categorizes resource types within different API versions. When the controller interacts with a CR, it checks the group and version specified in the CRD to ensure compatibility and perform appropriate API operations.


# Explanation of Files

## Deployment files

### ph-ee-CustomResourceDefinition.yaml 

Our CRD for the operator contains all the fields that our controller file might need to maintain the desired state of the cluster. It defines the structure and validation rules for the custom resources (CR), ensuring that the custom resources adhere to the specified format and contain all necessary information for the operator to function correctly.

#### Metadata

**Metadata** contains essential information about the CRD, such as its `name`, which identifies the CRD within the Kubernetes API. This `name` is formatted as `<plural>.<group>`, where `plural` is the plural form of the resource name and `group` specifies the API group. The `metadata` section helps Kubernetes identify and manage the CRD.

#### Spec

**Spec** defines the specifications and behavior of the custom resource. This includes:
- **Group**: The API group under which the CRD is categorized.
- **Names**: Specifies the `kind`, `listKind`, `plural`, `singular`, and optional `shortNames` for the custom resource.
- **Scope**: Indicates whether the CRD is `Namespaced` or `Cluster-wide`.
- **Versions**: Defines the versions of the CRD, including whether they are `served` and used for `storage`. It also specifies `subresources` like `status` and the `schema` for validation.

#### Schema (openAPIV3Schema)

**Schema (openAPIV3Schema)** outlines the structure and validation rules for the custom resource's specification. It includes the `spec` and its fields, providing a detailed structure for how the custom resources are defined and validated. This block is crucial for ensuring that the custom resources conform to the specified format.

- **Spec** fields:
  - `enabled`
  - `volMount`
  - `replicas`
  - `image`
  - `containerPort` 
  - `environment` 
  - `resources` 
  - `livenessProbe`
  - `readinessProbe`
  - `ingress`
  - `services`
  - `initContainerEnabled`
  - `rbacEnabled`
  - `secretEnabled`
  - `configMapEnabled`
  - `ingressEnabled`

#### Status

**Status** provides information about the state of the custom resource. It includes fields such as `availableReplicas`, `errorMessage`, `lastAppliedImage`, and `ready`. This section is used to track the current state and health of the resource, making it easier to monitor and manage its lifecycle.

### operator_deployment_manifests.yaml

This YAML file defines several Kubernetes resources essential for deploying and managing the PHEE Importer Operator. It starts with a `ServiceAccount`, which is used by the operator to interact with the Kubernetes API. The `Deployment` specifies how the operator should be deployed, including the Docker image to use, resource requests and limits, environment variables, and the service account to associate with it. The `ClusterRole` and `ClusterRoleBinding` provide the operator with the necessary permissions to access and manage various Kubernetes resources across the cluster. The `Role` and `RoleBinding` are used to grant specific permissions within the `default` namespace, ensuring the operator can manage resources like custom resources, their statuses, and associated roles. Overall, this file configures the operator's runtime environment, access controls, and permissions, ensuring it operates correctly and securely within the Kubernetes cluster. Two very important configurations to notice in this file are, the image name and the apigroups in `ClusterRole`

## SRC Files

### Custom Resource Files

#### PaymentHubDeployment.java File

This Java file defines the custom resource for `PaymentHubDeployment` in the Kubernetes ecosystem using the Fabric8 Kubernetes client. It extends the `CustomResource` class, which is part of the Fabric8 library, and implements the `Namespaced` interface to indicate that this custom resource is scoped to a namespace. The class is annotated with `@Version`, `@Group`, and `@Plural` to specify the API version, API group, and plural name of the custom resource, respectively. This setup allows the Kubernetes API to recognize and manage the `PaymentHubDeployment` resource, including its specification and status, as defined by the `PaymentHubDeploymentSpec` and `PaymentHubDeploymentStatus` classes. This file is crucial for enabling Kubernetes to handle the custom resource and its associated data effectively.

#### PaymentHubDeploymentSpec.java File

The `PaymentHubDeploymentSpec.java` file defines the specification for the `PaymentHubDeployment` custom resource in Kubernetes. It includes fields that detail the configuration and operational parameters of the custom resource, such as `enabled`, `volMount`, `replicas`, `image`, and `containerPort`. This class serves as the blueprint for how the custom resource should be structured and what information it should contain. It provides getters and setters for each field, ensuring that the specification can be easily managed and accessed. The significance of this file lies in its role in specifying the desired state and configuration for the custom resource, which the Kubernetes controller will use to manage and reconcile the resource's state within the cluster. This file is essential for translating the custom resource's desired state into a format that Kubernetes can understand and act upon.

#### PaymentHubDeploymentStatus.java File

The PaymentHubDeploymentStatus.java file represents the status of a PaymentHubDeployment custom resource in Kubernetes. It encapsulates information about the current state of the deployment, including the number of `availableReplicas`, any `errorMessage`, the `lastAppliedImage`, and whether the deployment is `ready`. This class provides a set of getter and setter methods to access and update these fields, allowing the status of the deployment to be tracked and modified. In addition to these methods, the file includes `toString()`, `equals()`, and `hashCode()` functions that facilitate object comparison and provide a string representation of the status. This is particularly useful for logging and debugging purposes, ensuring that the deployment status can be inspected and compared reliably.The significance of this file lies in its role in reflecting the real-time condition of the PaymentHubDeployment, enabling both developers and Kubernetes controllers to assess the current operational state of the custom resource and take necessary actions based on its status.

### OperatorMain.java File

The `OperatorMain.java` file serves as the entry point for the PHEE Importer Operator, initializing the Kubernetes client and registering the custom resource controller with the operator framework. It starts by setting up the Fabric8 Kubernetes client, which is used to interact with the Kubernetes API. The main method then registers the `PaymentHubDeploymentController` with the operator framework, associating it with the `PaymentHubDeployment` custom resource. This setup ensures that the controller is notified of any changes to the custom resource and can perform the necessary reconciliation actions. The `OperatorMain.java` file is crucial for bootstrapping the operator and ensuring that it is ready to manage the custom resource within the Kubernetes cluster. It handles the initial setup and configuration of the operator, making it the foundation for the operator's operation.

### PaymentHubDeploymentController.java File

The `PaymentHubDeploymentController.java` file is the core of the PHEE Importer Operator, responsible for watching the `PaymentHubDeployment` custom resource and reconciling its state within the Kubernetes cluster. The controller is registered with the operator framework in the `OperatorMain.java` file, which ensures that it is notified of any changes to the custom resource. The controller's main task is to reconcile the desired state specified in the custom resource with the actual state of the Kubernetes resources. It does this by creating, updating, or deleting resources such as Deployments, Services, Ingresses, and RBAC configurations based on the custom resource's specifications. The controller uses various utility classes to perform these actions, ensuring that all aspects of the custom resource are managed effectively. This file is the heart of the operator, driving the reconciliation process and ensuring that the Kubernetes cluster's state matches the desired state defined in the custom resource.
 
### Utility Classes

#### DeletionUtil.java File

The `DeletionUtil.java` file is a utility class designed for managing the deletion of Kubernetes resources associated with a custom resource of type `PaymentHubDeployment`. It provides methods to delete various Kubernetes resources such as Deployments, RBAC-related resources (ServiceAccounts, Roles, RoleBindings, ClusterRoles, and ClusterRoleBindings), Secrets, ConfigMaps, Ingress and Services. Each method is tailored to delete a specific type of resource based on the owner reference set by the custom resource, ensuring that resources created by the custom resource are properly cleaned up when the custom resource is deleted. The class uses the Fabric8 Kubernetes client to interact with the Kubernetes API and perform these deletion operations. This utility class is crucial for maintaining the integrity of the Kubernetes cluster by ensuring that no orphaned resources are left behind after a custom resource is deleted.

#### DeploymentUtils.java File

The `DeploymentUtils.java` file is a utility class that provides methods to create and manage Kubernetes `Deployment` resources for a custom resource of type `PaymentHubDeployment`. It includes methods to create a new `Deployment`, check if a `Deployment` already exists, and update an existing `Deployment`. The class uses the Fabric8 Kubernetes client to interact with the Kubernetes API and perform these operations. The `createDeployment` method, for example, takes the custom resource's specifications, such as image, replicas, and environment variables, and constructs a `Deployment` object that can be applied to the Kubernetes cluster. The utility also includes helper methods for setting up container specifications, resource requests and limits, liveness and readiness probes, and volume mounts. This class is essential for ensuring that the custom resource is properly deployed and managed within the Kubernetes cluster, providing the necessary logic to create, update, and maintain the `Deployment` resources associated with the custom resource.

#### LoggingUtil.java File

The `LoggingUtil.java` file is a utility class designed to facilitate consistent and structured logging within the PHEE Importer Operator. It provides methods for generating standard logging messages that include key details such as the custom resource name, namespace, and operation being performed. This helps in tracing the actions taken by the operator and diagnosing issues during its operation. The class ensures that all logging follows a uniform format, making it easier to analyze logs and understand the operator's behavior. By centralizing logging logic, this utility class also reduces code duplication and enhances maintainability. It plays a critical role in improving the observability and debuggability of the operator, making it easier to track and resolve issues.

#### NetworkingUtils.java File

The `NetworkingUtils.java` file is a utility class that provides methods for managing Kubernetes networking resources, specifically `Service` and `Ingress` resources associated with the `PaymentHubDeployment` custom resource. It includes methods to create, update, or delete these resources based on the custom resource's specifications. The class uses the Fabric8 Kubernetes client to interact with the Kubernetes API and perform these operations. For example, the `createService` method sets up a `Service` that exposes the custom resource's pods on a specified port, while the `createIngress` method configures an `Ingress` resource to manage external access to the service. This utility class is crucial for ensuring that the custom resource is accessible within the Kubernetes cluster and externally if needed. It handles the networking aspects of the custom resource, providing the necessary logic to manage `Service` and `Ingress` resources effectively.

#### OwnerReferenceUtils.java File

The `OwnerReferenceUtils.java` file is a utility class that provides methods for setting up and managing owner references in Kubernetes resources. Owner references are used to establish a parent-child relationship between resources, ensuring that when a parent resource is deleted, the associated child resources are also deleted automatically. This class includes methods to add an owner reference to a resource, ensuring that it is tied to the `PaymentHubDeployment` custom resource. The utility uses the Fabric8 Kubernetes client to interact with the Kubernetes API and modify the metadata of resources to include the owner reference. This is essential for ensuring proper cleanup of resources and preventing orphaned resources within the Kubernetes cluster. By managing owner references effectively, this utility class helps maintain the integrity and consistency of the resources associated with the custom resource.

#### RbacUtils.java File

The `RbacUtils.java` file is a utility class that provides methods for managing Kubernetes RBAC (Role-Based Access Control) resources associated with the `PaymentHubDeployment` custom resource. It includes methods to create, update, or delete RBAC resources such as `ServiceAccount`, `Role`, `RoleBinding`, `ClusterRole`, and `ClusterRoleBinding`. The class uses the Fabric8 Kubernetes client to interact with the Kubernetes API and perform these operations. For example, the `createServiceAccount` method sets up a `ServiceAccount` that can be used by the custom resource's pods to interact with the Kubernetes API, while the `createRole` and `createRoleBinding` methods establish the necessary permissions for the custom resource to manage its associated resources. This utility class is essential for ensuring that the custom resource has the appropriate permissions to operate within the Kubernetes cluster, providing the necessary logic to manage RBAC resources effectively.

#### ResourceUtils.java File

The `ResourceUtils.java` file is a utility class that provides methods for managing Kubernetes resources such as `ConfigMaps`, `Secrets`, and `PersistentVolumeClaims` (PVCs) associated with the `PaymentHubDeployment` custom resource. It includes methods to create, update, or delete these resources based on the custom resource's specifications. The class uses the Fabric8 Kubernetes client to interact with the Kubernetes API and perform these operations. For example, the `createConfigMap` method sets up a `ConfigMap` that can store configuration data for the custom resource, while the `createSecret` method handles sensitive data such as passwords and API keys. The `createPvc` method sets up a `PersistentVolumeClaim` to manage storage requirements. This utility class is crucial for ensuring that the custom resource has access to the necessary configuration, secrets, and storage resources, providing the necessary logic to manage these resources effectively.

#### StatusUpdateUtil.java File

The `StatusUpdateUtil.java` file is a utility class that provides methods for updating the status subresource of the `PaymentHubDeployment` custom resource in Kubernetes. The status subresource is used to track the current state of the custom resource, including fields like `availableReplicas`, `errorMessage`, `lastAppliedImage`, and `ready`. This class includes methods to update these fields based on the current state of the resources managed by the operator. The class uses the Fabric8 Kubernetes client to interact with the Kubernetes API and perform these status updates. This utility class is essential for keeping the custom resource's status in sync with the actual state of the resources in the cluster, providing the necessary logic to update and maintain the status subresource effectively.

## deploy-operator.sh

The `deploy-operator.sh` script is a shell script used to deploy the PHEE Importer Operator to a Kubernetes cluster. The script starts by creating the necessary Kubernetes resources, such as the custom resource definition (CRD) for `PaymentHubDeployment`, and then applies the `operator_deployment_manifests.yaml` file to deploy the operator itself. This script is essential for automating the deployment process of the operator, making it easy to set up the operator in a Kubernetes cluster. It provides a simple and repeatable way to deploy the operator, ensuring that all necessary steps are performed correctly.