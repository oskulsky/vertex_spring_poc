package com.finastra.gateway.components;

import com.finastra.gateway.common.Constants;
import com.finastra.gateway.services.ConfigService;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.OutboundMapping;
import io.vertx.core.Vertx;
import lombok.val;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = Constants.CONFIG_PREFIX, name = Constants.DIRECTION, havingValue = Constants.EGRESS)
public class EgressGateway {

    private static final String EGRESS_PROCESS_REQUEST_ADDRESS  = "direct:processRequest";

    @Autowired
    private ConfigService configService;

    @Autowired
    private Vertx vertx;

    @Autowired
    private CamelContext context;

    private CamelBridge bridge;

    @Bean
    public void egressFlow() throws Exception {
        val cbo = new CamelBridgeOptions(context)
                .addOutboundMapping(OutboundMapping.fromVertx(configService.getEventBusAddress())
                .toCamel(EGRESS_PROCESS_REQUEST_ADDRESS));

        context.addRoutes(new EgressRouteBuilder());

        bridge = CamelBridge.create(vertx, cbo);
        bridge.start();
    }

    @PreDestroy
    public void cleanUp() {
        Optional.ofNullable(bridge)
                .ifPresent(CamelBridge::stop);
    }

    class EgressRouteBuilder extends RouteBuilder {

        @Override
        public void configure() {
            from(EGRESS_PROCESS_REQUEST_ADDRESS)
                    .bean(configService, "resolveCommunicationType")
                    .process(new HeaderProc())
                    .to("log:Received egress request")
//                    .process().message(msg -> configService.resolveCommunicationType(msg.getBody()))
//                    .process("bean:configService?method=resolveCommunicationType")
                    .to("log: build request")
                    .to("log: route to desired channel");

        }
    }

    static class HeaderProc implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println(exchange.getIn().getHeaders().toString());
        }

        /*
        GetAccountRequestMessage req = exchange.getIn().getBody(GetAccountRequestMessage.class);
            System.out.println("OBYBYBY: " + req);
            CommunicationConfig commType = null;
            exchange.getOut().setHeader("commType", "oby comm Type");
         */
    }
}