package com.example;
 
// Kubernetes API model imports
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.*;
import io.fabric8.kubernetes.api.model.rbac.*;

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

// Custom classes
import com.example.customresource.PhEeImporterRdbms;
import com.example.utils.LoggingUtil;
import com.example.utils.StatusUpdateUtil;
import com.example.utils.ProbeUtils; 
import com.example.utils.ResourceDeletionUtil;

// utils
import java.util.*;


@ControllerConfiguration
public class PhEeImporterRdbmsController implements Reconciler<PhEeImporterRdbms> {

    private static final Logger log = LoggerFactory.getLogger(PhEeImporterRdbmsController.class);

    private final KubernetesClient kubernetesClient;

    public PhEeImporterRdbmsController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public UpdateControl<PhEeImporterRdbms> reconcile(PhEeImporterRdbms resource, Context<PhEeImporterRdbms> context) {
        String resourceName = resource.getMetadata().getName();

        // Check if the deployment is disabled
        if (resource.getSpec().getEnabled() == null || !resource.getSpec().getEnabled()) {
            log.info("Deployment {} is disabled, deleting all associated resources.", resourceName);
            ResourceDeletionUtil.deleteResources(kubernetesClient, resource);
            return StatusUpdateUtil.updateDisabledStatus(kubernetesClient, resource);
        }

        // Deployment is enabled, proceed with individual resource checks
        LoggingUtil.logResourceDetails(resource);

        try {
            // Check and reconcile RBACs
            if (resource.getSpec().getrbacEnabled() == null || !resource.getSpec().getrbacEnabled()) {
                log.info("RBACs for resource {} are disabled, deleting associated RBAC resources.", resourceName);
                ResourceDeletionUtil.deleteRbacResources(kubernetesClient, resource);
            } else {
                reconcileServiceAccount(resource);
                reconcileClusterRole(resource);
                reconcileClusterRoleBinding(resource);
                reconcileRole(resource);
                reconcileRoleBinding(resource);
            }

            // Check and reconcile Secrets
            if (resource.getSpec().getsecretEnabled() == null || !resource.getSpec().getsecretEnabled()) {
                log.info("Secrets for resource {} are disabled, deleting associated Secret resources.", resourceName);
                ResourceDeletionUtil.deleteSecretResources(kubernetesClient, resource);
            } else {
                reconcileSecret(resource);
            } 

            // Always reconcile the Deployment itself
            reconcileDeployment(resource);

            // Return success status update
            return StatusUpdateUtil.updateStatus(kubernetesClient, resource, resource.getSpec().getReplicas(), resource.getSpec().getImage(), true, "");

        } catch (Exception e) {
            // Log the error and return an error status update
            LoggingUtil.logError("Error during reconciliation for resource " + resourceName, e);
            return StatusUpdateUtil.updateErrorStatus(kubernetesClient, resource, resource.getSpec().getImage(), e);
        }
    } 
    
