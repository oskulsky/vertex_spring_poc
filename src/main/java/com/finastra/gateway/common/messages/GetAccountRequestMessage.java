package com.finastra.gateway.common.messages;

import lombok.Value;

@Value
public class GetAccountRequestMessage implements GatewayMessage {

    private String accountId;

    public GetAccountRequestMessage() {
        this("123");
    }

    public GetAccountRequestMessage(String accountId) {
        this.accountId = accountId;

    }
}
