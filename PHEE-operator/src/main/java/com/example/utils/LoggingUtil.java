package com.example.utils;

import com.example.customresource.PhEeImporterRdbms;
import com.example.customresource.PhEeImporterRdbmsSpec;
import com.example.customresource.PhEeImporterRdbmsSpec.Datasource;
import com.example.customresource.PhEeImporterRdbmsSpec.Resources;
import com.example.customresource.PhEeImporterRdbmsSpec.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUtil {

    private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);

    public static void logResourceDetails(PhEeImporterRdbms resource) {
        PhEeImporterRdbmsSpec spec = resource.getSpec();
        Integer replicas = spec.getReplicas();
        String image = spec.getImage();
        String springProfilesActive = spec.getSpringProfilesActive();
        Datasource datasource = spec.getDatasource();
        Resources resources = spec.getResources();
        Logging loggingConfig = spec.getLogging();
        String javaToolOptions = spec.getJavaToolOptions();
        String bucketName = spec.getBucketName();

        log.info("Reconciling PhEeImporterRdbms: {}", resource.getMetadata().getName());
        log.info("Desired state - Replicas: {}, Image: {}, Spring Profiles Active: {}, Java Tool Options: {}, Bucket Name: {}",
                replicas, image, springProfilesActive, javaToolOptions, bucketName);

        if (datasource != null) {
            log.info("Datasource Config - Username: {}, Host: {}, Port: {}, Schema: {}",
                    datasource.getUsername(), datasource.getHost(), datasource.getPort(), datasource.getSchema());
        } else {
            log.warn("No Datasource Config specified in the Spec.");
        }

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

        if (loggingConfig != null) {
            log.info("Logging Config - Level Root: {}, Pattern Console: {}",
                    loggingConfig.getLevelRoot(), loggingConfig.getPatternConsole());
        } else {
            log.warn("No Logging Config specified in the Spec.");
        }
    }

    public static void logError(String message, Exception e) {
        log.error(message, e);
    }
}
