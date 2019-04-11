package com.finastra.gateway.vertx.codecs;

import com.finastra.gateway.common.messages.GetAccountRequestMessage;

@Codec
public class GetAccountRequestMessageCodec extends GatewayMessageCodec<GetAccountRequestMessage, GetAccountRequestMessage> {

    @Override
    public Class<GetAccountRequestMessage> getOutType() {
        return GetAccountRequestMessage.class;
    }
}
