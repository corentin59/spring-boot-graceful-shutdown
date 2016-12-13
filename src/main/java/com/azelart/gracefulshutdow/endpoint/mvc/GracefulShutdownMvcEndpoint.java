package com.azelart.gracefulshutdow.endpoint.mvc;

import com.azelart.gracefulshutdow.endpoint.GracefulShutdownEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;

/**
 * Adapter to expose {@link GracefulShutdownEndpoint} as an {@link org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint}.
 *
 * @author Corentin Azelart
 */
@ConfigurationProperties(prefix = "endpoints.shutdown.graceful")
public class GracefulShutdownMvcEndpoint extends EndpointMvcAdapter {

    /**
     * Create a new {@link EndpointMvcAdapter}.
     * @param delegate the underlying {@link Endpoint} to adapt.
     */
    public GracefulShutdownMvcEndpoint(Endpoint<?> delegate) {
        super(delegate);
    }

    /**
     * Invoke the graceful shutdown.
     * @return the response entity
     */
    @GetMapping
    @ResponseBody
    @Override
    public Object invoke() {
        if (!getDelegate().isEnabled()) {
            return new ResponseEntity<Map<String, String>>(
                    Collections.singletonMap("message", "This endpoint is disabled"),
                    HttpStatus.NOT_FOUND);
        }
        return super.invoke();
    }
}
