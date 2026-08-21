package com.azelart.gracefulshutdow.service;

import org.apache.catalina.connector.Connector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Perform a tomcat shutdown.
 */
public class TomcatShutdown implements Shutdown, TomcatConnectorCustomizer {

    /**
     * Implementation of a Coyote connector.
     */
    private volatile Connector connector;

    /**
     * Logger from common util.
     */
    private static final Log logger = LogFactory.getLog(TomcatShutdown.class);

    /**
     * Perform a pause on the server.
     * @throw InterruptedException if we have an interruption
     */
    public void pause() {
         // Used to properly handle the work queue.
        final Executor executor = connector.getProtocolHandler().getExecutor();

        // Start the pause.
        connector.pause();

        // Close the server socket to make sure new connections are refused
        // instead of stalling in the accept queue while the endpoint is paused.
        closeServerSocket();
    }

    /**
     * Perform a shutdown
     * @param delay is delay to force is the delay before perform a force shutdown
     * @throws InterruptedException if we have an exception
     */
    public void shutdown(Integer delay) throws InterruptedException {
        // Used to properly handle the work queue.
        final Executor executor = connector.getProtocolHandler().getExecutor();
        final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

        /*
         * Initiates an orderly shutdown in which previously submitted
         * tasks are executed, but no new tasks will be accepted.
         * Invocation has no additional effect if already shut down.
         */
        threadPoolExecutor.shutdown();

        // We wait after the end of the current requests
        if(!threadPoolExecutor.awaitTermination(delay, TimeUnit.SECONDS)) {
            logger.warn("Tomcat thread pool did not shut down gracefully within " + delay + " second(s). Proceeding with force shutdown");
        } else {
            logger.debug("Tomcat thread pool is empty, we stop now");
        }
    }

    /**
     * Close the underlying server socket so that new HTTP connections are refused
     * at the TCP level instead of stalling in the accept queue while the connector
     * is paused. Existing connections keep being handled until they complete.
     */
    private void closeServerSocket() {
        try {
            final Object endpoint = connector.getProtocolHandler().getEndpoint();
            final Field serverSocketField = ReflectionUtils.findField(endpoint.getClass(), "serverSocket");
            if (serverSocketField != null) {
                ReflectionUtils.makeAccessible(serverSocketField);
                final Object serverSocket = ReflectionUtils.getField(serverSocketField, endpoint);
                if (serverSocket instanceof ServerSocket && !((ServerSocket) serverSocket).isClosed()) {
                    ((ServerSocket) serverSocket).close();
                    logger.info("Tomcat server socket is closed, new connections are now refused");
                }
            }
        } catch (final Exception ex) {
            logger.warn("Unable to close the tomcat server socket : " + ex.getMessage());
        }
    }

    /**
     * Set connector.
     * @param connector is the catalina connector.
     */
    public void customize(final Connector connector) {
        this.connector = connector;
    }
}
