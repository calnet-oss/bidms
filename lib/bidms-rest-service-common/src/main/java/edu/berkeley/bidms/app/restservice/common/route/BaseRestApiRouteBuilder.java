/*
 * Copyright (c) 2019, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.restservice.common.route;

import edu.berkeley.bidms.app.restservice.common.service.UnexpectedExceptionService;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for a REST route builder that is meant to route controller
 * requests to services.  Each controller endpoint will have a route builder
 * that extends this class.  The implementation at minimum needs to override
 * the {@link #BaseRestApiRouteBuilder(ProducerTemplate,
 * UnexpectedExceptionService, Object, String)} constructor such that it
 * calls the base constructor with the {@link ProducerTemplate} bean, the
 * {@link UnexpectedExceptionService} bean, the service bean that accepts the
 * request and the method name to call on the service bean.  The controller
 * will then call the {@link #receive(Object, HttpServletRequest)} method,
 * which will route the request to the service bean method.
 *
 * <p>
 * With the default implementation of the route (constructed by the {@link
 * #configure()} method, if an uncaught exception is thrown from the service,
 * the REST response is formulated by the return value of the {@link
 * UnexpectedExceptionService#respond(Object, Exception)} implementation.
 * </p>
 *
 * @param <I> The request model class.  This model contains the request
 *            data.
 * @param <O> The response model class.  This model contains the response
 *            data.
 */
public abstract class BaseRestApiRouteBuilder<I, O> extends RouteBuilder {
    /**
     * The {@link HttpServletRequest} value is placed in the Camel {@link
     * org.apache.camel.Exchange} properties using this property name.
     */
    public static final String EPROP_SERVLET_REQUEST = "edu.berkeley.bidms.SERVLET_REQUEST";

    private ProducerTemplate producerTemplate;
    private UnexpectedExceptionService unexpectedExceptionService;
    private Object service;
    private String serviceMethodName;

    /**
     * Constructor for the route builder.  Typically, a class that extends
     * this base class will call this parent constructor and pass the needed
     * values.
     *
     * @param producerTemplate           A {@link ProducerTemplate} bean
     *                                   instance.
     * @param unexpectedExceptionService A {@link UnexpectedExceptionService}
     *                                   bean instance.
     * @param service                    The route will call this service
     *                                   bean using the serviceMethodName.
     * @param serviceMethodName          The method name on the service bean
     *                                   to call when a request is received.
     */
    public BaseRestApiRouteBuilder(
            ProducerTemplate producerTemplate,
            UnexpectedExceptionService unexpectedExceptionService,
            Object service,
            String serviceMethodName
    ) {
        this.producerTemplate = producerTemplate;
        this.unexpectedExceptionService = unexpectedExceptionService;
        this.service = service;
        this.serviceMethodName = serviceMethodName;
    }

    /**
     * The {@link #receive(Object, HttpServletRequest)} method will send a
     * message to the endpoint returned by this method and the listener,
     * configured by the route in the {@link #configure()} method, will
     * consume the message off this endpoint. The default implementation will
     * return a URI string in the format of
     * <i>direct:[className]</i>.  This is largely an internal detail of how
     * Camel is used and typically this method would not be overridden.
     *
     * @return The Camel endpoint URI to route through.
     */
    public String getEndpointUri() {
        return "direct:" + getClass().getName();
    }

    /**
     * Controllers call this method when they receive a request.  This
     * triggers the route and delivers the request to the service.
     *
     * @param request        The model instance containing request data. This
     *                       is typically a result of Spring data binding in
     *                       the controller.
     * @param servletRequest Optionally, the {@link HttpServletRequest}
     *                       object passed in from the controller.  In the
     *                       default implementation, this is not used by the
     *                       route other than to make it available as a
     *                       property (named {@link #EPROP_SERVLET_REQUEST})
     *                       in the Camel {@link org.apache.camel.Exchange}.
     * @return The model instance containing the response data.  This is what
     * is returned by the service bean method.
     */
    @SuppressWarnings("unchecked")
    public O receive(I request, HttpServletRequest servletRequest) {
        return (O) producerTemplate.request(getEndpointUri(), exchange -> {
            exchange.getIn().setBody(request);
            exchange.setProperty(EPROP_SERVLET_REQUEST, servletRequest);
        }).getMessage().getBody();
    }

    /**
     * Configures the Camel route.  In the default implementation, if an
     * uncaught exception occurs while calling the service bean method, the
     * response is formulated by the {@link UnexpectedExceptionService#respond(Object,
     * Exception)} method.
     *
     * @throws Exception Thrown if there's an error building the Camel
     *                   route.
     */
    @Override
    public void configure() throws Exception {
        from(getEndpointUri())
                .doTry()
                .bean(service, serviceMethodName, true)
                .doCatch(Exception.class)
                .bean(unexpectedExceptionService, "respond", true)
                .end();
    }
}
