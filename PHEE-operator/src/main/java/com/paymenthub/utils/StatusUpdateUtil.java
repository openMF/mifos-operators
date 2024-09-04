package com.paymenthub.utils;

import com.paymenthub.customresource.PaymentHubDeployment;
import com.paymenthub.customresource.PaymentHubDeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for updating the status of the PaymentHubDeployment custom resource.
 */
public class StatusUpdateUtil {

    private static final Logger log = LoggerFactory.getLogger(StatusUpdateUtil.class);

    /**
     * Updates the status of the given PaymentHubDeployment resource with the provided details.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The PaymentHubDeployment custom resource to update.
     * @param replicas The number of available replicas.
     * @param image The last applied image.
     * @param isReady The readiness status of the resource.
     * @param errorMessage An optional error message if applicable.
     * @return An UpdateControl object indicating whether the status was successfully updated or not.
     */
    public static UpdateControl<PaymentHubDeployment> updateStatus(KubernetesClient kubernetesClient, PaymentHubDeployment resource, Integer replicas, String image, boolean isReady, String errorMessage) {
        PaymentHubDeploymentStatus status = new PaymentHubDeploymentStatus();
        status.setAvailableReplicas(replicas);
        status.setLastAppliedImage(image);
        status.setReady(isReady);
        status.setErrorMessage(errorMessage);

        resource.setStatus(status);
        log.info("Updating Status - Available Replicas: {}, Last Applied Image: {}, Ready: {}, Error Message: {}",
                status.getAvailableReplicas(), status.getLastAppliedImage(), status.isReady(), status.getErrorMessage());

        if (kubernetesClient.resources(PaymentHubDeployment.class)
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName())
                .get() != null) {
            return UpdateControl.patchStatus(resource);
        } else {
            log.error("Resource not found for status update: {}", resource.getMetadata().getName());
            return UpdateControl.noUpdate();
        }
    }

    /**
     * Updates the status of the given PaymentHubDeployment resource to indicate an error during reconciliation.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The PaymentHubDeployment custom resource to update.
     * @param image The last applied image.
     * @param e The exception that occurred during reconciliation.
     * @return An UpdateControl object indicating whether the status was successfully updated or not.
     */
    public static UpdateControl<PaymentHubDeployment> updateErrorStatus(KubernetesClient kubernetesClient, PaymentHubDeployment resource, String image, Exception e) {
        return updateStatus(kubernetesClient, resource, 0, image, false, "Error during reconciliation: " + e.getMessage());
    }

    /**
     * Updates the status of the given PaymentHubDeployment resource to indicate that the resource is disabled.
     * 
     * @param kubernetesClient The Kubernetes client used to interact with the Kubernetes API.
     * @param resource The PaymentHubDeployment custom resource to update.
     * @return An UpdateControl object indicating whether the status was successfully updated or not.
     */
    public static UpdateControl<PaymentHubDeployment> updateDisabledStatus(KubernetesClient kubernetesClient, PaymentHubDeployment resource) {
        PaymentHubDeploymentStatus status = new PaymentHubDeploymentStatus();
        status.setAvailableReplicas(0);
        status.setLastAppliedImage(resource.getSpec().getImage());
        status.setReady(false);
        status.setErrorMessage("Resource is disabled and not created.");

        resource.setStatus(status);
        log.info("Resource {} is disabled. Setting status - Available Replicas: 0, Last Applied Image: {}, Ready: false, Error Message: Resource is disabled and not created.",
                resource.getMetadata().getName());

        if (kubernetesClient.resources(PaymentHubDeployment.class)
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName())
                .get() != null) {
            return UpdateControl.patchStatus(resource);
        } else {
            log.error("Resource not found for status update: {}", resource.getMetadata().getName());
            return UpdateControl.noUpdate();
        }
    }
}
