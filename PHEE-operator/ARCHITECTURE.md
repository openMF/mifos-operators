# Architecture
This file contain the repo structure and information on each component operator include.

## Repo Structure

PHEE-operator/
├── deploy/
│   ├── crds/
│   │   └── ph-ee-importer-rdbms-crd.yaml 
│   ├── operator-deployment.yaml
│   └── ph-ee-importer-rdbms-cr.yaml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── operator/
│                       ├── PhEeImporterRdbms.java
│                       ├── PhEeImporterRdbmsController.java
│                       ├── PhEeImporterRdbmsSpec.java 
│                       └── OperatorMain.java
├── pom.xml
└── README.md

**ph-ee-importer-rdbms-crd.yaml:** This file contains the Custom Resource Definition (CRD) for the operator. The CRD defines the schema for the Custom Resources that will be managed by the operator.

**operator-deployment.yaml:** This file contains the deployment script for the operator itself, including its required Role-Based Access Control (RBAC) configurations.

**ph-ee-importer-rdbms-cr.yaml:** This file contains the Custom Resource (CR) script for the Importer RDBMS, which includes values for the fields defined in the CRD.

**OperatorMain.java:** This is the main file for the operator. It registers the operator controller and starts the operator to apply the reconciliation logic.

**PhEeImporterRdbmsController.java:** This file is the controller for the operator, containing the reconciliation logic for the Importer RDBMS deployment and its RBAC configurations. It uses the CR to create Kubernetes resource objects.

**PhEeImporterRdbmsSpec.java:** This file defines the specification for the operator, containing fields defined in the CRD and applied by the CRs.

**PhEeImporterRdbms.java:** This file defines the Custom Resource class according to the specification used in the controller file.

**pom.xml:** This file contains all the dependencies required for this project.


## Note
This file is still in progress will be updated as the project progresses.