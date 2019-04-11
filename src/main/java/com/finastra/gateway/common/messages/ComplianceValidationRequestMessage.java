package com.finastra.gateway.common.messages;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class ComplianceValidationRequestMessage implements GatewayMessage {

    String firstname;

    String surname;

    String id;

}
