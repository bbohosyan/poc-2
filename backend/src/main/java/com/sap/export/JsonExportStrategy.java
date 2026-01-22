package com.sap.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.entity.TableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("jsonExport")
public class JsonExportStrategy implements ExportStrategy {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public byte[] export(List<TableRow> data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export to JSON", e);
        }
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}