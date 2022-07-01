package org.jgb.quarkus.control.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.CompositeException;
import org.jboss.logging.Logger;

import java.util.HashMap;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Create a HTTP response from an exception.
 * <p>
 * Response Example:
 *
 * <pre>
 * HTTP/1.1 422 Unprocessable Entity
 * Content-Length: 111
 * Content-Type: application/json
 *
 * {
 *     "code": 422,
 *     "error": "Fruit name was not set on request.",
 *     "exceptionType": "javax.ws.rs.WebApplicationException"
 * }
 * </pre>
 */
@Provider
public class ErrorMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(ErrorMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("Failed to handle request", exception);

        Throwable throwable = exception;

        int code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        if (throwable instanceof WebApplicationException) {
            code = ((WebApplicationException) exception).getResponse().getStatus();
        }

        // This is a Mutiny exception and it happens
        if (throwable instanceof CompositeException) {
            throwable = throwable.getCause();
        }

        var exceptionJson = new HashMap<>();
        exceptionJson.put("exceptionType", throwable.getClass().getName());
        exceptionJson.put("code", code);

        if (exception.getMessage() != null) {
            exceptionJson.put("error", throwable.getMessage());
        }

        return Response.status(code)
                .entity(exceptionJson)
                .build();
    }

}