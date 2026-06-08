package com.devdoyen.nemologic.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GridConverter implements AttributeConverter<int[][], String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(int[][] attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting grid to JSON string", e);
        }
    }

    @Override
    public int[][] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new int[0][0];
        }
        try {
            return objectMapper.readValue(dbData, int[][].class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON string to grid", e);
        }
    }
}
