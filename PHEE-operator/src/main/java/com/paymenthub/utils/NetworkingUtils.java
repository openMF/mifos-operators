package com.paymenthub.utils;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.networking.v1.*;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;

import com.paymenthub.customresource.PaymentHubDeployment;
import com.paymenthub.customresource.PaymentHubDeploymentSpec;
import com.paymenthub.utils.OwnerReferenceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkingUtils {

    private final KubernetesClient kubernetesClient;
    private static final Logger log = LoggerFactory.getLogger(NetworkingUtils.class);

    public NetworkingUtils(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Reconciles the Services for the given custom resource.
     * This includes creating, updating, or deleting services as necessary.
     * 
     * @param resource The custom resource specifying the service configuration.
     */
    public void reconcileServices(PaymentHubDeployment resource) {
        log.info("Reconciling Services for resource: {}", resource.getMetadata().getName());

        List<Service> desiredServices = createServices(resource);
        log.debug("Desired Service specs: {}", desiredServices.stream().map(Service::toString).collect(Collectors.joining(", ")));

        // Get the list of existing services in the namespace
        List<Service> existingServices = kubernetesClient.services()
                .inNamespace(resource.getMetadata().getNamespace())
                .list()
                .getItems()
                .stream()
                .filter(service -> desiredServices.stream().anyMatch(desiredService -> desiredService.getMetadata().getName().equals(service.getMetadata().getName())))
                .collect(Collectors.toList());

        for (Service desiredService : desiredServices) {
            Optional<Service> existingServiceOpt = existingServices.stream()
                    .filter(existingService -> existingService.getMetadata().getName().equals(desiredService.getMetadata().getName()))
                    .findFirst();

            if (existingServiceOpt.isPresent()) {
                Service existingService = existingServiceOpt.get();

                // Compare the specs and only update if necessary
                if (!areServicesEqual(existingService, desiredService)) {
                    kubernetesClient.services()
                            .inNamespace(resource.getMetadata().getNamespace())
                            .withName(existingService.getMetadata().getName())
                            .patch(desiredService); // Apply a patch instead of replacing the service
                    log.info("Updated existing Service: {}", desiredService.getMetadata().getName());
                } else {
                    log.info("Service is up-to-date: {}", desiredService.getMetadata().getName());
                }
            } else {
                // Create the service if it doesn't exist
                kubernetesClient.services()
                        .inNamespace(resource.getMetadata().getNamespace())
                        .create(desiredService);
                log.info("Created new Service: {}", desiredService.getMetadata().getName());
            }
        }
    }

    // Helper method to compare services based on significant fields
    private boolean areServicesEqual(Service existingService, Service desiredService) {
        // Compare important fields such as spec, ports, selectors, etc.
        return Objects.equals(existingService.getSpec().getPorts(), desiredService.getSpec().getPorts())
                && Objects.equals(existingService.getSpec().getSelector(), desiredService.getSpec().getSelector())
                && Objects.equals(existingService.getSpec().getType(), desiredService.getSpec().getType());
    }


    /**
     * Creates a list of Kubernetes Service objects based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the service configuration.
     * @return A list of created Service objects.
     */
    private List<Service> createServices(PaymentHubDeployment resource) {
        log.info("Creating Services spec for resource: {}", resource.getMetadata().getName());

        PaymentHubDeploymentSpec spec = resource.getSpec();
        List<PaymentHubDeploymentSpec.Service> serviceSpecs = spec.getServices();

        return serviceSpecs.stream()
                .map(serviceSpec -> {
                    List<ServicePort> ports = serviceSpec.getPorts().stream()
                            .map(portSpec -> new ServicePortBuilder()
                                    .withName(portSpec.getName())
                                    .withPort(portSpec.getPort())
                                    .withTargetPort(new IntOrString(portSpec.getTargetPort()))
                                    .withProtocol(portSpec.getProtocol())
                                    .build())
                            .collect(Collectors.toList());

                    Map<String, String> labels = serviceSpec.getLabels();
                    if (labels == null) {
                        labels = new HashMap<>();  
                    }

                    labels.putIfAbsent("app", resource.getMetadata().getName());
                    labels.putIfAbsent("app.kubernetes.io/managed-by", "ph-ee-operator");

                    return new ServiceBuilder()
                            .withNewMetadata()
                                .withName(serviceSpec.getName())
                                .withNamespace(resource.getMetadata().getNamespace())
                                .withLabels(labels)
                                .withAnnotations(serviceSpec.getAnnotations())
                                .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource)) 
                            .endMetadata()
                            .withNewSpec()
                                .withSelector(serviceSpec.getSelector() != null ? serviceSpec.getSelector() :
                                        Map.of("app", resource.getMetadata().getName()))
                                .withPorts(ports)
                                .withType(serviceSpec.getType() != null ? serviceSpec.getType() : "ClusterIP")
                                .withSessionAffinity(serviceSpec.getSessionAffinity())
                            .endSpec()
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Reconciles the Ingress for the given custom resource.
     * This includes creating or updating the Ingress as necessary.
     * 
     * @param resource The custom resource specifying the Ingress configuration.
     */
    public void reconcileIngress(PaymentHubDeployment resource) {
        String ingressName = resource.getMetadata().getName() + "-ingress";
        log.info("Reconciling Ingress for resource: {}", resource.getMetadata().getName());

        Ingress ingress = createIngress(resource, ingressName);
        log.debug("Created Ingress spec: {}", ingress);

        Resource<Ingress> ingressResource = kubernetesClient.network().v1().ingresses()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(ingressName);

        if (ingressResource.get() == null) {
            ingressResource.create(ingress);
            log.info("Created new Ingress: {}", ingressName);
        } else {
            ingressResource.patch(ingress);
            log.info("Updated existing Ingress: {}", ingressName);
        }
    }

    /**
     * Creates a Kubernetes Ingress object based on the custom resource specifications.
     * 
     * @param resource The custom resource specifying the Ingress configuration.
     * @param ingressName The name of the Ingress to be created or updated.
     * @return The created Ingress object.
     */
    private Ingress createIngress(PaymentHubDeployment resource, String ingressName) {
        log.info("Creating Ingress spec for resource: {}", resource.getMetadata().getName());

        List<IngressTLS> ingressTlsList = resource.getSpec().getIngress().getTls().stream()
                .map(tls -> new IngressTLS(tls.getHosts(), tls.getSecretName()))
                .collect(Collectors.toList());

        List<IngressRule> rules = resource.getSpec().getIngress().getRules().stream()
                .map(rule -> new IngressRuleBuilder()
                        .withHost(rule.getHost())
                        .withNewHttp()
                            .addAllToPaths(rule.getPaths().stream().map(customPath -> 
                                new HTTPIngressPathBuilder()
                                    .withPath(customPath.getPath())
                                    .withPathType(customPath.getPathType())
                                    .withNewBackend()
                                        .withNewService()
                                            .withName(customPath.getBackend().getService().getName())
                                            .withNewPort()
                                                .withNumber(customPath.getBackend().getService().getPort().getNumber())
                                            .endPort()
                                        .endService()
                                    .endBackend()
                                .build()
                            ).collect(Collectors.toList()))
                        .endHttp()
                        .build()
                ).collect(Collectors.toList());

        Map<String, String> labels = resource.getSpec().getIngress().getLabels();
        if (labels == null) {
            labels = new HashMap<>();  
        }

        // Add default labels if they are not provided in the CR
        labels.putIfAbsent("app", resource.getMetadata().getName());
        labels.putIfAbsent("app.kubernetes.io/managed-by", "ph-ee-operator");
        
        return new IngressBuilder()
                .withNewMetadata()
                    .withName(ingressName)
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withLabels(labels)
                    .withAnnotations(resource.getSpec().getIngress().getAnnotations())
                    .withOwnerReferences(OwnerReferenceUtils.createOwnerReferences(resource)) 
                .endMetadata()
                .withNewSpec()
                    .withIngressClassName(resource.getSpec().getIngress().getClassName())
                    .withTls(ingressTlsList)
                    .withRules(rules)
                .endSpec()
                .build();
    }

}