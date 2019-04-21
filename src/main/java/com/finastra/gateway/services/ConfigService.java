package com.finastra.gateway.services;

import com.finastra.gateway.common.messages.ComplianceValidationRequestMessage;
import com.finastra.gateway.common.messages.GatewayMessage;
import com.finastra.gateway.common.messages.GetAccountRequestMessage;
import com.finastra.gateway.config.comm.configs.CommunicationConfig;
import com.finastra.gateway.config.comm.configs.ComplianceValidationConfig;
import com.finastra.gateway.config.comm.configs.GetAccountConfig;
import lombok.Builder;
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

    private Map<ConfigKey, ? extends CommunicationConfig> communicationTypeConfigMap;

    @PostConstruct
    public void init() {
        serviceModes = Arrays.stream(mode.split(",")).collect(Collectors.toList());
        initCommunicationTypeConfigMap();
    }

    private void initCommunicationTypeConfigMap() {
        // TODO OBY: Make sure only messages that are compatible in launched mode will be filling the map.

        String tenantId = "Ten-ant";

        Map<ConfigKey, CommunicationConfig> configs = new HashMap<>();
        addConfigEntry(configs, tenantId, GetAccountRequestMessage.class, new GetAccountConfig());
        addConfigEntry(configs, tenantId, ComplianceValidationRequestMessage.class, new ComplianceValidationConfig());
        communicationTypeConfigMap = configs;
    }

    private <T extends CommunicationConfig> void addConfigEntry(Map<ConfigKey, T> configMap, String tenantId, Class<? extends GatewayMessage> cls, T config) {
        ConfigKey key = ConfigKey.builder()
                .msgType(cls)
                .tenantId(tenantId)
                .build();
        configMap.put(key, config);
    }


    public String getEventBusAddress() {
        return eventBusAddress;
    }

    public <T extends GatewayMessage> CommunicationConfig resolveCommunicationType(String tenantId, T message) {
        ConfigKey key = ConfigKey.builder()
                .tenantId(tenantId)
                .msgType(message.getClass())
                .build();

        return Optional.ofNullable(communicationTypeConfigMap.get(key))
                .orElseThrow(() -> new IllegalArgumentException(key + " isn't supported on mode " + serviceModes.toString()));
    }

    @lombok.Value
    @Builder
    static class ConfigKey {
        private Class<? extends GatewayMessage> msgType;
        private String tenantId;
    }

}
