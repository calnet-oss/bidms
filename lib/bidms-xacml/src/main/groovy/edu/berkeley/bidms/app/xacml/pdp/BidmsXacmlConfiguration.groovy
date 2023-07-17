/*
 * Copyright (c) 2023, Regents of the University of California and
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
package edu.berkeley.bidms.app.xacml.pdp

import com.att.research.xacml.api.pdp.PDPEngine
import com.att.research.xacml.util.XACMLProperties
import com.att.research.xacmlatt.pdp.ATTPDPEngine
import com.att.research.xacmlatt.pdp.ATTPDPEngineFactory
import com.att.research.xacmlatt.pdp.policy.PolicyFinderFactory
import edu.berkeley.bidms.xacml.pdp.BidmsXacmlEvaluationContextFactory
import edu.berkeley.bidms.xacml.pdp.BidmsXacmlPolicyFinder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

import jakarta.annotation.PostConstruct

@Configuration
class BidmsXacmlConfiguration {

    private static ClassPathResource getXacmlPropertiesResource() {
        def r = new ClassPathResource("xacml.properties")
        if (!r.exists()) {
            throw new FileNotFoundException("xacml.properties not found as a resource file")
        }
        return r
    }

    private static File getXacmlPropertiesFile() {
        return xacmlPropertiesResource.getFile()
    }

    private static Properties getXacmlProperties() {
        Properties properties = new Properties()
        try (InputStream inStream = xacmlPropertiesResource.inputStream) {
            properties.load(inStream)
        }
        return properties
    }

    @PostConstruct
    private static void init() {
        // This is important for loading the Factories, i.e. important for FactoryFinder.find().
        System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, xacmlPropertiesFile.path)
    }

    @Bean
    BidmsXacmlPolicyFinder getSpringPolicyFinder() {
        def properties = xacmlProperties
        def policyFinder = PolicyFinderFactory.newInstance(properties).getPolicyFinder(properties)
        if (!(policyFinder instanceof BidmsXacmlPolicyFinder)) {
            throw new IllegalStateException("The configured policy finder is supposed to be an instance of ${BidmsXacmlPolicyFinder.name}  but instead it is ${policyFinder.getClass().name}.  Check that xacml.att.policyFinderFactory=${BidmsXacmlPolicyFinder.name} in xacml.properties.")
        }
        return (BidmsXacmlPolicyFinder) policyFinder
    }

    @Bean(destroyMethod = "shutdown")
    BidmsXacmlEvaluationContextFactory getSpringEvaluationContextFactory(BidmsXacmlPolicyFinder policyFinder) {
        return new BidmsXacmlEvaluationContextFactory(xacmlProperties, policyFinder)
    }

    @Bean
    PDPEngine getPDPEngine(BidmsXacmlEvaluationContextFactory evaluationContextFactory) {
        def properties = xacmlProperties
        def engineFactory = new ATTPDPEngineFactory()
        return new ATTPDPEngine(evaluationContextFactory, engineFactory.defaultBehavior, engineFactory.scopeResolver, properties)
    }
}
