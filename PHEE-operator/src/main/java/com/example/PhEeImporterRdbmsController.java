package com.example.operator;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.rbac.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder; 
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import java.util.concurrent.TimeUnit;
import com.example.PhEeImporterRdbms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;


@ControllerConfiguration
public class PhEeImporterRdbmsController implements Reconciler<PhEeImporterRdbms> {

    private static final Logger log = LoggerFactory.getLogger(PhEeImporterRdbmsController.class);

    @Inject
    private KubernetesClient kubernetesClient;

    @Override
    public UpdateControl<PhEeImporterRdbms> reconcile(PhEeImporterRdbms resource, Context<PhEeImporterRdbms> context) {
        log.info("Reconciling PhEeImporterRdbms: {}", resource.getMetadata().getName());

        // calling all the reconciliation methods and providing resource as an argument
        try {
            reconcileDeployment(resource); 
            reconcileSecret(resource);
            reconcileConfigMap(resource);
            reconcileServiceAccount(resource);
            reconcileClusterRole(resource);
            reconcileClusterRoleBinding(resource);
            reconcileRole(resource);
            reconcileRoleBinding(resource);

            return UpdateControl.noUpdate();
        } catch (Exception e) {
            log.error("Error during reconciliation", e);
            return UpdateControl.noUpdate();
        }
    }


