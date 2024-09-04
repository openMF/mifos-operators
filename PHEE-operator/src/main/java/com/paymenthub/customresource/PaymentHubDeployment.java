package com.paymenthub.customresource;

// Kubernetes API and model annotations
import io.fabric8.kubernetes.api.model.Namespaced;  
import io.fabric8.kubernetes.client.CustomResource;  
import io.fabric8.kubernetes.model.annotation.Group;  
import io.fabric8.kubernetes.model.annotation.Version;  
import io.fabric8.kubernetes.model.annotation.Plural;  

// Custom resource specification and status
import com.paymenthub.customresource.PaymentHubDeploymentSpec;  
import com.paymenthub.customresource.PaymentHubDeploymentStatus;  

/**
 * Custom resource definition for PaymentHubDeployment.
 * 
 * This class defines the custom resource for PaymentHubDeployment with its specification and status.
 * It extends the CustomResource class provided by the Fabric8 Kubernetes client and implements
 * the Namespaced interface to indicate that it is a namespaced resource.
 */
@Version("v1") // Specifies the API version
@Group("gazelle.mifos.io") // Specifies the API group
@Plural("paymenthubdeployments") // Specifies the plural name of the custom resource
public class PaymentHubDeployment extends CustomResource<PaymentHubDeploymentSpec, PaymentHubDeploymentStatus> implements Namespaced {
    // The class body can be empty or include additional methods or fields if needed
}
