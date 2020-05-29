/*
 * Copyright (c) 2020, Regents of the University of California and
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
package edu.berkeley.bidms.restclient.util;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class SmarterURIBuilder extends URIBuilder {
    public SmarterURIBuilder(URI uri) {
        super(uri);
    }

    /**
     * see {@link URIBuilder#addParameter(String, String)}
     */
    @Override
    public SmarterURIBuilder addParameter(final String param, final String value) {
        return (SmarterURIBuilder) super.addParameter(param, value);
    }

    /**
     * Same as {@link URIBuilder#addParameter(String, String)} except the
     * <code>value</code> parameter type is an {@link Object} which will
     * be converted to a string value using <code>value.toString()</code>.
     *
     * @param param Parameter name.
     * @param value Parameter value.
     * @return Returns self.
     */
    public SmarterURIBuilder addParameter(final String param, final Object value) {
        return addParameter(param, value.toString());
    }

    /**
     * Conditionally add a parameter to the URI query.
     *
     * @param add   A boolean to indicate if the parameter should be added to
     *              the query. If false, a call to this method is a "no-op."
     * @param param Parameter name.
     * @param value Parameter value.
     * @return Returns self.
     */
    public SmarterURIBuilder conditionalAddParameter(final boolean add, final String param, final Object value) {
        return add ? addParameter(param, value) : this;
    }

    /**
     * Same as {@link URIBuilder#build()} except {@link URISyntaxException}
     * is caught and wrapped by a {@link RuntimeException} that is then
     * thrown.
     *
     * @return The built {@link URI}.
     * @throws RuntimeException if an exception occurs while building the
     *                          {@link URI}.
     */
    public URI rbuild() {
        try {
            return build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}