    //Below are all the resource reconciliation methods
    private void reconcileDeployment(PhEeImporterRdbms resource) {
        Deployment deployment = createDeployment(resource);
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

    private void reconcileSecret(PhEeImporterRdbms resource) {
        Secret secret = createSecret(resource);
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

    private void reconcileConfigMap(PhEeImporterRdbms resource) {
        ConfigMap configMap = createConfigMap(resource);
        Resource<ConfigMap> configMapResource = kubernetesClient.configMaps()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName("ph-ee-config");

        if (configMapResource.get() == null) {
            configMapResource.create(configMap);
            log.info("Created new ConfigMap: {}", "ph-ee-config");
        } else {
            configMapResource.patch(configMap);
            log.info("Updated existing ConfigMap: {}", "ph-ee-config");
        }
    }

    private void reconcileServiceAccount(PhEeImporterRdbms resource) {
        ServiceAccount serviceAccount = createServiceAccount(resource);
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

    private void reconcileClusterRole(PhEeImporterRdbms resource) {
        ClusterRole clusterRole = createClusterRole(resource);
        Resource<ClusterRole> clusterRoleResource = kubernetesClient.rbac().clusterRoles()
                .withName("ph-ee-importer-rdbms-c-role");

        if (clusterRoleResource.get() == null) {
            clusterRoleResource.create(clusterRole);
            log.info("Created new ClusterRole: {}", "ph-ee-importer-rdbms-c-role");
        }
    }

    private void reconcileClusterRoleBinding(PhEeImporterRdbms resource) {
        ClusterRoleBinding clusterRoleBinding = createClusterRoleBinding(resource);
        Resource<ClusterRoleBinding> clusterRoleBindingResource = kubernetesClient.rbac().clusterRoleBindings()
                .withName("ph-ee-importer-rdbms-c-role-binding");

        if (clusterRoleBindingResource.get() == null) {
            clusterRoleBindingResource.create(clusterRoleBinding);
            log.info("Created new ClusterRoleBinding: {}", "ph-ee-importer-rdbms-c-role-binding");
        }
    }

    private void reconcileRole(PhEeImporterRdbms resource) {
        Role role = createRole(resource);
        Resource<Role> roleResource = kubernetesClient.rbac().roles()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName("ph-ee-importer-rdbms-role");

        if (roleResource.get() == null) {
            roleResource.create(role);
            log.info("Created new Role: {}", "ph-ee-importer-rdbms-role");
        }
    }

    private void reconcileRoleBinding(PhEeImporterRdbms resource) {
        RoleBinding roleBinding = createRoleBinding(resource);
        Resource<RoleBinding> roleBindingResource = kubernetesClient.rbac().roleBindings()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName("ph-ee-importer-rdbms-role-binding");

        if (roleBindingResource.get() == null) {
            roleBindingResource.create(roleBinding);
            log.info("Created new RoleBinding: {}", "ph-ee-importer-rdbms-role-binding");
        }
    }


    //Below are all the resource creation methods

     private Deployment createDeployment(PhEeImporterRdbms resource) {
        Deployment deployment = new Deployment();
        deployment.setMetadata(resource.getMetadata());

        DeploymentSpec deploymentSpec = new DeploymentSpec();
        deploymentSpec.setReplicas(resource.getSpec().getReplicas());

        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        PodSpec podSpec = new PodSpec();

        // Set up the container specifications based on the custom resource spec
        Container container = new Container();
        container.setName(resource.getMetadata().getName());
        container.setImage(resource.getSpec().getImage());

        // Environment variables
        List<EnvVar> envVars = new ArrayList<>();
        envVars.add(new EnvVar("SPRING_PROFILES_ACTIVE", resource.getSpec().getSpringProfilesActive(), null));
        envVars.add(new EnvVar("DATASOURCE_CORE_USERNAME", resource.getSpec().getDatasource().getUsername(), null));
        envVars.add(new EnvVar("DATASOURCE_CORE_PASSWORD", null, new EnvVarSourceBuilder().withNewSecretKeyRef("database-password", "importer-rdbms-secret", false).build()));
        envVars.add(new EnvVar("DATASOURCE_CORE_HOST", resource.getSpec().getDatasource().getHost(), null));
        envVars.add(new EnvVar("DATASOURCE_CORE_PORT", String.valueOf(resource.getSpec().getDatasource().getPort()), null));
        envVars.add(new EnvVar("DATASOURCE_CORE_SCHEMA", resource.getSpec().getDatasource().getSchema(), null));
        envVars.add(new EnvVar("LOGGING_LEVEL_ROOT", resource.getSpec().getLogging().getLevelRoot(), null));
        envVars.add(new EnvVar("LOGGING_PATTERN_CONSOLE", resource.getSpec().getLogging().getPatternConsole(), null));
        envVars.add(new EnvVar("JAVA_TOOL_OPTIONS", resource.getSpec().getJavaToolOptions(), null));
        envVars.add(new EnvVar("APPLICATION_BUCKET_NAME", resource.getSpec().getBucketName(), null));
        // Add AWS secrets
        envVars.add(new EnvVar("CLOUD_AWS_REGION_STATIC", null, new EnvVarSourceBuilder().withNewSecretKeyRef("aws-region", "bulk-processor-secret", false).build()));
        envVars.add(new EnvVar("AWS_ACCESS_KEY", null, new EnvVarSourceBuilder().withNewSecretKeyRef("aws-access-key", "bulk-processor-secret", false).build()));
        envVars.add(new EnvVar("AWS_SECRET_KEY", null, new EnvVarSourceBuilder().withNewSecretKeyRef("aws-secret-key", "bulk-processor-secret", false).build()));

        container.setEnv(envVars);

        // Resource limits and requests
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, Quantity> limits = new HashMap<>();
        limits.put("cpu", new Quantity(resource.getSpec().getResources().getLimits().getCpu()));
        limits.put("memory", new Quantity(resource.getSpec().getResources().getLimits().getMemory()));
        resourceRequirements.setLimits(limits);

        Map<String, Quantity> requests = new HashMap<>();
        requests.put("cpu", new Quantity(resource.getSpec().getResources().getRequests().getCpu()));
        requests.put("memory", new Quantity(resource.getSpec().getResources().getRequests().getMemory()));
        resourceRequirements.setRequests(requests);

        container.setResources(resourceRequirements);

        // Volume mounts
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setName("ph-ee-config");
        volumeMount.setMountPath("/config");
        container.setVolumeMounts(Collections.singletonList(volumeMount));

        podSpec.setContainers(Collections.singletonList(container));

        // Volumes
        Volume volume = new Volume();
        volume.setName("ph-ee-config");
        ConfigMapVolumeSource configMapVolumeSource = new ConfigMapVolumeSource();
        configMapVolumeSource.setName("ph-ee-config");
        volume.setConfigMap(configMapVolumeSource);
        podSpec.setVolumes(Collections.singletonList(volume));

        podTemplateSpec.setSpec(podSpec);
        deploymentSpec.setTemplate(podTemplateSpec);
        deployment.setSpec(deploymentSpec);

        return deployment;
    }

    private Secret createSecret(PhEeImporterRdbms resource) {
        return new SecretBuilder()
                .withNewMetadata()
                    .withName("importer-rdbms-secret")
                    .withNamespace(resource.getMetadata().getNamespace())
                .endMetadata()
                .addToData("database-password", Base64.getEncoder().encodeToString(resource.getSpec().getDatasource().getPassword().getBytes()))
                .build();
    }

    private ConfigMap createConfigMap(PhEeImporterRdbms resource) {
        return new ConfigMapBuilder()
                .withNewMetadata()
                    .withName("ph-ee-config")
                    .withNamespace(resource.getMetadata().getNamespace())
                .endMetadata()
                .addToData("config-file-name", "config-file-content") // Add actual config data
                .build();
    }

    private ServiceAccount createServiceAccount(PhEeImporterRdbms resource) {
        return new ServiceAccountBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms")
                    .withNamespace(resource.getMetadata().getNamespace())
                    .addToLabels("app", "ph-ee-importer-rdbms")
                    .addToLabels("chart", resource.getMetadata().getLabels().get("chart"))
                    .addToLabels("heritage", resource.getMetadata().getLabels().get("heritage"))
                    .addToLabels("release", resource.getMetadata().getLabels().get("release"))
                .endMetadata()
                .build();
    }

    private ClusterRole createClusterRole(PhEeImporterRdbms resource) {
        return new ClusterRoleBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-c-role")
                .endMetadata()
                .addNewRule()
                    .withApiGroups("")
                    .withResources("secrets")
                    .withVerbs("get", "watch", "list")
                .endRule()
                .build();
    }

    private ClusterRoleBinding createClusterRoleBinding(PhEeImporterRdbms resource) {
        return new ClusterRoleBindingBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-c-role-binding")
                .endMetadata()
                .withSubjects(new SubjectBuilder()
                        .withKind("ServiceAccount")
                        .withName("ph-ee-importer-rdbms")
                        .withNamespace(resource.getMetadata().getNamespace())
                        .build())
                .withRoleRef(new RoleRefBuilder()
                        .withKind("ClusterRole")
                        .withName("ph-ee-importer-rdbms-c-role")
                        .withApiGroup("rbac.authorization.k8s.io")
                        .build())
                .build();
    }

    private Role createRole(PhEeImporterRdbms resource) {
        return new RoleBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-role")
                    .withNamespace(resource.getMetadata().getNamespace())
                .endMetadata()
                .addNewRule()
                    .withApiGroups("")
                    .withResources("pods", "configmaps")
                    .withVerbs("get", "create", "update")
                .endRule()
                .build();
    }

    private RoleBinding createRoleBinding(PhEeImporterRdbms resource) {
        return new RoleBindingBuilder()
                .withNewMetadata()
                    .withName("ph-ee-importer-rdbms-role-binding")
                    .withNamespace(resource.getMetadata().getNamespace())
                .endMetadata()
                .withSubjects(new SubjectBuilder()
                        .withKind("ServiceAccount")
                        .withName("ph-ee-importer-rdbms")
                        .withNamespace(resource.getMetadata().getNamespace())
                        .build())
                .withRoleRef(new RoleRefBuilder()
                        .withKind("Role")
                        .withName("ph-ee-importer-rdbms-role")
                        .withApiGroup("rbac.authorization.k8s.io")
                        .build())
                .build();
    }
}
