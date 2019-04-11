package com.finastra.gateway.config.comm.configs;

public class GetAccountConfig implements CommunicationConfig {

    @Override
    public CommunicationType type() {
        return CommunicationType.REST;
    }
}
