package com.azelart.gracefulshutdow.service;

/**
 * Shutdown service.
 */
public interface Shutdown {

    /**
     * Perform a pause on the server.
     * @throw InterruptedException if we have an interruption
     */
    void pause() throws InterruptedException;

    /**
     * Perform shutdown.
     * @param delay is delay to force
     * @throw InterruptedException if we have an interruption
     */
    void shutdown(Integer delay) throws InterruptedException;
}
