package com.example;
 
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import com.example.operator.PhEeImporterRdbmsController;

public class OperatorMain {
    public static void main(String[] args) {
        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            Operator operator = new Operator(client, o -> o.withStopOnInformerErrorDuringStartup(false));
            Reconciler reconciler = new PhEeImporterRdbmsController();
            operator.register(reconciler);
            operator.start();
        }
    }
}
