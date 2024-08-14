package com.example.utils;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.customresource.PhEeImporterRdbms;

public class ResourceDeletionUtil {

    private static final Logger log = LoggerFactory.getLogger(ResourceDeletionUtil.class);

    public static void deleteResources(KubernetesClient kubernetesClient, PhEeImporterRdbms resource) {
        String namespace = resource.getMetadata().getNamespace();
        String name = resource.getMetadata().getName();

        // Delete Deployment
        if (kubernetesClient.apps().deployments().inNamespace(namespace).withName(name).get() != null) {
            kubernetesClient.apps().deployments().inNamespace(namespace).withName(name).delete();
            log.info("Deleted Deployment: {}", name);
        } else {
            log.warn("Deployment {} not found, skipping deletion.", name);
        }

        // Delete all RBAC-related resources
        deleteRbacResources(kubernetesClient, resource);

        // Delete Secret
        deleteSecretResources(kubernetesClient, resource);

        // Delete ConfigMap
        deleteConfigMapResources(kubernetesClient, resource);

        // Delete Ingress
        deleteIngressResources(kubernetesClient, resource);
    }

    public static void deleteRbacResources(KubernetesClient kubernetesClient, PhEeImporterRdbms resource) {
        String namespace = resource.getMetadata().getNamespace();
        String name = resource.getMetadata().getName();
        String saName = name + "-sa";
        String roleName = name + "-role";
        String roleBindingName = name + "-rolebinding";
        String clusterRoleName = name + "-clusterrole";
        String clusterRoleBindingName = name + "-clusterrolebinding";

        // Delete ServiceAccount
        if (kubernetesClient.serviceAccounts().inNamespace(namespace).withName(saName).get() != null) {
            kubernetesClient.serviceAccounts().inNamespace(namespace).withName(saName).delete();
            log.info("Deleted ServiceAccount: {}", saName);
        } else {
            log.warn("ServiceAccount {} not found, skipping deletion.", saName);
        }

        // Delete Role
        if (kubernetesClient.rbac().roles().inNamespace(namespace).withName(roleName).get() != null) {
            kubernetesClient.rbac().roles().inNamespace(namespace).withName(roleName).delete();
            log.info("Deleted Role: {}", roleName);
        } else {
            log.warn("Role {} not found, skipping deletion.", roleName);
        }

        // Delete RoleBinding
        if (kubernetesClient.rbac().roleBindings().inNamespace(namespace).withName(roleBindingName).get() != null) {
            kubernetesClient.rbac().roleBindings().inNamespace(namespace).withName(roleBindingName).delete();
            log.info("Deleted RoleBinding: {}", roleBindingName);
        } else {
            log.warn("RoleBinding {} not found, skipping deletion.", roleBindingName);
        }

        // Delete ClusterRole
        if (kubernetesClient.rbac().clusterRoles().withName(clusterRoleName).get() != null) {
            kubernetesClient.rbac().clusterRoles().withName(clusterRoleName).delete();
            log.info("Deleted ClusterRole: {}", clusterRoleName);
        } else {
            log.warn("ClusterRole {} not found, skipping deletion.", clusterRoleName);
        }

        // Delete ClusterRoleBinding
        if (kubernetesClient.rbac().clusterRoleBindings().withName(clusterRoleBindingName).get() != null) {
            kubernetesClient.rbac().clusterRoleBindings().withName(clusterRoleBindingName).delete();
            log.info("Deleted ClusterRoleBinding: {}", clusterRoleBindingName);
        } else {
            log.warn("ClusterRoleBinding {} not found, skipping deletion.", clusterRoleBindingName);
        }
    }

    public static void deleteSecretResources(KubernetesClient kubernetesClient, PhEeImporterRdbms resource) {
        String namespace = resource.getMetadata().getNamespace();
        String name = resource.getMetadata().getName();
        String secretName = name + "-secret";

        // Delete Secret
        if (kubernetesClient.secrets().inNamespace(namespace).withName(secretName).get() != null) {
            kubernetesClient.secrets().inNamespace(namespace).withName(secretName).delete();
            log.info("Deleted Secret: {}", secretName);
        } else {
            log.warn("Secret {} not found, skipping deletion.", secretName);
        }
    }

    public static void deleteConfigMapResources(KubernetesClient kubernetesClient, PhEeImporterRdbms resource) {
        String namespace = resource.getMetadata().getNamespace();
        String name = resource.getMetadata().getName();
        String configMapName = name + "-configmap";

        // Delete ConfigMap
        if (kubernetesClient.configMaps().inNamespace(namespace).withName(configMapName).get() != null) {
            kubernetesClient.configMaps().inNamespace(namespace).withName(configMapName).delete();
            log.info("Deleted ConfigMap: {}", configMapName);
        } else {
            log.warn("ConfigMap {} not found, skipping deletion.", configMapName);
        }
    }

    public static void deleteIngressResources(KubernetesClient kubernetesClient, PhEeImporterRdbms resource) {
        String namespace = resource.getMetadata().getNamespace();
        String name = resource.getMetadata().getName();
        String ingressName = name + "-ingress";

        // Delete Ingress
        if (kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(ingressName).get() != null) {
            kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(ingressName).delete();
            log.info("Deleted Ingress: {}", ingressName);
        } else {
            log.warn("Ingress {} not found, skipping deletion.", ingressName);
        }
    }
}
