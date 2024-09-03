package com.paymenthub;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import com.paymenthub.PaymentHubDeploymentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to start the Payment Hub EE Operator.
 * Sets up the Kubernetes client, initializes the operator, and starts the reconciliation process.
 */
public class OperatorMain {
    private static final Logger log = LoggerFactory.getLogger(OperatorMain.class);

    /**
     * Main method to run the Payment Hub EE Operator.
     * Initializes the Kubernetes client, creates an Operator instance, registers the reconciler,
     * and starts the operator. The operator will run indefinitely until stopped.
     * 
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        log.info("Starting the Payment Hub EE Operator!");

        // Initialize the Kubernetes client using the KubernetesClientBuilder
        KubernetesClient client = new KubernetesClientBuilder().build(); // Moved outside try block
        Operator operator = new Operator(client, o -> o.withStopOnInformerErrorDuringStartup(false));
        log.info("Operator instance created.");

        try {
            // Create and register the reconciler for the operator
            Reconciler reconciler = new PaymentHubDeploymentController(client); // Pass client to the controller
            operator.register(reconciler);
            log.info("Reconciler {} registered.", reconciler.getClass().getSimpleName());

            // Start the operator
            operator.start();
            log.info("Operator started successfully.");

            // Keep the operator running indefinitely
            Thread.currentThread().join();
        } catch (Exception e) {
            // Log any errors encountered during the operator startup
            log.error("Failed to start the operator due to an error: ", e);
        } finally {
            // Ensure the client is closed when the operator is stopped
            client.close();
        }
    }
}
