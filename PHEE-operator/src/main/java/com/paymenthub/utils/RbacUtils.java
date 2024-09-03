package com.paymenthub.utils;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.rbac.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

import com.paymenthub.customresource.PaymentHubDeployment; 
import com.paymenthub.utils.OwnerReferenceUtils;

public class RbacUtils {

    private static final Logger log = LoggerFactory.getLogger(RbacUtils.class);
    private final KubernetesClient kubernetesClient;

    public RbacUtils(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Reconciles the ServiceAccount for the given custom resource.
     * This method ensures that the ServiceAccount exists and is up-to-date based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the ServiceAccount configuration.
     */
    public void reconcileServiceAccount(PaymentHubDeployment resource) {
        String saName = resource.getMetadata().getName() + "-sa";
        log.info("Reconciling ServiceAccount for resource: {}", resource.getMetadata().getName());
        ServiceAccount serviceAccount = createServiceAccount(resource, saName);
        log.debug("Created ServiceAccount spec: {}", serviceAccount);

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

    /**
     * Creates a Kubernetes ServiceAccount object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the ServiceAccount configuration.
     * @param saName The name to assign to the ServiceAccount.
     * @return The created ServiceAccount object.
     */
    private ServiceAccount createServiceAccount(PaymentHubDeployment resource, String saName) {
        log.debug("Creating ServiceAccount spec for resource: {}", resource.getMetadata().getName());
        return new ServiceAccountBuilder()
                .withNewMetadata()
                    .withName(saName)
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource)) 
                .endMetadata()
                .build();
    }

    /**
     * Reconciles the Role for the given custom resource.
     * This method ensures that the Role exists and is up-to-date based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the Role configuration.
     */
    public void reconcileRole(PaymentHubDeployment resource) {
        String roleName = resource.getMetadata().getName() + "-role";
        log.info("Reconciling Role for resource: {}", resource.getMetadata().getName());
        Role role = createRole(resource, roleName);
        log.debug("Created Role spec: {}", role);

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

    /**
     * Creates a Kubernetes Role object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the Role configuration.
     * @param roleName The name to assign to the Role.
     * @return The created Role object.
     */
    private Role createRole(PaymentHubDeployment resource, String roleName) {
        log.debug("Creating Role spec for resource: {}", resource.getMetadata().getName());
        return new RoleBuilder()
                .withNewMetadata()
                    .withName(roleName)
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource)) 
                .endMetadata()
                .addNewRule()
                    .withApiGroups("")
                    .withResources("pods", "services", "endpoints", "persistentvolumeclaims")
                    .withVerbs("get", "list", "watch", "create", "update", "patch", "delete")
                .endRule()
                .build();
    }

    /**
     * Reconciles the RoleBinding for the given custom resource.
     * This method ensures that the RoleBinding exists and is up-to-date based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the RoleBinding configuration.
     */
    public void reconcileRoleBinding(PaymentHubDeployment resource) {
        String roleBindingName = resource.getMetadata().getName() + "-rolebinding";
        log.info("Reconciling RoleBinding for resource: {}", resource.getMetadata().getName());
        RoleBinding roleBinding = createRoleBinding(resource, roleBindingName);
        log.debug("Created RoleBinding spec: {}", roleBinding);

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

    /**
     * Creates a Kubernetes RoleBinding object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the RoleBinding configuration.
     * @param roleBindingName The name to assign to the RoleBinding.
     * @return The created RoleBinding object.
     */
    private RoleBinding createRoleBinding(PaymentHubDeployment resource, String roleBindingName) {
        log.debug("Creating RoleBinding spec for resource: {}", resource.getMetadata().getName());
        return new RoleBindingBuilder()
                .withNewMetadata()
                    .withName(roleBindingName)
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource)) 
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

    /**
     * Reconciles the ClusterRole for the given custom resource.
     * This method ensures that the ClusterRole exists and is up-to-date based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the ClusterRole configuration.
     */
    public void reconcileClusterRole(PaymentHubDeployment resource) {
        String clusterRoleName = resource.getMetadata().getName() + "-clusterrole";
        log.info("Reconciling ClusterRole for resource: {}", resource.getMetadata().getName());
        ClusterRole clusterRole = createClusterRole(resource, clusterRoleName);
        log.debug("Created ClusterRole spec: {}", clusterRole);

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

    /**
     * Creates a Kubernetes ClusterRole object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the ClusterRole configuration.
     * @param clusterRoleName The name to assign to the ClusterRole.
     * @return The created ClusterRole object.
     */
    private ClusterRole createClusterRole(PaymentHubDeployment resource, String clusterRoleName) {
        log.debug("Creating ClusterRole spec for resource: {}", resource.getMetadata().getName());
        return new ClusterRoleBuilder()
                .withNewMetadata()
                    .withName(clusterRoleName)
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource)) 
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

    /**
     * Reconciles the ClusterRoleBinding for the given custom resource.
     * This method ensures that the ClusterRoleBinding exists and is up-to-date based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the ClusterRoleBinding configuration.
     */
    public void reconcileClusterRoleBinding(PaymentHubDeployment resource) {
        String clusterRoleBindingName = resource.getMetadata().getName() + "-clusterrolebinding";
        log.info("Reconciling ClusterRoleBinding for resource: {}", resource.getMetadata().getName());
        ClusterRoleBinding clusterRoleBinding = createClusterRoleBinding(resource, clusterRoleBindingName);
        log.debug("Created ClusterRoleBinding spec: {}", clusterRoleBinding);

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

    /**
     * Creates a Kubernetes ClusterRoleBinding object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the ClusterRoleBinding configuration.
     * @param clusterRoleBindingName The name to assign to the ClusterRoleBinding.
     * @return The created ClusterRoleBinding object.
     */
    private ClusterRoleBinding createClusterRoleBinding(PaymentHubDeployment resource, String clusterRoleBindingName) {
        log.debug("Creating ClusterRoleBinding spec for resource: {}", resource.getMetadata().getName());
        return new ClusterRoleBindingBuilder()
                .withNewMetadata()
                    .withName(clusterRoleBindingName)
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource)) 
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

}
