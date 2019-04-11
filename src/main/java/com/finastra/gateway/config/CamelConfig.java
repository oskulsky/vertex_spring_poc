package com.finastra.gateway.config;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean
    public CamelContext camelContext() throws Exception {
        JndiContext jndiContext = new JndiContext();
        return new DefaultCamelContext(jndiContext);
    }

    @Bean
    public void initRoutes() throws Exception {
        CamelContext context = camelContext();
        context.start();
    }
}
