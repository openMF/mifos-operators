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
        LoggingUtil.logResourceDetails(resource);

        try { 

            reconcileServiceAccount(resource);
            reconcileSecret(resource); 
            reconcileClusterRole(resource);
            reconcileClusterRoleBinding(resource);
            reconcileRole(resource);
            reconcileRoleBinding(resource); 
            reconcileDeployment(resource);

            return StatusUpdateUtil.updateStatus(kubernetesClient, resource, resource.getSpec().getReplicas(), resource.getSpec().getImage(), true, "");

        } catch (Exception e) {
            LoggingUtil.logError("Error during reconciliation", e);
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



    private void reconcileSecret(PhEeImporterRdbms resource) {
        log.info("Reconciling Secret for resource: {}", resource.getMetadata().getName());
        Secret secret = createSecret(resource);
        log.info("Created Secret spec: {}", secret);

        Resource<Secret> secretResource = kubernetesClient.secrets()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName("importer-rdbms-secret");

        if (secretResource.get() == null) {
            secretResource.create(secret);
            log.info("Created new Secret: {}", "importer-rdbms-secret");
        } else {
            secretResource.patch(secret);
            log.info("Updated existing Secret: {}", "importer-rdbms-secret");
        }
    }

    private Secret createSecret(PhEeImporterRdbms resource) {
        log.info("Creating Secret spec for resource: {}", resource.getMetadata().getName());
        return new SecretBuilder()
                .withNewMetadata()
                    .withName("importer-rdbms-secret")
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(createOwnerReferences(resource))
                .endMetadata()
                .addToData("database-password", Base64.getEncoder().encodeToString(resource.getSpec().getDatasource().getPassword().getBytes()))
                .build();
    }


    private void reconcileServiceAccount(PhEeImporterRdbms resource) {
        log.info("Reconciling ServiceAccount for resource: {}", resource.getMetadata().getName());
        ServiceAccount serviceAccount = createServiceAccount(resource);
        log.info("Created ServiceAccount spec: {}", serviceAccount);

        Resource<ServiceAccount> serviceAccountResource = kubernetesClient.serviceAccounts()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName("ph-ee-importer-rdbms");

        if (serviceAccountResource.get() == null) {
            serviceAccountResource.create(serviceAccount);
            log.info("Created new ServiceAccount: {}", "ph-ee-importer-rdbms");
        } else {
            serviceAccountResource.patch(serviceAccount);
            log.info("Updated existing ServiceAccount: {}", "ph-ee-importer-rdbms");
        }
    }

    private ServiceAccount createServiceAccount(PhEeImporterRdbms resource) {
        log.info("Creating ServiceAccount spec for resource: {}", resource.getMetadata().getName());
        return new ServiceAccountBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms")
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(createOwnerReferences(resource))
                .endMetadata()
                .build();
    }



    private void reconcileRole(PhEeImporterRdbms resource) {
        log.info("Reconciling Role for resource: {}", resource.getMetadata().getName());
        Role role = createRole(resource);
        log.info("Created Role spec: {}", role);

        Resource<Role> roleResource = kubernetesClient.rbac().roles()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName("ph-ee-importer-rdbms-role");

        if (roleResource.get() == null) {
            roleResource.create(role);
            log.info("Created new Role: {}", "ph-ee-importer-rdbms-role");
        } else {
            roleResource.patch(role);
            log.info("Updated existing Role: {}", "ph-ee-importer-rdbms-role");
        }
    }

    private Role createRole(PhEeImporterRdbms resource) {
        log.info("Creating Role spec for resource: {}", resource.getMetadata().getName());
        return new RoleBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-role")
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



    private void reconcileRoleBinding(PhEeImporterRdbms resource) {
        log.info("Reconciling RoleBinding for resource: {}", resource.getMetadata().getName());
        RoleBinding roleBinding = createRoleBinding(resource);
        log.info("Created RoleBinding spec: {}", roleBinding);

        Resource<RoleBinding> roleBindingResource = kubernetesClient.rbac().roleBindings()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName("ph-ee-importer-rdbms-rolebinding");

        if (roleBindingResource.get() == null) {
            roleBindingResource.create(roleBinding);
            log.info("Created new RoleBinding: {}", "ph-ee-importer-rdbms-rolebinding");
        } else {
            roleBindingResource.patch(roleBinding);
            log.info("Updated existing RoleBinding: {}", "ph-ee-importer-rdbms-rolebinding");
        }
    }

    private RoleBinding createRoleBinding(PhEeImporterRdbms resource) {
        log.info("Creating RoleBinding spec for resource: {}", resource.getMetadata().getName());
        return new RoleBindingBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-rolebinding")
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(createOwnerReferences(resource))
                .endMetadata()
                .withSubjects(new SubjectBuilder()
                        .withKind("ServiceAccount")
                        .withName("ph-ee-importer-rdbms")
                        .withNamespace(resource.getMetadata().getNamespace())
                        .build())
                .withRoleRef(new RoleRefBuilder()
                        .withApiGroup("rbac.authorization.k8s.io")
                        .withKind("Role")
                        .withName("ph-ee-importer-rdbms-role")
                        .build())
                .build();
    }



    private void reconcileClusterRole(PhEeImporterRdbms resource) {
        log.info("Reconciling ClusterRole for resource: {}", resource.getMetadata().getName());
        ClusterRole clusterRole = createClusterRole(resource);
        log.info("Created ClusterRole spec: {}", clusterRole);

        Resource<ClusterRole> clusterRoleResource = kubernetesClient.rbac().clusterRoles()
                .withName("ph-ee-importer-rdbms-clusterrole");

        if (clusterRoleResource.get() == null) {
            clusterRoleResource.create(clusterRole);
            log.info("Created new ClusterRole: {}", "ph-ee-importer-rdbms-clusterrole");
        } else {
            clusterRoleResource.patch(clusterRole);
            log.info("Updated existing ClusterRole: {}", "ph-ee-importer-rdbms-clusterrole");
        }
    }

    private ClusterRole createClusterRole(PhEeImporterRdbms resource) {
        log.info("Creating ClusterRole spec for resource: {}", resource.getMetadata().getName());
        return new ClusterRoleBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-clusterrole")
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



    private void reconcileClusterRoleBinding(PhEeImporterRdbms resource) {
        log.info("Reconciling ClusterRoleBinding for resource: {}", resource.getMetadata().getName());
        ClusterRoleBinding clusterRoleBinding = createClusterRoleBinding(resource);
        log.info("Created ClusterRoleBinding spec: {}", clusterRoleBinding);

        Resource<ClusterRoleBinding> clusterRoleBindingResource = kubernetesClient.rbac().clusterRoleBindings()
                .withName("ph-ee-importer-rdbms-clusterrolebinding");

        if (clusterRoleBindingResource.get() == null) {
            clusterRoleBindingResource.create(clusterRoleBinding);
            log.info("Created new ClusterRoleBinding: {}", "ph-ee-importer-rdbms-clusterrolebinding");
        } else {
            clusterRoleBindingResource.patch(clusterRoleBinding);
            log.info("Updated existing ClusterRoleBinding: {}", "ph-ee-importer-rdbms-clusterrolebinding");
        }
    }

    private ClusterRoleBinding createClusterRoleBinding(PhEeImporterRdbms resource) {
        log.info("Creating ClusterRoleBinding spec for resource: {}", resource.getMetadata().getName());
        return new ClusterRoleBindingBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-clusterrolebinding")
                    .withOwnerReferences(createOwnerReferences(resource))
                .endMetadata()
                .withSubjects(new SubjectBuilder()
                        .withKind("ServiceAccount")
                        .withName("ph-ee-importer-rdbms")
                        .withNamespace(resource.getMetadata().getNamespace())
                        .build())
                .withRoleRef(new RoleRefBuilder()
                        .withApiGroup("rbac.authorization.k8s.io")
                        .withKind("ClusterRole")
                        .withName("ph-ee-importer-rdbms-clusterrole")
                        .build())
                .build();
    }



    private List<OwnerReference> createOwnerReferences(PhEeImporterRdbms resource) {
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