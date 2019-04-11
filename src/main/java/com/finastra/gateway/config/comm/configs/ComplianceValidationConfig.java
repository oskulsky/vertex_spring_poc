package com.finastra.gateway.config.comm.configs;

public class ComplianceValidationConfig implements CommunicationConfig {


    @Override
    public CommunicationType type() {
        return CommunicationType.MQ;
    }
}
