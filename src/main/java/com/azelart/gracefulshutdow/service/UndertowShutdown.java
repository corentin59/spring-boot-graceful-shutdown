package com.azelart.gracefulshutdow.service;

import com.azelart.gracefulshutdow.wrapper.UndertowShutdownHandlerWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Undertow shutdown.
 */
public class UndertowShutdown implements Shutdown {

    /**
     * The wrapper for manual commands.
     */
    @Autowired
    private UndertowShutdownHandlerWrapper undertowShutdownHandlerWrapper;

    /**
     * Logger from common util.
     */
    private static final Log logger = LogFactory.getLog(UndertowShutdown.class);

    /**
     * Perform a pause on the server.

     * @throw InterruptedException if we have an interruption
     */
    public void pause() throws InterruptedException {
        // Nothing todo with Undertow.
    }

    /**
     * Perform shutdown.
     * @param delay is delay to force
     * @throw InterruptedException if we have an interruption
     */
    public void shutdown(Integer delay) throws InterruptedException {
        undertowShutdownHandlerWrapper.getGracefulShutdownHandler().shutdown();
        undertowShutdownHandlerWrapper.getGracefulShutdownHandler().awaitShutdown(delay * 1000);
    }
}
