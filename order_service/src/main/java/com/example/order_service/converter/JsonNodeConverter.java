package com.example.order_service.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }

        try {
            return mapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert JSON to String", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }

        try {
            return mapper.readTree(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize JsonNode", e);
        }
    }
}
