# PHEE Operator Architecture

This document provides a quick overview of the architecture of the PHEE Operator, detailing the components this operator.

## Repo Structure

```
PHEE-operator/
├── deploy/
│   ├── cr/
│   │   └── ph-ee-CustomResource.yaml
│   ├── crds/
│   │   └── ph-ee-CustomResourceDefinition.yaml
│   └── operator/
│       └── operator_deployment_manifests.yaml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── paymenthub/
│                   ├── customresource/ 
│                   │   ├── PaymentHubDeployment.java
│                   │   ├── PaymentHubDeploymentSpec.java
│                   │   └── PaymentHubDeploymentStatus.java 
│                   ├── utils/
|                   │   ├── DeletionUtil.java 
|                   │   ├── DeploymentUtils.java 
|                   │   ├── LoggingUtil.java 
|                   │   ├── NetworkingUtils.java 
|                   │   ├── OwnerReferenceUtils.java 
|                   │   ├── RbacUtils.java 
|                   │   ├── ResourceUtils.java 
|                   │   └── StatusUpdateUtil.java
│                   ├── OperatorMain.java
|                   └── PaymentHubDeploymentController.java
├── ARCHITECTURE.md
├── deploy-operator.sh 
├── DEVELOPER_GUIDE.md
├── pom.xml
├── README.md 
└── ys_values.md
```

## Table of Contents

