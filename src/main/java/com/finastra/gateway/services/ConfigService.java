package com.finastra.gateway.services;

import com.finastra.gateway.common.messages.ComplianceValidationRequestMessage;
import com.finastra.gateway.common.messages.GatewayMessage;
import com.finastra.gateway.common.messages.GetAccountRequestMessage;
import com.finastra.gateway.config.comm.configs.CommunicationConfig;
import com.finastra.gateway.config.comm.configs.ComplianceValidationConfig;
import com.finastra.gateway.config.comm.configs.GetAccountConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigService {

    @Value("gateway.mode")
    private String mode;

    private List<String> serviceModes;

    @Value("${vertx.eventbus.address}")
    private String eventBusAddress;

    private Map<Class<? extends GatewayMessage>, ? extends CommunicationConfig>  communicationTypeResolver;

    @PostConstruct
    public void init() {
        serviceModes = Arrays.stream(mode.split(",")).collect(Collectors.toList());
        initCommunicationTypeResolver();
    }

    private void initCommunicationTypeResolver() {
        // TODO OBY: Make sure only messages that are compatible in launched mode will be filling the map.
        Map<Class<? extends GatewayMessage>, CommunicationConfig> configs = new HashMap<>();
        configs.put(GetAccountRequestMessage.class, new GetAccountConfig());
        configs.put(ComplianceValidationRequestMessage.class, new ComplianceValidationConfig());

        communicationTypeResolver = configs;
    }


    public String getEventBusAddress() {
        return eventBusAddress;
    }

    public <T extends GatewayMessage> CommunicationConfig resolveCommunicationType(T message) {
        Class<? extends GatewayMessage> cls = message.getClass();
        return Optional.ofNullable(communicationTypeResolver.get(cls))
                .orElseThrow(() -> new IllegalArgumentException(cls + " isn't supported on mode " + serviceModes.toString()));
    }

}
