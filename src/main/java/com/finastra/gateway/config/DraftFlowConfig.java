package com.finastra.gateway.config;

import com.finastra.gateway.common.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DraftFlowConfig {

    @ConditionalOnProperty(prefix = Constants.CONFIG_PREFIX, name = Constants.DIRECTION, value = Constants.EGRESS)
    public void egressTransform() {

    }

    @ConditionalOnProperty(prefix = Constants.CONFIG_PREFIX, name = Constants.DIRECTION, value = Constants.EGRESS)
    public void egressAuth() {

    }

    @ConditionalOnProperty(prefix = Constants.CONFIG_PREFIX, name = Constants.DIRECTION, value = Constants.INGRESS)
    public void ingressTransform() {

    }

}
