package com.azelart.gracefulshutdow.service;

import org.apache.catalina.connector.Connector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;

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
     * Set connector.
     * @param connector is the catalina connector.
     */
    public void customize(final Connector connector) {
        this.connector = connector;
    }
}
