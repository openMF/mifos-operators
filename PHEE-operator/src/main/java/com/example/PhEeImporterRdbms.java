package com.example;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import com.example.operator.PhEeImporterRdbmsSpec;
 
public class PhEeImporterRdbms extends CustomResource<PhEeImporterRdbmsSpec, Void> implements Namespaced {
}
