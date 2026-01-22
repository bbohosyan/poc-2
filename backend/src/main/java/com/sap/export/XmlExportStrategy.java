package com.sap.export;

import com.sap.entity.TableRow;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component("xmlExport")
public class XmlExportStrategy implements ExportStrategy {

    @Override
    public byte[] export(List<TableRow> data) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rows>\n");

        for (TableRow row : data) {
            xml.append("  <row>\n");
            xml.append("    <id>").append(row.getId()).append("</id>\n");
            xml.append("    <typeNumber>").append(row.getTypeNumber()).append("</typeNumber>\n");
            xml.append("    <typeSelector>").append(escapeXml(row.getTypeSelector())).append("</typeSelector>\n");
            xml.append("    <typeFreeText>").append(escapeXml(row.getTypeFreeText())).append("</typeFreeText>\n");
            xml.append("    <createdAt>").append(row.getCreatedAt()).append("</createdAt>\n");
            xml.append("  </row>\n");
        }

        xml.append("</rows>");
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}