    private void reconcileDeployment(PhEeImporterRdbms resource) {
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

    private Deployment createDeployment(PhEeImporterRdbms resource) {
        log.info("Creating Deployment spec for resource: {}", resource.getMetadata().getName());

        // Define labels for the Deployment and Pod templates
        Map<String, String> labels = new HashMap<>();
        labels.put("app", resource.getMetadata().getName());
        labels.put("managed-by", "ph-ee-importer-operator"); // Optional label for identifying managed resources

        // Build the container with environment variables, resources, and volume mounts
        Container container = new ContainerBuilder()
            .withName(resource.getMetadata().getName())
            .withImage(resource.getSpec().getImage())
            .withEnv(createEnvironmentVariables(resource))
            .withResources(createResourceRequirements(resource))
            .withVolumeMounts(new VolumeMountBuilder()
                .withName("ph-ee-config")
                .withMountPath("/config")
                .build())
            .withLivenessProbe(ProbeUtils.createProbe(resource, "liveness"))
            .withReadinessProbe(ProbeUtils.createProbe(resource, "readiness"))
            .build();

        // Create PodSpec with the defined container and volumes
        PodSpec podSpec = new PodSpecBuilder()
            .withContainers(container)
            .withVolumes(new VolumeBuilder()
                .withName("ph-ee-config")
                .withConfigMap(new ConfigMapVolumeSourceBuilder()
                    .withName("ph-ee-config")
                    .build())
                .build())
            .build();

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

        // Create Deployment metadata with owner references
        ObjectMeta metadata = new ObjectMetaBuilder()
            .withName(resource.getMetadata().getName())
            .withNamespace(resource.getMetadata().getNamespace())
            .withLabels(labels)
            .withOwnerReferences(createOwnerReferences(resource))
            .build();

        // Build the final Deployment object
        return new DeploymentBuilder()
            .withMetadata(metadata)
            .withSpec(deploymentSpec)
            .build();
    }

    // Helper method to create environment variables
    private List<EnvVar> createEnvironmentVariables(PhEeImporterRdbms resource) {
        return Arrays.asList(
            new EnvVar("SPRING_PROFILES_ACTIVE", resource.getSpec().getSpringProfilesActive(), null),
            new EnvVar("DATASOURCE_CORE_USERNAME", resource.getSpec().getDatasource().getUsername(), null),
            new EnvVar("DATASOURCE_CORE_PASSWORD", null, new EnvVarSourceBuilder().withNewSecretKeyRef("database-password", "importer-rdbms-secret", false).build()),
            new EnvVar("DATASOURCE_CORE_HOST", resource.getSpec().getDatasource().getHost(), null),
            new EnvVar("DATASOURCE_CORE_PORT", String.valueOf(resource.getSpec().getDatasource().getPort()), null),
            new EnvVar("DATASOURCE_CORE_SCHEMA", resource.getSpec().getDatasource().getSchema(), null),
            new EnvVar("LOGGING_LEVEL_ROOT", resource.getSpec().getLogging().getLevelRoot(), null),
            new EnvVar("LOGGING_PATTERN_CONSOLE", resource.getSpec().getLogging().getPatternConsole(), null),
            new EnvVar("JAVA_TOOL_OPTIONS", resource.getSpec().getJavaToolOptions(), null),
            new EnvVar("APPLICATION_BUCKET_NAME", resource.getSpec().getBucketName(), null),
            new EnvVar("CLOUD_AWS_REGION_STATIC", null, new EnvVarSourceBuilder().withNewSecretKeyRef("aws-region", "bulk-processor-secret", false).build()),
            new EnvVar("AWS_ACCESS_KEY", null, new EnvVarSourceBuilder().withNewSecretKeyRef("aws-access-key", "bulk-processor-secret", false).build()),
            new EnvVar("AWS_SECRET_KEY", null, new EnvVarSourceBuilder().withNewSecretKeyRef("aws-secret-key", "bulk-processor-secret", false).build())
        );
    }

    // Helper method to create resource requirements
    private ResourceRequirements createResourceRequirements(PhEeImporterRdbms resource) {
        return new ResourceRequirementsBuilder()
            .withLimits(new HashMap<String, Quantity>() {{
                put("cpu", new Quantity(resource.getSpec().getResources().getLimits().getCpu()));
                put("memory", new Quantity(resource.getSpec().getResources().getLimits().getMemory()));
            }})
            .withRequests(new HashMap<String, Quantity>() {{
                put("cpu", new Quantity(resource.getSpec().getResources().getRequests().getCpu()));
                put("memory", new Quantity(resource.getSpec().getResources().getRequests().getMemory()));
            }})
            .build();
    }
 

// Reconcile Secret
private void reconcileSecret(PhEeImporterRdbms resource) {
    String secretName = resource.getMetadata().getName() + "-secret";
    log.info("Reconciling Secret for resource: {}", resource.getMetadata().getName());
    Secret secret = createSecret(resource, secretName);
    log.info("Created Secret spec: {}", secret);

    Resource<Secret> secretResource = kubernetesClient.secrets()
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(secretName);

    if (secretResource.get() == null) {
        secretResource.create(secret);
        log.info("Created new Secret: {}", secretName);
    } else {
        secretResource.patch(secret);
        log.info("Updated existing Secret: {}", secretName);
    }
}

private Secret createSecret(PhEeImporterRdbms resource, String secretName) {
    log.info("Creating Secret spec for resource: {}", resource.getMetadata().getName());
    return new SecretBuilder()
            .withNewMetadata()
                .withName(secretName)
                .withNamespace(resource.getMetadata().getNamespace())
                .withOwnerReferences(createOwnerReferences(resource))
            .endMetadata()
            .addToData("database-password", Base64.getEncoder().encodeToString(resource.getSpec().getDatasource().getPassword().getBytes()))
            .build();
}

// Reconcile ServiceAccount
private void reconcileServiceAccount(PhEeImporterRdbms resource) {
    String saName = resource.getMetadata().getName() + "-sa";
    log.info("Reconciling ServiceAccount for resource: {}", resource.getMetadata().getName());
    ServiceAccount serviceAccount = createServiceAccount(resource, saName);
    log.info("Created ServiceAccount spec: {}", serviceAccount);

    Resource<ServiceAccount> serviceAccountResource = kubernetesClient.serviceAccounts()
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(saName);

    if (serviceAccountResource.get() == null) {
        serviceAccountResource.create(serviceAccount);
        log.info("Created new ServiceAccount: {}", saName);
    } else {
        serviceAccountResource.patch(serviceAccount);
        log.info("Updated existing ServiceAccount: {}", saName);
    }
}

private ServiceAccount createServiceAccount(PhEeImporterRdbms resource, String saName) {
    log.info("Creating ServiceAccount spec for resource: {}", resource.getMetadata().getName());
    return new ServiceAccountBuilder()
            .withNewMetadata()
                .withName(saName)
                .withNamespace(resource.getMetadata().getNamespace())
                .withOwnerReferences(createOwnerReferences(resource))
            .endMetadata()
            .build();
}

// Reconcile Role
private void reconcileRole(PhEeImporterRdbms resource) {
    String roleName = resource.getMetadata().getName() + "-role";
    log.info("Reconciling Role for resource: {}", resource.getMetadata().getName());
    Role role = createRole(resource, roleName);
    log.info("Created Role spec: {}", role);

    Resource<Role> roleResource = kubernetesClient.rbac().roles()
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(roleName);

    if (roleResource.get() == null) {
        roleResource.create(role);
        log.info("Created new Role: {}", roleName);
    } else {
        roleResource.patch(role);
        log.info("Updated existing Role: {}", roleName);
    }
}

private Role createRole(PhEeImporterRdbms resource, String roleName) {
    log.info("Creating Role spec for resource: {}", resource.getMetadata().getName());
    return new RoleBuilder()
            .withNewMetadata()
                .withName(roleName)
                .withNamespace(resource.getMetadata().getNamespace())
                .withOwnerReferences(createOwnerReferences(resource))
            .endMetadata()
            .addNewRule()
                .withApiGroups("")
                .withResources("pods", "services", "endpoints", "persistentvolumeclaims")
                .withVerbs("get", "list", "watch", "create", "update", "patch", "delete")
            .endRule()
            .build();
}

// Reconcile RoleBinding
private void reconcileRoleBinding(PhEeImporterRdbms resource) {
    String roleBindingName = resource.getMetadata().getName() + "-rolebinding";
    log.info("Reconciling RoleBinding for resource: {}", resource.getMetadata().getName());
    RoleBinding roleBinding = createRoleBinding(resource, roleBindingName);
    log.info("Created RoleBinding spec: {}", roleBinding);

    Resource<RoleBinding> roleBindingResource = kubernetesClient.rbac().roleBindings()
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(roleBindingName);

    if (roleBindingResource.get() == null) {
        roleBindingResource.create(roleBinding);
        log.info("Created new RoleBinding: {}", roleBindingName);
    } else {
        roleBindingResource.patch(roleBinding);
        log.info("Updated existing RoleBinding: {}", roleBindingName);
    }
}

private RoleBinding createRoleBinding(PhEeImporterRdbms resource, String roleBindingName) {
    log.info("Creating RoleBinding spec for resource: {}", resource.getMetadata().getName());
    return new RoleBindingBuilder()
            .withNewMetadata()
                .withName(roleBindingName)
                .withNamespace(resource.getMetadata().getNamespace())
                .withOwnerReferences(createOwnerReferences(resource))
            .endMetadata()
            .withSubjects(new SubjectBuilder()
                    .withKind("ServiceAccount")
                    .withName(resource.getMetadata().getName() + "-sa")
                    .withNamespace(resource.getMetadata().getNamespace())
                    .build())
            .withRoleRef(new RoleRefBuilder()
                    .withApiGroup("rbac.authorization.k8s.io")
                    .withKind("Role")
                    .withName(resource.getMetadata().getName() + "-role")
                    .build())
            .build();
}

// Reconcile ClusterRole
private void reconcileClusterRole(PhEeImporterRdbms resource) {
    String clusterRoleName = resource.getMetadata().getName() + "-clusterrole";
    log.info("Reconciling ClusterRole for resource: {}", resource.getMetadata().getName());
    ClusterRole clusterRole = createClusterRole(resource, clusterRoleName);
    log.info("Created ClusterRole spec: {}", clusterRole);

    Resource<ClusterRole> clusterRoleResource = kubernetesClient.rbac().clusterRoles()
            .withName(clusterRoleName);

    if (clusterRoleResource.get() == null) {
        clusterRoleResource.create(clusterRole);
        log.info("Created new ClusterRole: {}", clusterRoleName);
    } else {
        clusterRoleResource.patch(clusterRole);
        log.info("Updated existing ClusterRole: {}", clusterRoleName);
    }
}

private ClusterRole createClusterRole(PhEeImporterRdbms resource, String clusterRoleName) {
    log.info("Creating ClusterRole spec for resource: {}", resource.getMetadata().getName());
    return new ClusterRoleBuilder()
            .withNewMetadata()
                .withName(clusterRoleName)
                .withOwnerReferences(createOwnerReferences(resource))
            .endMetadata()
            .addNewRule()
                .withApiGroups("")
                .withResources("pods", "services", "endpoints", "persistentvolumeclaims")
                .withVerbs("get", "list", "watch", "create", "update", "patch", "delete")
            .endRule()
            .addNewRule()
                .withApiGroups("apps")
                .withResources("deployments")
                .withVerbs("get", "list", "watch", "create", "update", "patch", "delete")
            .endRule()
            .build();
}

// Reconcile ClusterRoleBinding
private void reconcileClusterRoleBinding(PhEeImporterRdbms resource) {
    String clusterRoleBindingName = resource.getMetadata().getName() + "-clusterrolebinding";
    log.info("Reconciling ClusterRoleBinding for resource: {}", resource.getMetadata().getName());
    ClusterRoleBinding clusterRoleBinding = createClusterRoleBinding(resource, clusterRoleBindingName);
    log.info("Created ClusterRoleBinding spec: {}", clusterRoleBinding);

    Resource<ClusterRoleBinding> clusterRoleBindingResource = kubernetesClient.rbac().clusterRoleBindings()
            .withName(clusterRoleBindingName);

    if (clusterRoleBindingResource.get() == null) {
        clusterRoleBindingResource.create(clusterRoleBinding);
        log.info("Created new ClusterRoleBinding: {}", clusterRoleBindingName);
    } else {
        clusterRoleBindingResource.patch(clusterRoleBinding);
        log.info("Updated existing ClusterRoleBinding: {}", clusterRoleBindingName);
    }
}

private ClusterRoleBinding createClusterRoleBinding(PhEeImporterRdbms resource, String clusterRoleBindingName) {
    log.info("Creating ClusterRoleBinding spec for resource: {}", resource.getMetadata().getName());
    return new ClusterRoleBindingBuilder()
            .withNewMetadata()
                .withName(clusterRoleBindingName)
                .withOwnerReferences(createOwnerReferences(resource))
            .endMetadata()
            .withSubjects(new SubjectBuilder()
                    .withKind("ServiceAccount")
                    .withName(resource.getMetadata().getName() + "-sa")
                    .withNamespace(resource.getMetadata().getNamespace())
                    .build())
            .withRoleRef(new RoleRefBuilder()
                    .withApiGroup("rbac.authorization.k8s.io")
                    .withKind("ClusterRole")
                    .withName(resource.getMetadata().getName() + "-clusterrole")
                    .build())
            .build();
}

    private List<OwnerReference> createOwnerReferences(PhEeImporterRdbms resource) {
        // clusterRole and clusterRoleBinding can not be deleted using owner reference
        return Collections.singletonList(
            new OwnerReferenceBuilder()
                .withApiVersion(resource.getApiVersion())
                .withKind(resource.getKind())
                .withName(resource.getMetadata().getName())
                .withUid(resource.getMetadata().getUid())
                .withController(true)
                .withBlockOwnerDeletion(true)
                .build()
        );
    }
}