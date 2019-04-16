package com.finastra.gateway.services;

import com.finastra.gateway.common.Constants;
import org.apache.camel.Predicate;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public Predicate isAuthorized() {
        return exchange -> "Drag the waters".equals(exchange.getIn().getHeader(Constants.AUTHORIZTION));
    }
}
