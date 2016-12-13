package com.azelart.gracefulshutdow.wrapper;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;

/**
 * Undertow handler wrapper.
 */
public class UndertowShutdownHandlerWrapper implements HandlerWrapper {

    /**
     * graceful shutdown handler.
     */
    private GracefulShutdownHandler gracefulShutdownHandler;

    /**
     * Wrapper.
     * @param handler is the http handler from chain.
     * @return the Undertown shutdown handler.
     */
    public HttpHandler wrap(final HttpHandler handler) {
        if(gracefulShutdownHandler == null) {
            this.gracefulShutdownHandler = new GracefulShutdownHandler(handler);
        }
        return gracefulShutdownHandler;
    }

    /**
     * Return the graceful shutdown handler to perform manual command : pause/shutdown.
     * @return the shutdown handler.
     */
    public GracefulShutdownHandler getGracefulShutdownHandler() {
        return gracefulShutdownHandler;
    }

}
