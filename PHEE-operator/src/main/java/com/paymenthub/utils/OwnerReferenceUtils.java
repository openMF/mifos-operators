package com.paymenthub.utils;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import com.paymenthub.customresource.PaymentHubDeployment;

import java.util.Collections;
import java.util.List;

public class OwnerReferenceUtils {

    /**
     * Creates a list of OwnerReferences for the given custom resource.
     * ClusterRole and ClusterRoleBinding cannot be deleted using owner references.
     *
     * @param resource The custom resource for which to create OwnerReferences.
     * @return A list containing one OwnerReference.
     */
    public static List<OwnerReference> createOwnerReferences(PaymentHubDeployment resource) {
        return Collections.singletonList(
            new OwnerReferenceBuilder()
                .withApiVersion(resource.getApiVersion())
                .withKind(resource.getKind())
                .withName(resource.getMetadata().getName())
                .withUid(resource.getMetadata().getUid())
                .withController(true)
                .withBlockOwnerDeletion(true)
                .build()
        );
    }
}
