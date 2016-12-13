package com.azelart.gracefulshutdow.endpoint;

import com.azelart.gracefulshutdow.service.Shutdown;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Actuator endpoint.
 */
@ConfigurationProperties(prefix = "endpoints.shutdown.graceful")
public class GracefulShutdownEndpoint extends AbstractEndpoint<Map<String, Object>> implements ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    /**
     * SPI interface to be implemented by most if not all application contexts.
     * Provides facilities to configure an application context in addition
     * to the application context client methods in the
     * {@link org.springframework.context.ApplicationContext} interface.
     */
    private ConfigurableApplicationContext context;

    /**
     * Number of seconds before brute shutdown.
     */
    private Integer timeout;

    /**
     * The timer before launch graceful shutdown.
     * The health checker return OUT_OF_SERVICE.
     */
    private Integer wait;

    /**
     * Start date for shutdown.
     */
    private Date startShutdown;

    /**
     * Stop date for shutdown.
     */
    private Date stopShutdown;

    /**
     * Shutdown operation.
     */
    @Autowired
    private Shutdown shutdown;

    /**
     * Graceful shutdown without context.
     */
    private static final Map<String, Object> NO_CONTEXT_MESSAGE = Collections
            .unmodifiableMap(Collections.<String, Object>singletonMap("message",
                    "No context to shutdown."));

    /**
     * Graceful shutdown with context.
     */
    private static final Map<String, Object> SHUTDOWN_MESSAGE = Collections
            .unmodifiableMap(Collections.<String, Object>singletonMap("message",
                    "Graceful shutting down, bye..."));

    /**
     * Logger from common util.
     */
    private static final Log logger = LogFactory.getLog(GracefulShutdownEndpoint.class);

    /**
     * Create a new {@link GracefulShutdownEndpoint} instance.
     * @param timeout is the time to wait before launch brutal shutdown
     * @param wait is the time to wait before launch graceful shutdown
     */
    public GracefulShutdownEndpoint(final Integer timeout, final Integer wait) {
        super("shutdowngraceful", Boolean.TRUE, Boolean.FALSE);
        this.timeout = timeout;
        this.wait = wait;
    }

    /**
     * When the spring context is close.
     * This is catch :
     * - CTRL+C : ?
     * - Actuator Shutdown ?
     * @param event is the close event.
     */
    public void onApplicationEvent(final ContextClosedEvent event) {
        if(stopShutdown != null && startShutdown != null) {
            final long seconds = (stopShutdown.getTime() - startShutdown.getTime())/1000;
            logger.info("Shutdown performed in " + seconds + " second(s)");
        }
    }

    /**
     * Invoke the graceful shutdown.
     * @return the context message if present
     */
    public Map<String, Object> invoke() {
        if (this.context == null) {
            return NO_CONTEXT_MESSAGE;
        } try {
            return SHUTDOWN_MESSAGE;
        } finally {

            /*
             * We use a thread to detach process.
             * And permit to empty the thread pool executor.
             */
            final Thread thread = new Thread(new Runnable() {

                public void run() {

                try {
                    // We top the start
                    startShutdown = new Date();

                    // Set Health Checker in OUT_OF_SERVICE state.
                    logger.info("We are now in OUT_OF_SERVICE mode, please wait " + wait + " second(s)...");
                    Thread.sleep(wait * 1000);
                    shutdown.pause();

                    // Pause the protocol.
                    logger.info("Graceful shutdown in progess... We don't accept new connection... Wait after latest connections (max : " + timeout + " seconds)... ");

                    // perform stop
                    shutdown.shutdown(timeout);

                    // Close spring context.
                    stopShutdown = new Date();
                    context.close();
                } catch (final InterruptedException ex) {
                    logger.error("The await termination has been interrupted : " + ex.getMessage());
                    Thread.currentThread().interrupt();
                }
                }
            });

            // Link and start thread.
            thread.setContextClassLoader(getClass().getClassLoader());
            thread.start();
        }
    }

    /**
     * Set application context.
     * This is read-only while the application is running.
     * @param applicationContext is the application context.
     * @throws BeansException
     */
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            this.context = (ConfigurableApplicationContext) applicationContext;
        }
    }

    /**
     * Represent the start of shutdown.
     * If she set we are in shutdown graceful state.
     * @return the date if the shutdown is un progress
     */
    public Date getStartShutdown() {
        return startShutdown;
    }
}
