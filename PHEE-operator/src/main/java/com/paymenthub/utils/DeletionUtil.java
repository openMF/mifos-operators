package com.paymenthub.utils;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.model.Service; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.paymenthub.customresource.PaymentHubDeployment;
import java.util.List; 


/**
 * Utility class for handling the deletion of Kubernetes resources associated with a custom resource.
 */
public class DeletionUtil {

    private static final Logger log = LoggerFactory.getLogger(DeletionUtil.class);

    /**
     * Deletes all Kubernetes resources associated with the specified custom resource.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The custom resource whose associated resources are to be deleted.
     */
    public static void deleteResources(KubernetesClient kubernetesClient, PaymentHubDeployment resource) {
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

        deleteService(kubernetesClient, resource);
    }

    /**
     * Deletes all RBAC-related resources (ServiceAccount, Role, RoleBinding, ClusterRole, ClusterRoleBinding)
     * associated with the specified custom resource.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The custom resource whose RBAC-related resources are to be deleted.
     */
    public static void deleteRbacResources(KubernetesClient kubernetesClient, PaymentHubDeployment resource) {
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

    /**
     * Deletes the Secret associated with the specified custom resource.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The custom resource whose Secret is to be deleted.
     */
    public static void deleteSecretResources(KubernetesClient kubernetesClient, PaymentHubDeployment resource) {
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

    /**
     * Deletes the ConfigMap associated with the specified custom resource.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The custom resource whose ConfigMap is to be deleted.
     */
    public static void deleteConfigMapResources(KubernetesClient kubernetesClient, PaymentHubDeployment resource) {
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

    /**
     * Deletes the Ingress associated with the specified custom resource.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The custom resource whose Ingress is to be deleted.
     */
    public static void deleteIngressResources(KubernetesClient kubernetesClient, PaymentHubDeployment resource) {
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

    /**
     * Deletes the services specified in the custom resource from the Kubernetes cluster.
     * 
     * This method iterates over the list of services defined in the custom resource's spec and deletes each service
     * from the specified namespace if it exists. 
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.  
     * @param resource The custom resource containing the list of services to be deleted.  
     */
    public static void deleteService(KubernetesClient kubernetesClient, PaymentHubDeployment resource) {
        String namespace = resource.getMetadata().getNamespace();
        
        // Use fully qualified name for custom Service class
        List<com.paymenthub.customresource.PaymentHubDeploymentSpec.Service> services = resource.getSpec().getServices();
        
        if (services != null && !services.isEmpty()) {
            for (com.paymenthub.customresource.PaymentHubDeploymentSpec.Service service : services) {
                String serviceName = service.getName(); // Use fully qualified name for custom Service class
                
                // Check if the service exists in the namespace
                io.fabric8.kubernetes.api.model.Service existingService = kubernetesClient.services()
                        .inNamespace(namespace)
                        .withName(serviceName)
                        .get();

                if (existingService != null) {
                    // Delete the service if it exists
                    kubernetesClient.services()
                            .inNamespace(namespace)
                            .withName(serviceName)
                            .delete();
                    log.info("Deleted Service: {}", serviceName);
                } else {
                    log.warn("Service {} not found, skipping deletion.", serviceName);
                }
            }
        } else {
            log.warn("No services found in the spec, skipping service deletion.");
        }
    }
}
