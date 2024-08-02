package com.example.customresource;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import com.example.customresource.PhEeImporterRdbmsSpec;
import com.example.customresource.PhEeImporterRdbmsStatus;
import io.fabric8.kubernetes.model.annotation.Plural;

@Version("v1")
@Group("my.custom.group")
@Plural("pheeimporterrdbmses")
public class PhEeImporterRdbms extends CustomResource<PhEeImporterRdbmsSpec, PhEeImporterRdbmsStatus> implements Namespaced {
}
