#!/bin/bash

# Commands that can be used for this script
# ./deploy-operator.sh -m deploy 
# ./deploy-operator.sh -m cleanup

# Exit script on any error
set -e

# Text Formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;94m'
BOLD='\033[1m'
NC='\033[0m' # No Color


# Common Environment Setup for both deploy and cleanup
export IMAGE_NAME="ph-ee-importer-rdbms-operator"
export IMAGE_TAG="latest"
export OPERATOR_NAMESPACE="default"   
export HELM_CHART_PATH="../mifos-gazelle/src/mojafos/deployer/apps/ph_template/helm/gazelle"   
export RELEASE_NAME="phee"   
export VALUES_FILE="ys_values.yaml"   
export HELM_NAMESPACE="paymenthub"

# Function to deploy the operator
deploy_operator() {

    # Display script banner
    echo -e "${BLUE}${BOLD}"
    echo "===================================================="
    echo "              K8s Operator Deployment               "
    echo "===================================================="
    echo -e "${NC}"

    echo -e "${YELLOW}${BOLD}Starting Operator Deployment...${NC}"

    # 1. Pre-requisites Check
    echo -e "${BLUE}Checking pre-requisites...${NC}"
    # Check if kubectl, docker, mvn, and helm are installed
    if ! command -v kubectl &> /dev/null || ! command -v docker &> /dev/null || ! command -v helm &> /dev/null
    then
        echo -e "${RED}kubectl, docker, and helm are required but not installed. Exiting.${NC}"
        exit 1
    fi

    # Check if Java is installed, if not, install it
    if ! command -v java &> /dev/null
    then
        echo -e "${YELLOW}Java is not installed. Installing OpenJDK 21...${NC}"
        sudo apt-get update
        sudo apt-get install openjdk-21-jdk -y || { echo -e "${RED}Failed to install Java. Exiting.${NC}"; exit 1; }
        echo -e "${GREEN}Java installed successfully.${NC}"
    else
        echo -e "${GREEN}Java is already installed.${NC}"
    fi

    # Check if Maven is installed, if not, install it
    if ! command -v mvn &> /dev/null
    then
        echo -e "${YELLOW}Maven is not installed. Installing Maven...${NC}"
        sudo apt-get install maven -y || { echo -e "${RED}Failed to install Maven. Exiting.${NC}"; exit 1; }
        echo -e "${GREEN}Maven installed successfully.${NC}"
    else
        echo -e "${GREEN}Maven is already installed.${NC}"
    fi

    echo -e "${GREEN}Pre-requisites are met.${NC}"

    # 2. Upgrading the Helm chart with ys_values.yaml
    echo -e "${BLUE}Upgrading helm chart...${NC}" 
    helm upgrade $RELEASE_NAME $HELM_CHART_PATH -n $HELM_NAMESPACE -f $VALUES_FILE || { echo -e "${RED}Helm upgrade failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Helm chart upgrade successful.${NC}"

    # 3. Build the Java Project
    echo -e "${BLUE}Building the Java project...${NC}"
    mvn clean install || { echo -e "${RED}Maven build failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Java project build successful.${NC}"

    # 4. Build the Docker Image Locally
    echo -e "${BLUE}Building the Docker image locally...${NC}"
    mvn compile jib:dockerBuild -Dimage=$IMAGE_NAME:$IMAGE_TAG || { echo -e "${RED}Docker image build failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Docker image build successful.${NC}"

    # 5. Save the Docker Image to a TAR File
    echo -e "${BLUE}Saving the Docker image to a TAR file...${NC}"
    sudo docker save $IMAGE_NAME:$IMAGE_TAG -o $IMAGE_NAME.tar || { echo -e "${RED}Docker save failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Docker image saved to TAR file.${NC}"

    # 6. Import the Docker Image into k3s Cluster
    echo -e "${BLUE}Importing the Docker image into the k3s cluster...${NC}"
    sudo k3s ctr images import $IMAGE_NAME.tar || { echo -e "${RED}Image import to k3s failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Docker image successfully imported to k3s.${NC}"

    # 7. Apply CRD, Operator, and CR
    echo -e "${BLUE}Applying CRD...${NC}"
    kubectl apply -f deploy/crds/ph-ee-importer-rdbms-crd.yaml
    echo -e "${GREEN}CRD applied successfully.${NC}"

    echo -e "${BLUE}Deploying the operator...${NC}"
    kubectl apply -f deploy/operator/operator.yaml
    echo -e "${GREEN}Operator deployed successfully.${NC}"

    echo -e "${BLUE}Applying CR...${NC}"
    kubectl apply -f deploy/cr/ph-ee-importer-rdbms-cr.yaml
    echo -e "${GREEN}CR applied successfully.${NC}"

    # 8. Post-Deployment Verification
    echo -e "${BLUE}Verifying deployment...${NC}"
    kubectl rollout status deployment/phee-importer-operator -n $OPERATOR_NAMESPACE || { echo -e "${RED}Deployment verification failed. Exiting.${NC}"; exit 1; }

    echo -e "${GREEN}${BOLD}Operator deployment completed successfully!${NC}"
}

# Function to clean up the deployed resources
cleanup_operator() {

    # Display script banner
    echo -e "${BLUE}${BOLD}"
    echo "===================================================="
    echo "              K8s Operator Cleaup               "
    echo "===================================================="
    echo -e "${NC}"

    echo -e "${YELLOW}${BOLD}Starting Cleanup...${NC}"

    echo -e "${BLUE}Deleting CR...${NC}"
    kubectl delete -f deploy/cr/ph-ee-importer-rdbms-cr.yaml || { echo -e "${RED}Failed to delete CR. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}CR deleted successfully.${NC}"

    echo -e "${BLUE}Deleting operator...${NC}"
    kubectl delete -f deploy/operator/operator.yaml || { echo -e "${RED}Failed to delete operator. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Operator deleted successfully.${NC}"

    echo -e "${BLUE}Deleting CRD...${NC}"
    kubectl delete -f deploy/crds/ph-ee-importer-rdbms-crd.yaml || { echo -e "${RED}Failed to delete CRD. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}CRD deleted successfully.${NC}"

    if [[ -z "$IMAGE_NAME" || -z "$IMAGE_TAG" ]]; then
        echo -e "${YELLOW}Image name or tag is not set correctly. Skipping Docker image removal.${NC}"
    else
        echo -e "${BLUE}Removing local Docker image...${NC}"
        docker rmi $IMAGE_NAME:$IMAGE_TAG || { echo -e "${RED}Failed to remove Docker image. Exiting.${NC}"; exit 1; }
        echo -e "${GREEN}Docker image removed successfully.${NC}"
    fi

    echo -e "${BLUE}Removing the TAR file...${NC}"
    rm $IMAGE_NAME.tar || { echo -e "${RED}Failed to remove TAR file. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}TAR file removed successfully.${NC}"

    echo -e "${GREEN}${BOLD}Cleanup completed successfully!${NC}"
}

# Main script logic to handle different modes
if [ "$1" == "-m" ]; then
    case "$2" in
        deploy)
            deploy_operator
            ;;
        cleanup)
            cleanup_operator
            ;;
        *)
            echo -e "${RED}Invalid mode specified. Use '-m deploy' or '-m cleanup'.${NC}"
            exit 1
            ;;
    esac
else
    echo -e "${YELLOW}Usage: ./deploy-operator.sh -m <mode>${NC}"
    echo "Modes:"
    echo "  ${GREEN}deploy${NC}   - Upgrade Helm chart, build, deploy, and verify the operator."
    echo "  ${GREEN}cleanup${NC}  - Remove the operator and related resources."
    exit 1
fi

# End of script
