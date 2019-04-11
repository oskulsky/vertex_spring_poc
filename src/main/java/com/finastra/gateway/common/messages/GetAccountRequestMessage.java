package com.finastra.gateway.common.messages;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class GetAccountRequestMessage implements GatewayMessage {

    String accountId;
}
