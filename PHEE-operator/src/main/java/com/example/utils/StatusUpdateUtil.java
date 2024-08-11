package com.example.utils;

import com.example.customresource.PhEeImporterRdbms;
import com.example.customresource.PhEeImporterRdbmsStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUpdateUtil {

    private static final Logger log = LoggerFactory.getLogger(StatusUpdateUtil.class);

    public static UpdateControl<PhEeImporterRdbms> updateStatus(KubernetesClient kubernetesClient, PhEeImporterRdbms resource, Integer replicas, String image, boolean isReady, String errorMessage) {
        PhEeImporterRdbmsStatus status = new PhEeImporterRdbmsStatus();
        status.setAvailableReplicas(replicas);
        status.setLastAppliedImage(image);
        status.setReady(isReady);
        status.setErrorMessage(errorMessage);

        resource.setStatus(status);
        log.info("Updating Status - Available Replicas: {}, Last Applied Image: {}, Ready: {}, Error Message: {}",
                status.getAvailableReplicas(), status.getLastAppliedImage(), status.isReady(), status.getErrorMessage());

        if (kubernetesClient.resources(PhEeImporterRdbms.class)
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName())
                .get() != null) {
            return UpdateControl.patchStatus(resource);
        } else {
            log.error("Resource not found for status update: {}", resource.getMetadata().getName());
            return UpdateControl.noUpdate();
        }
    }

    public static UpdateControl<PhEeImporterRdbms> updateErrorStatus(KubernetesClient kubernetesClient, PhEeImporterRdbms resource, String image, Exception e) {
        return updateStatus(kubernetesClient, resource, 0, image, false, "Error during reconciliation: " + e.getMessage());
    }

    public static UpdateControl<PhEeImporterRdbms> updateDisabledStatus(KubernetesClient kubernetesClient, PhEeImporterRdbms resource) {
        PhEeImporterRdbmsStatus status = new PhEeImporterRdbmsStatus();
        status.setAvailableReplicas(0);
        status.setLastAppliedImage(resource.getSpec().getImage());
        status.setReady(false);
        status.setErrorMessage("Resource is disabled and not created.");

        resource.setStatus(status);
        log.info("Resource {} is disabled. Setting status - Available Replicas: 0, Last Applied Image: {}, Ready: false, Error Message: Resource is disabled and not created.",
                resource.getMetadata().getName());

        if (kubernetesClient.resources(PhEeImporterRdbms.class)
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
