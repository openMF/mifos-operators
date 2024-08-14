package com.example.utils;  

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.HTTPGetActionBuilder;
import com.example.customresource.PhEeImporterRdbms;
import com.example.customresource.PhEeImporterRdbmsSpec;

public class ProbeUtils {

    public static Probe createProbe(PhEeImporterRdbms resource, String probeType) {
        PhEeImporterRdbmsSpec.Probe probeSpec = null;
        
        if ("liveness".equals(probeType)) {
            probeSpec = resource.getSpec().getLivenessProbe();
        } else if ("readiness".equals(probeType)) {
            probeSpec = resource.getSpec().getReadinessProbe();
        }

        if (probeSpec == null) {
            return null;
        }
        
        return new ProbeBuilder()
            .withHttpGet(new HTTPGetActionBuilder()
                .withPath(probeSpec.getPath())
                .withPort(new IntOrString(probeSpec.getPort()))
                .build())
            .withInitialDelaySeconds(probeSpec.getInitialDelaySeconds())
            .withPeriodSeconds(probeSpec.getPeriodSeconds())
            .withFailureThreshold(probeSpec.getFailureThreshold())
            .withTimeoutSeconds(probeSpec.getTimeoutSeconds())
            .build();
    }
}
