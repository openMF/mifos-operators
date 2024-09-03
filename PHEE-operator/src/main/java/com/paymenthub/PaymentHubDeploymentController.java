package com.paymenthub;

// Kubernetes API model imports
import io.fabric8.kubernetes.api.model.*;  
import io.fabric8.kubernetes.api.model.apps.*;  
 
// Kubernetes client imports
import io.fabric8.kubernetes.client.KubernetesClient; 
import io.fabric8.kubernetes.client.dsl.Resource;  

// Operator SDK imports
import io.javaoperatorsdk.operator.api.reconciler.Context;    
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;    
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;  
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;  

// Logging imports
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  

// Custom classes and utils
import com.paymenthub.customresource.PaymentHubDeploymentSpec; 
import com.paymenthub.customresource.PaymentHubDeployment;  
import com.paymenthub.utils.LoggingUtil;  
import com.paymenthub.utils.StatusUpdateUtil;   
import com.paymenthub.utils.DeletionUtil;  
import com.paymenthub.utils.DeploymentUtils;  
import com.paymenthub.utils.RbacUtils;  
import com.paymenthub.utils.ResourceUtils;  
import com.paymenthub.utils.NetworkingUtils;  
import com.paymenthub.utils.OwnerReferenceUtils;  
 

// Java utils
import java.time.Instant;  
import java.util.*;  
import java.util.stream.Collectors;  



@ControllerConfiguration
public class PaymentHubDeploymentController implements Reconciler<PaymentHubDeployment> {


    /**
     * The PaymentHubDeploymentController class is responsible for managing Kubernetes resources related to the
     * `PaymentHubDeployment` custom resource. It uses various utilities and the Kubernetes client for operations.
     * 
     * - {@code log} is a static logger for logging information, warnings, and errors.
     * - {@code kubernetesClient} is used to interact with the Kubernetes API server to manage resources.
     * - {@code rbacUtils} provides utility methods for handling RBAC (Role-Based Access Control) related operations.
     * - {@code resourceUtils} offers utility methods for creating and managing Kubernetes resources such as ConfigMaps and Secrets.
     * - {@code networkingUtils} includes utility methods for managing networking components like Services and Ingresses.
     */
    private static final Logger log = LoggerFactory.getLogger(PaymentHubDeploymentController.class);
    private final KubernetesClient kubernetesClient;
    private final RbacUtils rbacUtils;
    private final ResourceUtils resourceUtils;
    private final NetworkingUtils networkingUtils;