1. [Introduction](#introduction)
2. [Overview](#overview) 
3. [Components](#components)
   - [Custom Resource Definition (CRD)](#custom-resource-definition-crd)
   - [Custom Resource (CR)](#custom-resource-cr)
   - [OperatorMain](#OperatorMain)
   - [Controller](#controller)
   - [Utility Classes](#utility-classes) 
4. [Deployment](#deployment)
5. [Design Decisions](#design-decisions)


## Introduction

The PHEE Operator is a Kubernetes Operator designed to manage and automate the lifecycle of a specific Custom Resource (CR) within a Kubernetes cluster. Built on top of the Mifos-Gazelle script, the PHEE Operator streamlines the deployment, management, and cleanup of complex Kubernetes resources by leveraging custom automation and a well-defined structure. The operator is particularly tailored to manage deployments within the Mifos ecosystem, currently configured to handle deployments with their associated ingress and services under the paymenthub deployment. 

## Overview

The PHEE Operator comprises several key components:
- **Custom Resource Definitions (CRDs):** Define the schema and structure for custom resources in Kubernetes.
- **Custom Resources (CRs):** Represent the desired state of deployments as instances of the CRD.
- **OperatorMain:** Sets up the Kubernetes client, initializes the operator, registers the controller and starts the reconciliation process.
- **Controller:** Handles reconciliation loops to ensure the cluster's state matches the desired configuration.
- **Utility Classes:** Provide essential functions for creating resources, managing configurations, and logging.


## Components

### Custom Resource Definition (CRD)

- **File**: `deploy/crds/ph-ee-CustomResourceDefinition.yaml`

- **Purpose**: Defines the schema and structure for Custom Resources managed by the operator.

- **Details**:
  - Specifies fields such as `spec`, `status`, and others essential for CRs.
  - Includes validation rules for ensuring data integrity within Custom Resources.

### Custom Resource (CR)

- **File**: `deploy/cr/ph-ee-CustomResource.yaml`

- **Purpose**: Defines the actual Custom Resource instances, detailing the specific configuration for all deployments under the paymenthub.

- **Details**:
  - Populates the fields as specified in the CRD.
  - Contains configuration parameters and resource specifications for each deployment.

### OperatorMain

- **Main File**: `src/main/java/com/paymenthub/operator/OperatorMain.java`

- **Purpose**: Serves as the entry point for the operator, responsible for initializing the operator and registering the controller that handles CR lifecycle management.

- **Details**:
  - Initializes the Kubernetes client for communication with the cluster.
  - Registers the CRD schema and binds the controller to monitor and manage Custom Resources.

### Controller

- **File**: `src/main/java/com/paymenthub/operator/PaymentHubDeploymentController.java`

- **Purpose**: Implements the logic for reconciling the desired state of Custom Resources with the actual state in the cluster, ensuring that deployments are created and maintained according to the specifications in the CR.

- **Details**:
  - Continuously watches for changes in Custom Resources and reconciles the state.
  - Manages the creation and updates of Kubernetes resources like deployments, RBACs, services, and ingress as defined by the CR.
  - Handles error conditions and retry mechanisms to ensure stability and consistency in resource management.


### Utility Classes

#### DeletionUtil.java
- **File**: `src/main/java/com/paymenthub/utils/DeletionUtil.java`
- **Purpose**: Manages the deletion of Kubernetes resources like Deployments, RBAC resources, Secrets, ConfigMaps, and Services.

#### DeploymentUtils.java
- **File**: `src/main/java/com/paymenthub/utils/DeploymentUtils.java`
- **Purpose**: Handles creation, updating, and management of Kubernetes `Deployment` resources.

#### LoggingUtil.java
- **File**: `src/main/java/com/paymenthub/utils/LoggingUtil.java`
- **Purpose**: Provides consistent and structured logging for the operator.

#### NetworkingUtils.java
- **File**: `src/main/java/com/paymenthub/utils/NetworkingUtils.java`
- **Purpose**: Manages Kubernetes networking resources such as `Service` and `Ingress`.

#### OwnerReferenceUtils.java
- **File**: `src/main/java/com/paymenthub/utils/OwnerReferenceUtils.java`
- **Purpose**: Manages owner references in Kubernetes resources to ensure proper cleanup.

#### RbacUtils.java
- **File**: `src/main/java/com/paymenthub/utils/RbacUtils.java`
- **Purpose**: Handles creation and management of RBAC resources like `ServiceAccounts`, `Roles`, and `RoleBindings`.

#### ResourceUtils.java
- **File**: `src/main/java/com/paymenthub/utils/ResourceUtils.java`
- **Purpose**: Manages resources like `ConfigMaps`, `Secrets`, and `PersistentVolumeClaims`.

#### StatusUpdateUtil.java
- **File**: `src/main/java/com/paymenthub/utils/StatusUpdateUtil.java`
- **Purpose**: Updates the status subresource of the `PaymentHubDeployment` custom resource.


### Custom Resource Classes

#### PaymentHubDeployment.java

- **File**: `src/main/java/com/paymenthub/customresource/PaymentHubDeployment.java`

- **Purpose**: Defines the Custom Resource class according to the specification used in the controller file.

#### PaymentHubDeploymentSpec.java

- **File**: `src/main/java/com/paymenthub/customresource/PaymentHubDeploymentSpec.java`

- **Purpose**: Defines the specification for the operator, containing fields defined in the CRD and applied by the CRs. Provide getters and setters to access the CR values in the operator.

#### PaymentHubDeploymentStatus.java

- **File**: `src/main/java/com/paymenthub/customresource/PaymentHubDeploymentStatus.java`

- **Purpose**: Defines the status fields for the Custom Resource, allowing the operator to communicate the status of the resource.


### pom.xml

- **File**: `pom.xml`

- **Purpose**: This file contains all the dependencies required for this project.

## Deployment

The deployment of the PHEE Operator involves several steps:

- **CRD Deployment**: Apply the CRD to the cluster.
- **Operator Deployment**: Deploy the operator. 
- **CR Deployment**: Create custom resources as needed.

Follow steps in [README.md](README.md) 

Deployment files:

- **CRD**: `deploy/crds/ph-ee-CustomResourceDefinition.yaml`
- **Operator Deployment**: `deploy/operator/operator_deployment_manifests.yaml` 
- **Custom Resource**: `deploy/cr/ph-ee-CustomResource.yaml`

## Design Decisions

- **Language Choice**: The operator is implemented in Java due to its strong typing and extensive ecosystem.
- **Framework**: Utilized the Java Operator SDK for streamlined development.
- **CRD Structure**: Designed to be extensible and easy to validate.
- **Controller Logic**: Focused on idempotency and modularity.
  

 