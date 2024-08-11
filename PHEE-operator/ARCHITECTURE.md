# PHEE Operator Architecture

This document provides an in-depth overview of the architecture of the PHEE Operator, detailing the design decisions, components, and their interactions.

## Repo Structure

```
PHEE-operator/
├── deploy/
│   ├── cr/
│   │   └── ph-ee-importer-rdbms-cr.yaml
│   ├── crds/
│   │   └── ph-ee-importer-rdbms-crd.yaml
│   └── operator/
│       └── operator.yaml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   ├── customresource/ 
│                   │   ├── PhEeImporterRdbms.java
│                   │   ├── PhEeImporterRdbmsSpec.java
│                   │   └── PhEeImporterRdbmsStatus.java
│                   ├── utils/
|                   │   ├── LoggingUtil.java 
|                   │   ├── ProbeUtils.java 
|                   │   ├── ResourceDeletionUtil.java 
|                   │   └── StatusUpdateUtil.java
│                   ├── OperatorMain.java
|                   └── PhEeImporterRdbmsController.java
├── ARCHITECTURE.md
├── pom.xml
└── README.md
```

## Table of Contents

1. [Introduction](#introduction)
2. [Overview](#overview)
3. [Components](#components)
   - [Custom Resource Definition (CRD)](#custom-resource-definition-crd)
   - [Custom Resource (CR)](#custom-resource-cr)
   - [Operator](#operator)
   - [Controller](#controller)
   - [Utility Classes](#utility-classes)
4. [Deployment](#deployment)
5. [Design Decisions](#design-decisions) 

## Introduction

The PHEE Operator is designed to manage and automate the lifecycle of a Custom Resource within a Kubernetes cluster. This document outlines the key components and architectural decisions that shape the operator.

## Overview

The PHEE Operator comprises several key components:
- Custom Resource Definitions (CRDs) that specify the schema for custom resources.
- Custom Resources (CRs) that represent instances of the CRD.
- The Operator which contains the logic for managing CRs.
- The Controller which handles reconciliation loops to ensure the desired state of the cluster.
- Utility classes that aid in logging and status updates.


## Components

### Custom Resource Definition (CRD)

- **File**: `deploy/crds/ph-ee-importer-rdbms-crd.yaml`

- **Purpose**: This file contains the Custom Resource Definition (CRD) for the operator. The CRD defines the schema for the Custom Resources that will be managed by the operator.

- **Details**:
  - Specifies fields such as `spec`, `status`, etc.
  - Defines validation criteria for each field.

### Custom Resource (CR)

- **File**: `deploy/cr/ph-ee-importer-rdbms-cr.yaml`

- **Purpose**: This file contains the Custom Resource (CR) script for the Importer RDBMS, which includes values for the fields defined in the CRD.

- **Details**:
  - Contains fields defined in the CRD.
  - Specifies configuration parameters for the Importer RDBMS.

### Operator

- **Main File**: `src/main/java/com/example/operator/OperatorMain.java`

- **Purpose**: This is the main file for the operator. It registers the operator controller and starts the operator to apply the reconciliation logic.

- **Details**:
  - Initializes the Kubernetes client.
  - Registers custom resource schemas and controllers.

### Controller

- **File**: `src/main/java/com/example/operator/PhEeImporterRdbmsController.java`

- **Purpose**: This file is the controller for the operator, containing the reconciliation logic for the Importer RDBMS deployment and its RBAC configurations. It uses the CR to create Kubernetes resource objects.

- **Details**:
  - Watches for changes to custom resources.
  - Applies necessary changes to the cluster to match the desired state.
  - Handles error states and retries.

### Custom Resource Classes

#### PhEeImporterRdbms.java

- **File**: `src/main/java/com/example/customresource/PhEeImporterRdbms.java`

- **Purpose**: Defines the Custom Resource class according to the specification used in the controller file.

#### PhEeImporterRdbmsSpec.java

- **File**: `src/main/java/com/example/customresource/PhEeImporterRdbmsSpec.java`

- **Purpose**: Defines the specification for the operator, containing fields defined in the CRD and applied by the CRs.

#### PhEeImporterRdbmsStatus.java

- **File**: `src/main/java/com/example/customresource/PhEeImporterRdbmsStatus.java`

- **Purpose**: Defines the status fields for the Custom Resource, allowing the operator to communicate the current state of the resource.

### Utility Classes

#### LoggingUtil.java

- **File**: `src/main/java/com/example/utils/LoggingUtil.java`

- **Purpose**: Provides utility methods for logging within the operator.

#### ProbeUtils.java

- **File**: `src/main/java/com/example/utils/ProbeUtils.java`

- **Purpose**: Provides helper methods for adding probes to the deployment.

#### ResourceDeletionUtil.java

- **File**: `src/main/java/com/example/utils/ResourceDeletionUtil.java`

- **Purpose**: Provides helper methods to delete the deployment and its RBACs according to toggle enable/disable in Custom Resource.

#### StatusUpdateUtil.java

- **File**: `src/main/java/com/example/utils/StatusUpdateUtil.java`

- **Purpose**: Provides utility methods for updating the status of the Custom Resource.

### pom.xml

- **File**: `pom.xml`

- **Purpose**: This file contains all the dependencies required for this project.

## Deployment

The deployment of the PHEE Operator involves several steps:

- **CRD Deployment**: Apply the CRD to the cluster.
- **Operator Deployment**: Deploy the operator using a Deployment resource. 
- **CR Deployment**: Create custom resources as needed.

Follow steps in [README.md](./README.md) 

Deployment files:

- **CRD**: `deploy/crds/ph-ee-importer-rdbms-crd.yaml`
- **Operator Deployment**: `deploy/operator/operator.yaml` 
- **Custom Resource**: `deploy/cr/ph-ee-importer-rdbms-cr.yaml`

## Design Decisions

- **Language Choice**: The operator is implemented in Java due to its strong typing and extensive ecosystem.
- **Framework**: Utilized the Java Operator SDK for streamlined development.
- **CRD Structure**: Designed to be extensible and easy to validate.
- **Controller Logic**: Focused on idempotency and robustness.
  


### Note
This file is still in progress will be updated as the project progresses.