    /**
     * Constructor for initializing the PaymentHubDeploymentController with the necessary clients and utilities.
     * 
     * @param kubernetesClient The Kubernetes client used for interacting with the Kubernetes API server.
     */
    public PaymentHubDeploymentController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        this.rbacUtils = new RbacUtils(kubernetesClient);
        this.resourceUtils = new ResourceUtils(kubernetesClient);
        this.networkingUtils = new NetworkingUtils(kubernetesClient);
    }

    /**
     * Reconciles the custom resource by managing associated Kubernetes resources such as RBAC, Secrets, ConfigMaps,
     * Ingress, Services, and the Deployment itself. Handles the enablement and disablement of these resources
     * based on the specifications defined in the custom resource.
     *
     * @param resource The custom resource containing the specifications for the various Kubernetes resources.
     * @param context  The context in which the reconciliation is taking place, providing access to cached resources.
     * @return UpdateControl<PaymentHubDeployment> The control object that dictates the next steps for the reconciliation loop.
     */
    @Override
    public UpdateControl<PaymentHubDeployment> reconcile(PaymentHubDeployment resource, Context<PaymentHubDeployment> context) {
        String resourceName = resource.getMetadata().getName();

        // Check if the deployment is disabled
        if (resource.getSpec().getEnabled() == null || !resource.getSpec().getEnabled()) {
            log.info("Deployment {} is disabled, deleting all associated resources.", resourceName);
            DeletionUtil.deleteResources(kubernetesClient, resource);
            return StatusUpdateUtil.updateDisabledStatus(kubernetesClient, resource);
        }

        // Log detailed resource information for debugging
        LoggingUtil.logResourceDetails(resource);

        try {
            // Check and reconcile RBACs
            if (resource.getSpec().getRbacEnabled() == null || !resource.getSpec().getRbacEnabled()) {
                log.info("RBACs for resource {} are disabled, deleting associated RBAC resources.", resourceName);
                DeletionUtil.deleteRbacResources(kubernetesClient, resource);
            } else {
                // INFO level log to indicate RBAC reconciliation start
                log.info("Reconciling RBAC resources for {}.", resourceName); 
                rbacUtils.reconcileServiceAccount(resource);
                rbacUtils.reconcileRole(resource);
                rbacUtils.reconcileRoleBinding(resource);
                rbacUtils.reconcileClusterRole(resource);
                rbacUtils.reconcileClusterRoleBinding(resource);
            }

            // Check and reconcile Secrets
            if (resource.getSpec().getSecretEnabled() == null || !resource.getSpec().getSecretEnabled()) {
                log.info("Secrets for resource {} are disabled, deleting associated Secret resources.", resourceName);
                DeletionUtil.deleteSecretResources(kubernetesClient, resource);
            } else {
                // DEBUG level log to indicate Secret reconciliation
                log.debug("Reconciling Secret for {}.", resourceName);
                resourceUtils.reconcileSecret(resource);
            } 

            // Check and reconcile ConfigMaps
            if (resource.getSpec().getConfigMapEnabled() == null || !resource.getSpec().getConfigMapEnabled()) {
                log.info("ConfigMap for resource {} is disabled, deleting associated ConfigMap resources.", resourceName);
                DeletionUtil.deleteConfigMapResources(kubernetesClient, resource);
            } else {
                // DEBUG level log to indicate ConfigMap reconciliation
                log.debug("Reconciling ConfigMap for {}.", resourceName);
                resourceUtils.reconcileConfigmap(resource);
            }

            // Check and reconcile Ingress and Services
            if ("ph-ee-connector-gsma".equals(resourceName)) {
                // Special case: only reconcile Services, not Ingress, for "ph-ee-connector-gsma"
                log.info("Special case for {}: Reconciling Services only, not Ingress.", resourceName);
                networkingUtils.reconcileServices(resource);
            } else {
                if (resource.getSpec().getIngressEnabled() == null || !resource.getSpec().getIngressEnabled()) {
                    log.info("Ingress for resource {} is disabled, deleting associated Ingress resources.", resourceName);
                    DeletionUtil.deleteIngressResources(kubernetesClient, resource);
                } else {
                    // INFO level log to indicate Ingress and Service reconciliation
                    log.info("Reconciling Ingress and Service for {}.", resourceName);
                    networkingUtils.reconcileServices(resource);
                    networkingUtils.reconcileIngress(resource);
                }
            }

            // Always reconcile the Deployment itself
            log.info("Reconciling Deployment for {}.", resourceName);
            reconcileDeployment(resource);

            // Return success status update
            log.info("Reconciliation successful for {}.", resourceName);
            return StatusUpdateUtil.updateStatus(kubernetesClient, resource, resource.getSpec().getReplicas(), resource.getSpec().getImage(), true, "");

        } catch (Exception e) {
            // Log the error and return an error status update
            log.error("Error during reconciliation for resource " + resourceName, e);
            return StatusUpdateUtil.updateErrorStatus(kubernetesClient, resource, resource.getSpec().getImage(), e);
        }
    }

    
    /**
     * Reconciles the Deployment based on the given custom resource.
     * 
     * @param resource The custom resource containing the specifications for the deployment.
     */
    private void reconcileDeployment(PaymentHubDeployment resource) {
        log.info("Reconciling Deployment for resource: {}", resource.getMetadata().getName());
        Deployment deployment = createDeployment(resource);
        log.info("Created Deployment spec: {}", deployment);

        Resource<Deployment> deploymentResource = kubernetesClient.apps().deployments()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName());

        if (deploymentResource.get() == null) {
            deploymentResource.create(deployment);
            log.info("Created new Deployment: {}", resource.getMetadata().getName());
        } else {
            deploymentResource.patch(deployment);
            log.info("Updated existing Deployment: {}", resource.getMetadata().getName());
        }
    }


    /**
     * Creates a Kubernetes Deployment object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the deployment configuration.
     * @return The created Deployment object, or null if critical fields are missing.
     */
    private Deployment createDeployment(PaymentHubDeployment resource) {
        log.info("Creating Deployment spec for resource: {}", resource.getMetadata().getName());

        // Define labels for the Deployment and Pod templates
        Map<String, String> labels = resource.getSpec().getLabels();
        if (labels == null) {
            labels = new HashMap<>();
        }

        labels.putIfAbsent("app", resource.getMetadata().getName());
        labels.putIfAbsent("app.kubernetes.io/managed-by", "ph-ee-operator");

        // Build the main container with environment variables, resources, and volume mounts
        ContainerBuilder containerBuilder = new ContainerBuilder()
            .withName(resource.getMetadata().getName())
            .withImage(resource.getSpec().getImage())
            .withEnv(DeploymentUtils.createEnvironmentVariables(resource))
            .withResources(DeploymentUtils.createResourceRequirements(resource))
            .withLivenessProbe(DeploymentUtils.createProbe(resource, "liveness"))
            .withReadinessProbe(DeploymentUtils.createProbe(resource, "readiness"));

        // Conditionally add the container port if it's provided in the CR
        Integer containerPort = resource.getSpec().getContainerPort();
        if (containerPort != null) {
            containerBuilder.withPorts(new ContainerPortBuilder()
                .withContainerPort(containerPort)
                .build());
        } else {
            log.info("Container port not provided, skipping port configuration.");
        }

        // Logging for volume mount configuration
        log.debug("Volume mount configuration: {}", resource.getSpec().getVolMount());

        // Add volume mount conditionally
        if (resource.getSpec().getVolMount() != null && Boolean.TRUE.equals(resource.getSpec().getVolMount().getEnabled())) {
            String volMountName = resource.getSpec().getVolMount().getName();
            if (volMountName != null) {
                containerBuilder.withVolumeMounts(new VolumeMountBuilder()
                    .withName(volMountName)
                    .withMountPath("/config")
                    .build());
            } else {
                log.warn("Volume mount name is null, skipping volume mount.");
            }
        }

        Container container = containerBuilder.build();

        // Create PodSpec with the defined container and volumes
        PodSpecBuilder podSpecBuilder = new PodSpecBuilder()
            .withContainers(container);

        // Check the flag to determine whether to add the init container 
        if (Boolean.TRUE.equals(resource.getSpec().getInitContainerEnabled())) {
            log.info("Init container enabled, adding to the PodSpec.");
            Container initContainer = new ContainerBuilder()
                .withName("wait-db")
                .withImage("jwilder/dockerize")
                .withArgs("-timeout=120s", "-wait", "tcp://operationsmysql:3306") 
                .build();
            podSpecBuilder.withInitContainers(initContainer);
        } else {
            log.info("Init container not enabled, skipping init container.");
        }

        // Add volumes conditionally
        if (resource.getSpec().getVolMount() != null && Boolean.TRUE.equals(resource.getSpec().getVolMount().getEnabled())) {
            String volMountName = resource.getSpec().getVolMount().getName();
            if (volMountName != null) {
                podSpecBuilder.withVolumes(new VolumeBuilder()
                    .withName(volMountName)
                    .withConfigMap(new ConfigMapVolumeSourceBuilder()
                        .withName(volMountName)
                        .build())
                    .build());
            } else {
                log.warn("Volume mount name is null, skipping volume creation.");
            }
        }

        PodSpec podSpec = podSpecBuilder.build();

        // Build the PodTemplateSpec with metadata and spec
        PodTemplateSpec podTemplateSpec = new PodTemplateSpecBuilder()
            .withNewMetadata()
                .withLabels(labels)
            .endMetadata()
            .withSpec(podSpec)
            .build();

        // Define the DeploymentSpec with replicas, selector, and template
        DeploymentSpec deploymentSpec = new DeploymentSpecBuilder()
            .withReplicas(resource.getSpec().getReplicas())
            .withSelector(new LabelSelectorBuilder()
                .withMatchLabels(labels)
                .build())
            .withTemplate(podTemplateSpec)
            .build();

        // Handle the case where metadata fields might be null
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();

        if (name == null || namespace == null) {
            log.error("Name or namespace is null, cannot create deployment metadata.");
            return null;  // Or handle it appropriately
        }

        // Create Deployment metadata with owner references
        ObjectMeta metadata = new ObjectMetaBuilder()
            .withName(name)
            .withNamespace(namespace)
            .withLabels(labels)
            .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource))
            .build();

        // Log the final deployment object for debugging purposes
        log.debug("Final Deployment object: {}", metadata);

        // Build the final Deployment object
        return new DeploymentBuilder()
            .withMetadata(metadata)
            .withSpec(deploymentSpec)
            .build();
    }


}