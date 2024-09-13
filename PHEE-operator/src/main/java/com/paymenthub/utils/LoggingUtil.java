package com.paymenthub.utils;

import com.paymenthub.customresource.PaymentHubDeployment;
import com.paymenthub.customresource.PaymentHubDeploymentSpec; 
import com.paymenthub.customresource.PaymentHubDeploymentSpec.Resources; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging details of the PaymentHubDeployment custom resource.
 */
public class LoggingUtil {

    private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);

    /**
     * Logs the details of the given PaymentHubDeployment custom resource.
     * This includes general information, datasource configuration, resource limits and requests, and logging configuration.
     * 
     * @param resource The PaymentHubDeployment custom resource whose details are to be logged.
     */
    public static void logResourceDetails(PaymentHubDeployment resource) {
        PaymentHubDeploymentSpec spec = resource.getSpec();
        Integer replicas = spec.getReplicas();
        String image = spec.getImage();
        Resources resources = spec.getResources();

        // Log the name and desired state of the custom resource
        log.info("Reconciling PaymentHubDeployment: {}", resource.getMetadata().getName());
        log.info("Desired state - Replicas: {}, Image: {}",
                replicas, image);


        // Log resource limits and requests if available
        if (resources != null) {
            if (resources.getLimits() != null) {
                log.info("Resource Limits - CPU: {}, Memory: {}",
                        resources.getLimits().getCpu(), resources.getLimits().getMemory());
            } else {
                log.warn("No Resource Limits specified.");
            }

            if (resources.getRequests() != null) {
                log.info("Resource Requests - CPU: {}, Memory: {}",
                        resources.getRequests().getCpu(), resources.getRequests().getMemory());
            } else {
                log.warn("No Resource Requests specified.");
            }
        } else {
            log.warn("No Resource Config specified in the Spec.");
        }
    }

    /**
     * Logs an error message along with an exception stack trace.
     * 
     * @param message The error message to log.
     * @param e The exception whose stack trace will be logged.
     */
    public static void logError(String message, Exception e) {
        log.error(message, e);
    }
}
