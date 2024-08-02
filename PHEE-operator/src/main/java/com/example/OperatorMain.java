package com.example;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import com.example.PhEeImporterRdbmsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperatorMain {
    private static final Logger log = LoggerFactory.getLogger(OperatorMain.class);

    public static void main(String[] args) {
        log.info("Starting the Payment Hub EE Operator!");

        KubernetesClient client = new KubernetesClientBuilder().build(); // Moved outside try block
        Operator operator = new Operator(client, o -> o.withStopOnInformerErrorDuringStartup(false));
        log.info("Operator instance created.");

        try {
            Reconciler reconciler = new PhEeImporterRdbmsController(client); // Pass client to the controller
            operator.register(reconciler);
            log.info("Reconciler {} registered.", reconciler.getClass().getSimpleName());

            operator.start();
            log.info("Operator started successfully.");

            // Keep the operator running indefinitely
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Failed to start the operator due to an error: ", e);
        } finally {
            // Ensure the client is closed when the operator is stopped
            client.close();
        }
    }
}
