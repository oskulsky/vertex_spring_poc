package com.finastra.gateway.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finastra.gateway.common.Constants;
import com.finastra.gateway.common.messages.GatewayMessage;
import com.finastra.gateway.common.messages.GetAccountRequestMessage;
import com.finastra.gateway.config.comm.configs.CommunicationConfig;
import com.finastra.gateway.services.AuthService;
import com.finastra.gateway.services.ConfigService;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.OutboundMapping;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
        CamelBridgeOptions cbo = new CamelBridgeOptions(context)
                .addOutboundMapping(OutboundMapping.fromVertx(configService.getEventBusAddress())
                .toCamel(EGRESS_PROCESS_REQUEST_ADDRESS));

        context.addRoutes(new EgressRouteBuilder());

        /* temp */
//        context.addRoutes(new TempRouteBuilder());
        /* end-temp */

        bridge = CamelBridge.create(vertx, cbo);
        bridge.start();
        context.start();
    }

    @PreDestroy
    public void cleanUp() {
        Optional.ofNullable(bridge)
                .ifPresent(CamelBridge::stop);
    }

    @SuppressWarnings("unused") // This causes a recursion.
    public String dynRoute(Exchange exchange) {
        Message msg = exchange.getIn();
        log.info("Dynamically routing body: " + msg.getBody());
        CommunicationConfig config = msg.getHeader(Constants.GATEWAY_MESSAGE_CONFIG, CommunicationConfig.class);
        log.info("Dynamically routing body-criteria: " + config.toString());
        return "direct://" + config.type().toString();
    }

    class EgressRouteBuilder extends RouteBuilder {

        @Override
        public void configure() {
            from(EGRESS_PROCESS_REQUEST_ADDRESS) // TODO OBY: Reactify
                    .validate(authService.isAuthorized())
                    .process(new TenantProcessor())
                    .process(new EgressRequestProcessor())
                    .to("log:Received egress request")
                    .recipientList(simple("direct:${header." + Constants.GATEWAY_MESSAGE_CONFIG + ".type}"))
//                    .dynamicRouter(method(EgressGateway.class, "dynRoute"))
//                    .process().message(msg -> configService.resolveCommunicationType(msg.getBody()))
//                    .process("bean:configService?method=resolveCommunicationType")
                    /*.to("log: build request")
                    .to("log: route to desired channel")
                    .log("Done processing")*/
                    .to("direct:REST");

            from ("direct:REST")
                    .process(xchg -> {
                        Message msg = xchg.getIn();
                        msg.setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST));
                        msg.setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                        msg.setHeader(Exchange.HTTP_QUERY, "param=oby&param2=121");
                    })
                    .to("http4://localhost:8080/oby/trance")
                    .process(xchg -> System.out.println(xchg.getIn().getBody() + "\n\n\n" + xchg.getOut().getBody()))
                    .to("log:REST");

            from ("direct:MQ").to("log:obytrance-MQ");

            rest("/say")
                    .get("/hello").route().to("log:Anom-Rest").endRest()
                    .get("/bye").consumes("application/json").to("direct:bye")
                    .post("/bye").to("mock:update");

            rest("/amon").get().to("log:Anom-Rest");
            rest("/amon")

                    .post("/amarth").type(GetAccountRequestMessage.class)
                        .produces(MediaType.APPLICATION_JSON_VALUE)
                        .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .route()
                    .to("log:testery")
                    .process(exchange -> {
                        String msg = exchange.getIn().getBody(String.class);
                        log.info("Resty body-string: " + msg);
                        GetAccountRequestMessage accMsg = exchange.getIn().getBody(GetAccountRequestMessage.class);
                        log.info("Resty body-obj: " + accMsg);
                        log.info("Resty body-oby-ject: " + new ObjectMapper().readValue(msg, GetAccountRequestMessage.class));

                    })
                    .endRest()
                    .to("log:Amon amarth");

//            from("rest:get:amon").to("log:Anom-Rest");


            /*from ("rest:get:")
//                    .transform().simple("Song is: ${header.song}")
                    .to("log:Anom-Rest");*/
//                    .process(xchg -> xchg.getIn().setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST)));
//                    .to("http4://localhost:8080/oby/trance?param=gid")
//                    .to("log:REST");

            from("jetty:http://localhost:8081/oby?httpMethodRestrict=POST")
                    .unmarshal().json(JsonLibrary.Jackson, GatewayMessage.class)
                    /*.process(exchange -> {
                        System.out.println("str: " + exchange.getIn().getBody(String.class));
                        System.out.println("OBJ:" + exchange.getIn().getBody(GetAccountRequestMessage.class));
                    })*/
                    .to("log:test-jetty");
        }
    }

    /*class Expr implements Expression {

        @Override
        public <T> T evaluate(Exchange exchange, Class<T> type) {
            return ;
        }
    }*/

    class TenantProcessor implements Processor {

        @Override
        public void process(Exchange exchange) {
            log.info("Tenant: " + exchange.getIn().getHeader(Constants.TENANT).toString());
        }
    }

    class EgressRequestProcessor implements Processor {

        @Override
        public void process(Exchange exchange) {
            Message message = exchange.getIn();
            GatewayMessage gatewayMsg = message.getBody(GatewayMessage.class);
            CommunicationConfig config = configService.resolveCommunicationType(message.getHeader(Constants.TENANT).toString(), gatewayMsg);
            exchange.getIn().setHeader(Constants.GATEWAY_MESSAGE_CONFIG, config);
        }
    }

    @Deprecated
    class TempRouteBuilder extends RouteBuilder {

        @Override
        public void configure() {
            /*from("direct:OBY")
                    .to("log:I'm done");*/

            from ("direct:REST")
                    .to("log:obytrance-rest");

            /*from ("direct:REST")
                    .process(xchg -> xchg.getIn().setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST)));*/
//                    .to("http4://localhost:8080/oby/trance?param=gid")
//                    .to("log:REST");

            from ("direct:MQ").to("log:MQ");
        }
    }

    public static void main(String args[]) throws JsonProcessingException {
        System.out.println(new ObjectMapper().writeValueAsString(new GetAccountRequestMessage("666")));
    }
}