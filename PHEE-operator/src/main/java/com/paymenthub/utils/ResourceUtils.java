package com.paymenthub.utils;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.Base64;
import java.util.logging.Logger;

import java.util.*;

import com.paymenthub.customresource.PaymentHubDeployment; 
import com.paymenthub.utils.OwnerReferenceUtils;

/**
 * Utility class for managing Kubernetes resources like ConfigMaps and Secrets.
 */
public class ResourceUtils {
    private static final Logger log = Logger.getLogger(ResourceUtils.class.getName());
    private final KubernetesClient kubernetesClient;

    public ResourceUtils(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Reconciles the ConfigMap for the given custom resource. Creates or updates the ConfigMap as necessary.
     * 
     * @param resource The custom resource containing the specifications for the ConfigMap.
     */
    public void reconcileConfigmap(PaymentHubDeployment resource) {
        String name = resource.getMetadata().getName() + "-configmap";
        log.info("Reconciling ConfigMap for resource: " + resource.getMetadata().getName());
        ConfigMap configMap = createConfigMap(resource, name);
        log.info("Created ConfigMap spec: " + configMap);

        Resource<ConfigMap> configMapResource = kubernetesClient.configMaps()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(name);

        if (configMapResource.get() == null) {
            configMapResource.create(configMap);
            log.info("Created new ConfigMap: " + name);
        } else {
            configMapResource.patch(configMap);
            log.info("Updated existing ConfigMap: " + name);
        }
    }

    /**
     * Creates a Kubernetes ConfigMap object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the ConfigMap configuration.
     * @param name The name of the ConfigMap.
     * @return The created ConfigMap object.
     */
    private ConfigMap createConfigMap(PaymentHubDeployment resource, String name) {
        log.info("Creating ConfigMap spec for resource: " + resource.getMetadata().getName());
        
        Map<String, String> data = new HashMap<>();
        data.put("configuration.properties", 
            "oauth.enabled false\n" +
            "oauth.basicAuth true\n" +
            "oauth.basicAuthToken Y2xpZW50Og==\n" +
            "oauth.serverUrl https://ops-bk.sandbox.mifos.io\n" +  
            "serverUrl https://ops.sandbox.mifos.io\n"  +
            "auth.enabled false\n" +
            "auth.tenant phdefault");

        return new ConfigMapBuilder()
                .withNewMetadata()
                    .withName(name)
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource))
                .endMetadata()
                .addToData(data) // Add the configuration properties data
                .build();
    }


    /**
     * Reconciles the Secret for the given custom resource. Creates or updates the Secret as necessary.
     * 
     * @param resource The custom resource containing the specifications for the Secret.
     */
    public void reconcileSecret(PaymentHubDeployment resource) {
        String secretName = resource.getMetadata().getName() + "-secret";
        log.info("Reconciling Secret for resource: " + resource.getMetadata().getName());
        Secret secret = createSecret(resource, secretName);
        log.info("Created Secret spec: " + secret);

        Resource<Secret> secretResource = kubernetesClient.secrets()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(secretName);

        if (secretResource.get() == null) {
            secretResource.create(secret);
            log.info("Created new Secret: " + secretName);
        } else {
            secretResource.patch(secret);
            log.info("Updated existing Secret: " + secretName);
        }
    }

    /**
     * Creates a Kubernetes Secret object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the Secret configuration.
     * @param secretName The name of the Secret.
     * @return The created Secret object.
     */
    private Secret createSecret(PaymentHubDeployment resource, String secretName) {
        log.info("Creating Secret spec for resource: " + resource.getMetadata().getName());

        // Initialize the SecretBuilder with common metadata
        SecretBuilder secretBuilder = new SecretBuilder()
                .withNewMetadata()
                    .withName(secretName)
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource))
                .endMetadata();

        // Add access_key, secret_key, and aws-region only if the deployment is for ph-ee-connector-bulk
        if ("ph-ee-connector-bulk".equals(resource.getMetadata().getName())) {
            secretBuilder
                .addToData("aws-access-key", Base64.getEncoder().encodeToString("root".getBytes()))
                .addToData("aws-secret-key", Base64.getEncoder().encodeToString("password".getBytes()))
                .addToData("aws-region", Base64.getEncoder().encodeToString("ap-south-1".getBytes()));

            return secretBuilder.build();
        }

        // Add api-key, project-id, and database-password only if the deployment is for message-gateway
        if ("message-gateway".equals(resource.getMetadata().getName())) {
            secretBuilder
                .addToData("api-key", Base64.getEncoder().encodeToString("<api-key>".getBytes()))
                .addToData("project-id", Base64.getEncoder().encodeToString("<project-id>".getBytes()))
                .addToData("database-password", Base64.getEncoder().encodeToString("password".getBytes()));

            return secretBuilder.build();
        }

        // Add database-password for all other deployments
        secretBuilder.addToData("database-password", Base64.getEncoder().encodeToString("password".getBytes()));

        return secretBuilder.build();
    }



}
