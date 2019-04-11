package com.finastra.gateway.vertx.codecs;

import com.finastra.gateway.common.messages.ComplianceValidationRequestMessage;

@Codec
public class ComplianceValidationRequestMessageCodec extends GatewayMessageCodec<ComplianceValidationRequestMessage, ComplianceValidationRequestMessage> {

    @Override
    public Class<ComplianceValidationRequestMessage> getOutType() {
        return ComplianceValidationRequestMessage.class;
    }

}
