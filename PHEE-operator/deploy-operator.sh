#!/bin/bash

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
export IMAGE_NAME="ph-ee-operator"
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
    Check if kubectl, docker, mvn, and helm are installed
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

    # 2. Upgrading the current helm chart deployment
    echo -e "${BLUE}Upgrading current helm deployment...${NC}"
    helm upgrade $RELEASE_NAME $HELM_CHART_PATH -n $HELM_NAMESPACE -f $VALUES_FILE || { echo -e "${RED}Helm upgrade failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Helm chart upgrade successful.${NC}"

    # 3. Build the Java Project
    echo -e "${BLUE}Building the Java project...${NC}"
    mvn clean package || { echo -e "${RED}Maven build failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Java project build successful.${NC}"

    # 4. Build the Docker Image Locally
    echo -e "${BLUE}Building the Docker image locally...${NC}"
    docker build -t $IMAGE_NAME:$IMAGE_TAG . || { echo -e "${RED}Docker image build failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Docker image build successful.${NC}"

    # 5. Save the Docker Image to a TAR File
    echo -e "${BLUE}Saving the Docker image to a TAR file...${NC}"
    docker save $IMAGE_NAME:$IMAGE_TAG -o $IMAGE_NAME.tar || { echo -e "${RED}Docker save failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Docker image saved to TAR file.${NC}"

    # 6. Import the Docker Image into k3s Cluster
    echo -e "${BLUE}Importing the Docker image into the k3s cluster...${NC}"
    sudo k3s ctr images import $IMAGE_NAME.tar || { echo -e "${RED}Image import to k3s failed. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}Docker image successfully imported to k3s.${NC}"

    # 7. Apply CRD, Operator, and CR
    echo -e "${BLUE}Applying CRD...${NC}"
    kubectl apply -f deploy/crds/ph-ee-CustomResourceDefinition.yaml
    echo -e "${GREEN}CRD applied successfully.${NC}"

    echo -e "${BLUE}Deploying the operator...${NC}"
    kubectl apply -f deploy/operator/operator_deployment_manifests.yaml
    echo -e "${GREEN}Operator deployed successfully.${NC}"

    echo -e "${BLUE}Applying CR...${NC}"
    kubectl apply -f deploy/cr/ph-ee-CustomResource.yaml
    echo -e "${GREEN}CR applied successfully.${NC}"

    # 8. Post-Deployment Verification
    echo -e "${BLUE}Verifying deployment...${NC}"
    kubectl rollout status deployment/ph-ee-operator -n $OPERATOR_NAMESPACE || { echo -e "${RED}Deployment verification failed. Exiting.${NC}"; exit 1; }

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
    kubectl delete -f deploy/cr/ph-ee-CustomResource.yaml || { echo -e "${RED}Failed to delete CR. Exiting.${NC}"; }
    echo -e "${GREEN}CR deleted successfully.${NC}"

    echo -e "${BLUE}Deleting operator...${NC}"
    kubectl delete -f deploy/operator/operator_deployment_manifests.yaml || { echo -e "${RED}Failed to delete operator. Exiting.${NC}"; }
    echo -e "${GREEN}Operator deleted successfully.${NC}"

    echo -e "${BLUE}Deleting CRD...${NC}"
    kubectl delete -f deploy/crds/ph-ee-CustomResourceDefinition.yaml || { echo -e "${RED}Failed to delete CRD. Exiting.${NC}"; }
    echo -e "${GREEN}CRD deleted successfully.${NC}"

    if [[ -z "$IMAGE_NAME" || -z "$IMAGE_TAG" ]]; then
        echo -e "${YELLOW}Image name or tag is not set correctly. Skipping Docker image removal.${NC}"
    else
        echo -e "${BLUE}Removing local Docker image...${NC}"
        docker rmi $IMAGE_NAME:$IMAGE_TAG || { echo -e "${RED}Failed to remove Docker image. Exiting.${NC}"; }
        echo -e "${GREEN}Docker image removed successfully.${NC}"
    fi

    echo -e "${BLUE}Removing the TAR file...${NC}"
    rm $IMAGE_NAME.tar || { echo -e "${RED}Failed to remove TAR file. Exiting.${NC}"; exit 1; }
    echo -e "${GREEN}TAR file removed successfully.${NC}"

    echo -e "${GREEN}${BOLD}Cleanup completed successfully!${NC}"
}

update_cr () {
        # Display script banner
    echo -e "${BLUE}${BOLD}"
    echo "===================================================="
    echo "              Updating Custom Resource(CR)          "
    echo "===================================================="
    echo -e "${NC}"

    echo -e "${BLUE}Updating CR...${NC}"
    kubectl apply -f deploy/cr/ph-ee-CustomResource.yaml
    echo -e "${GREEN}CR applied successfully.${NC}"

}

update_operator () {
        # Display script banner
    echo -e "${BLUE}${BOLD}"
    echo "===================================================="
    echo "              Updating Operator Deployment          "
    echo "===================================================="
    echo -e "${NC}"

    echo -e "${BLUE}Deploying the operator...${NC}"
    kubectl apply -f deploy/operator/operator_deployment_manifests.yaml
    echo -e "${GREEN}Operator deployed successfully.${NC}"

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
elif [ "$1" == "-u" ]; then
    case "$2" in
        cr)
            update_cr
            ;;
        operator)
            update_operator
            ;;
        *)
            echo -e "${RED}Invalid update mode specified. Use '-u cr' or '-u operator'.${NC}"
            exit 1
            ;;
    esac
else
    echo -e "${YELLOW}Usage: ./deploy-operator.sh -m <mode> or ./deploy-operator.sh -u <update_mode>${NC}"
    echo "Modes:"
    echo -e "  ${GREEN}deploy${NC}   - Upgrade Helm chart, build, deploy, and verify the operator."
    echo -e "  ${GREEN}cleanup${NC}  - Remove the operator and related resources."
    echo "Update Modes:"
    echo -e "  ${GREEN}cr${NC}        - Apply updates to the Custom Resource (CR)."
    echo -e "  ${GREEN}operator${NC}  - Apply updates to the operator deployment."
    exit 1
fi


# End of script
