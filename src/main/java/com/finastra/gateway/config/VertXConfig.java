package com.finastra.gateway.config;

import com.finastra.gateway.vertx.codecs.Codec;
import com.finastra.gateway.vertx.codecs.GatewayMessageCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@Slf4j
public class VertXConfig {

    @Bean
    public Vertx vertx() {
        Vertx vertx = Vertx.vertx();
        registerMessageCodecs(vertx);
        return vertx;
    }

    private void registerMessageCodecs(Vertx vertx) {
        EventBus bus = vertx.eventBus();
        Set<Class<?>> codecs = new Reflections("com.finastra.gateway").getTypesAnnotatedWith(Codec.class);
        codecs.stream()
                .map(cls -> {
                    try {
                        return (GatewayMessageCodec) cls.newInstance();
                    } catch (Exception e) {
                        log.error("Failed to instantiate Message-Codec (codec: " + cls.getSimpleName() + ")", e);
                        throw new RuntimeException(e);
                    }
                })
                .peek(codec -> log.info("Registering codec " + codec.getClass().getName()
                        + " (message type: " + codec.getOutType() + ")"))
                .forEach(codec -> bus.registerDefaultCodec(codec.getOutType(), codec));
    }

    @Bean
    @ConditionalOnProperty(prefix = "oby.", value = "trance")
    public void oby () {
        log.info("Hi oby");
    }

    @Bean
    @ConditionalOnProperty(prefix = "oby.", value = "trance", havingValue = "nope")
    public void oby1() {
        log.info("Hi oby1");
    }

}
