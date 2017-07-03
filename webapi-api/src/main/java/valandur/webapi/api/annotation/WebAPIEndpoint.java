package valandur.webapi.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a method as an endpoint for the Web-API. The method needs to have at least one argument of
 * type {@link valandur.webapi.api.servlet.IServletData}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebAPIEndpoint {

    enum HttpMethod {
        GET, PUT, POST, DELETE,
    }

    /**
     * Defines what HTTP verb this endpoint will be available at.
     * @return The HTTP method for this endpoint.
     */
    HttpMethod method();

    /**
     * Defines what subpath this endpoint will be available at. This is relative to the
     * servlet path {@link WebAPIServlet#basePath().
     * @return The path of the endpoint.
     */
    String path();

    /**
     * The permissions required to access this endpoint. An empty String (the default) means that no permissions are
     * required. These permissions are relative to the {@link WebAPIServlet#basePath()}, which serves as the root
     * permissions entry.
     * @return The permissions required for this endpoint.
     */
    String perm() default "";
}
