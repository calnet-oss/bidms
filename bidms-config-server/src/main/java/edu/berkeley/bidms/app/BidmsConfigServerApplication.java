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
package edu.berkeley.bidms.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.web.context.WebApplicationContext;

//
// To test: curl https://host:port/bidms-config/APP/PROFILE/LABEL
// (LABEL is optional.)
// (There are other url possibilities.  The quck-start doc shows them.)
//
// The actual health check URL would be:
// (as explained in https://cloud.spring.io/spring-cloud-config/reference/html/#_health_indicator)
// curl https://host:port/bidms-config/app/default
//
// APP/PROFILE/LABEL is explained:
// https://cloud.spring.io/spring-cloud-config/reference/html/#_quick_start
// and
// https://cloud.spring.io/spring-cloud-config/reference/html/#_environment_repository
//
// APP is client's application name.  PROFILE is the client's application
// profile (development, production, etc).  For git backend, if specified,
// LABEL is the git branch.  (If not specified, master is assumed.)
//

// https://cloud.spring.io/spring-cloud-config/multi/multi__embedding_the_config_server.html
@SpringBootApplication
@EnableConfigServer
public class BidmsConfigServerApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(BidmsConfigServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BidmsConfigServerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BidmsConfigServerApplication.class);
    }

    @Override
    protected WebApplicationContext run(SpringApplication application) {
        return super.run(application);
    }
}
