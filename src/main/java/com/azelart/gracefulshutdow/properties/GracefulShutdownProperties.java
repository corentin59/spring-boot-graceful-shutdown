package com.azelart.gracefulshutdow.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global graceful shutdown properties.
 * @author Corentin Azelart
 */
@ConfigurationProperties(prefix = "endpoints.shutdown.graceful")
public class GracefulShutdownProperties {

    /**
     * The timer before launch graceful shutdown.
     * The health checker return OUT_OF_SERVICE.
     */
    private Integer wait = 30;

    /**
     * The timeout before force shutdown.
     */
    private Integer timeout = 30;

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getWait() {
        return wait;
    }

    public void setWait(Integer wait) {
        this.wait = wait;
    }
}
