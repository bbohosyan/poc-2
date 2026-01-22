package com.sap.export;

import com.sap.entity.TableRow;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component("csvExport")
public class CsvExportStrategy implements ExportStrategy {

    @Override
    public byte[] export(List<TableRow> data) {
        StringBuilder csv = new StringBuilder();

        csv.append("ID,Type Number,Type Selector,Type Free Text,Created At\n");

        for (TableRow row : data) {
            csv.append(row.getId()).append(",")
                    .append(row.getTypeNumber()).append(",")
                    .append(escapeCsv(row.getTypeSelector())).append(",")
                    .append(escapeCsv(row.getTypeFreeText())).append(",")
                    .append(row.getCreatedAt()).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}