package com.finastra.gateway.vertx.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finastra.gateway.common.messages.GatewayMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class GatewayMessageCodec<I extends GatewayMessage, O extends GatewayMessage> implements MessageCodec<I, O> {

    private ObjectMapper mapper = new ObjectMapper();

    public abstract Class<O> getOutType();

    @Override
    public void encodeToWire(Buffer buffer, I obj) { // TODO OBY: Serialize differently
        try {
            String json = mapper.writeValueAsString(obj);
            buffer.appendInt(json.getBytes(StandardCharsets.UTF_8).length);
            buffer.appendString(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public O decodeFromWire(int pos, Buffer buffer) { // TODO OBY: Deserialize differently
        int byteCount = buffer.getInt(pos);
        try {
            return mapper.readValue(buffer.getString(pos + 4, pos + byteCount), getOutType()); // 4 is the size on an int
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public O transform(I obj) {
        return (O) obj;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}