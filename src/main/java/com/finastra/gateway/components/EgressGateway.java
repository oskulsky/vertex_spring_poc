package com.finastra.gateway.components;

import com.finastra.gateway.common.Constants;
import com.finastra.gateway.common.messages.GatewayMessage;
import com.finastra.gateway.config.comm.configs.CommunicationConfig;
import com.finastra.gateway.services.AuthService;
import com.finastra.gateway.services.ConfigService;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.OutboundMapping;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
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
@Slf4j
public class EgressGateway {

    private static final String EGRESS_PROCESS_REQUEST_ADDRESS  = "direct:processRequest";

    @Autowired
    private ConfigService configService;

    @Autowired
    private AuthService authService;

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

        /* temp */
        context.addRoutes(new TempRouteBuilder());
        /* end-temp */

        bridge = CamelBridge.create(vertx, cbo);
        bridge.start();
    }

    @PreDestroy
    public void cleanUp() {
        Optional.ofNullable(bridge)
                .ifPresent(CamelBridge::stop);
    }

    @SuppressWarnings("unused")
    public String dynRoute(Exchange exchange) {
        Message msg = exchange.getIn();
        log.info("Dynamically routing body: " + msg.getBody());
        CommunicationConfig config = msg.getHeader(Constants.GATEWAY_MESSAGE_CONFIG, CommunicationConfig.class);
        log.info("Dynamically routing body-criteria: " + config.toString());
        return "direct:" + config.type();
    }

    class EgressRouteBuilder extends RouteBuilder {

        @Override
        public void configure() {
            from(EGRESS_PROCESS_REQUEST_ADDRESS) // TODO OBY: Reactify
                    .validate(authService.isAuthorized())
                    .process(new TenantProcessor())
                    .process(new EgressRequestProcessor())
                    .to("log:Received egress request")
                    .dynamicRouter(method(EgressGateway.class, "dynRoute"))
//                    .process().message(msg -> configService.resolveCommunicationType(msg.getBody()))
//                    .process("bean:configService?method=resolveCommunicationType")
                    .to("log:build request")
                    .to("log:route to desired channel")
                    .log("Done processing");
        }
    }

    class TenantProcessor implements Processor {

        @Override
        public void process(Exchange exchange) {
            log.info(exchange.getIn().getHeader(Constants.TENANT).toString());
        }
    }

    class EgressRequestProcessor implements Processor {

        @Override
        public void process(Exchange exchange) {
            GatewayMessage msg = exchange.getIn().getBody(GatewayMessage.class);
            CommunicationConfig config = configService.resolveCommunicationType(msg);
            exchange.getIn().setHeader(Constants.GATEWAY_MESSAGE_CONFIG, config);
        }
    }

    @Deprecated
    class TempRouteBuilder extends RouteBuilder {

        @Override
        public void configure() {
            from("direct:OBY")
                    .to("log:I'm done");

            from ("direct:REST").to("log:REST");
            from ("direct:MQ").to("log:MQ");

        }
    }
}