package com.sap.export;

import com.sap.entity.TableRow;
import java.util.List;

public interface ExportStrategy {
    byte[] export(List<TableRow> data);
    String getContentType();
    String getFileExtension();
}