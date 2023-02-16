package com.mario.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class EventHelper {

    ObjectMapper mapper;

    public EventHelper(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    public PlacedOrderEvent deserialize(String event) {
        PlacedOrderEvent placedOrderEvent;
        try {
            String unescaped = mapper.readValue(event, String.class);
            placedOrderEvent = mapper.readValue(unescaped, PlacedOrderEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't deserialize event", e);
        }
        return placedOrderEvent;
    }

    public String getHeaderAsString(Headers headers, String name) {
        var value = headers.lastHeader(name);
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException(
                    String.format("Expected record header %s not present", name));
        }
        return new String(value.value(), StandardCharsets.UTF_8);
    }

}
