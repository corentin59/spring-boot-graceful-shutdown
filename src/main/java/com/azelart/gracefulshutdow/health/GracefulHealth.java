package com.azelart.gracefulshutdow.health;

import com.azelart.gracefulshutdow.endpoint.GracefulShutdownEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Strategy class used to provide an indication of application health.
 */
@EnableAutoConfiguration
public class GracefulHealth implements HealthIndicator {

    /**
     * The graceful shutdown endpoint.
     */
    @Autowired
    private GracefulShutdownEndpoint gracefulShutdownEndpoint;

    /**
     * Return the health state.
     * @return OK or OUT_OF_SERVICE
     */
    public Health health() {
        if(gracefulShutdownEndpoint.getStartShutdown() == null) {
            return Health.up().build();
        } else {
            return Health.outOfService().build();
        }
    }
}
