package edu.berkeley.calnet.ucbmatch.config

import groovy.util.logging.Log4j
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.InitializingBean

@Log4j
class MatchConfigFactoryBean implements FactoryBean<MatchConfig>, InitializingBean {
    String resource

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
        def script = this.getClass().getResourceAsStream(resource).text
        log.debug("MatchConfig parsing config")
        try {
            config = parseConfig(script)
        } catch (e) {
            throw new RuntimeException("Failed to parse script $resource",e)
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


