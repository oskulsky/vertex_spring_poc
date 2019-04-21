package com.finastra.gateway.services;

import com.finastra.gateway.common.Constants;
import com.finastra.gateway.common.messages.ComplianceValidationRequestMessage;
import com.finastra.gateway.common.messages.GatewayMessage;
import com.finastra.gateway.common.messages.GetAccountRequestMessage;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@Slf4j
@EnableScheduling
@Deprecated
public class Komponenta {

    private SecureRandom raondom = new SecureRandom();

    @Autowired
    private ConfigService configService;


    @Autowired
    private Vertx vertx;

    @Autowired
    private CamelContext camelContext;


    /*@PostConstruct
    public void init() {
        CamelBridgeOptions opts = new CamelBridgeOptions(camelContext);
        opts.addOutboundMapping(OutboundMapping.fromVertx(configService.getEventBusAddress())
                .setHeadersCopy(true)
                .toCamel("direct:oby"));

        CamelBridge bridge = CamelBridge.create(vertx, opts);
        bridge.start();
//        vertx.eventBus().consumer(ADDRESS, this::processMsg);
    }*/

    private void processMsg(Message msg) {
        log.info("Processed: " + msg.body());
        log.info("Headers: " + msg.headers().toString());
    }

    @Scheduled(fixedRate = 10000)
    public void fire() {
        long time = System.currentTimeMillis();
        long num = sumUP(time);
        GatewayMessage msg = num % 3 == 0 ? new GetAccountRequestMessage(Long.valueOf(time).toString())
                : new ComplianceValidationRequestMessage("First", "Sec", Long.valueOf(time).toString());

//        GatewayMessage msg = new GetAccountRequestMessage(Long.valueOf(time).toString());

        DeliveryOptions opts = new DeliveryOptions()
                .addHeader(Constants.TENANT, "Ten-ant")
                .addHeader(Constants.AUTHORIZTION, "Drag the waters");

        vertx.eventBus().send(configService.getEventBusAddress(), msg, opts);
    }

    private int sumUP(long num) {
        int acc = 0;

        while(num > 0) {
            acc += num % 10;
            num /= 10;
        }

        return acc;

    }
}
