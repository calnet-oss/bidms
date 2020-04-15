/*
 * Copyright (c) 2014, Regents of the University of California and
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
package edu.berkeley.bidms.app.matchengine.config

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.Resource

@CompileStatic
@Slf4j
class MatchConfigFactoryBean implements FactoryBean<MatchConfig>, InitializingBean {
    Resource resource

    private MatchConfig config

    @Override
    MatchConfig getObject() throws Exception {
        config
    }

    @Override
    Class<?> getObjectType() {
        return MatchConfig
    }

    @Override
    boolean isSingleton() {
        return true
    }

    @Override
    void afterPropertiesSet() throws Exception {
        log.debug("MatchConfig reading config from $resource")
        InputStream is = resource.inputStream
        String script = null
        try {
            script = is.text
        }
        finally {
            is.close()
        }
        log.debug("MatchConfig parsing config")
        try {
            config = parseConfig(script)
        } catch (e) {
            throw new RuntimeException("Failed to parse script $resource", e)
        }
    }

    static MatchConfig parseConfig(String script) {
        Script s = new GroovyClassLoader().parseClass(script).newInstance() as Script
        s.binding = new MatchAttributeBinding()
        return s.run() as MatchConfig
    }

    private static class MatchAttributeBinding extends Binding {
        MatchConfigBuilder builder = new MatchConfigBuilder()

        Object getVariable(String name) {
            return { Object... args ->
                builder.invokeMethod(name, args)
                return builder.config
            }
        }
    }
}


