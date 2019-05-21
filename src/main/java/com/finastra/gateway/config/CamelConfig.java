package com.finastra.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finastra.gateway.common.messages.GatewayMessage;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.TypeConverterSupport;
import org.apache.camel.util.jndi.JndiContext;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Configuration
public class CamelConfig {

    @Bean
    public CamelContext camelContext() throws Exception {
        JndiContext jndiContext = new JndiContext();
        CamelContext context = new DefaultCamelContext(jndiContext);
        context.getTypeConverterRegistry().addTypeConverter(
                InputStream.class, GatewayMessage.class, new GatewayMessageConverter());

        return context;
    }

    @Bean
    public ServletRegistrationBean<CamelHttpTransportServlet> servletRegistrationBean() {
        final ServletRegistrationBean servlet = new ServletRegistrationBean(
                new CamelHttpTransportServlet(), "/camel/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

    /*@Bean
    public void initRoutes() throws Exception {
        CamelContext context = camelContext();
        context.start();
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("rest:get:oby").to("log:temp");
            }
        });
    }*/

    class GatewayMessageConverter extends TypeConverterSupport {

        @Override
        public <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
            GatewayMessage msg = (GatewayMessage) value;
            try {
                return (T) new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(msg));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
