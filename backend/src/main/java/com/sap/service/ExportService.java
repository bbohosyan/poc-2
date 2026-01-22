package com.sap.service;

import com.sap.entity.TableRow;
import com.sap.export.ExportStrategy;
import com.sap.export.ExportStrategyFactory;
import com.sap.repository.TableRowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportService {

    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private TableRowRepository repository;

    @Autowired
    private ExportStrategyFactory strategyFactory;

    public ExportResult export(String format) {
        LOG.info("Starting export in {} format", format);

        List<TableRow> data = fetchData();

        ExportStrategy strategy = strategyFactory.getStrategy(format);

        byte[] exportedData = strategy.export(data);

        ExportResult result = new ExportResult(
                exportedData,
                strategy.getContentType(),
                generateFileName(format, strategy.getFileExtension())
        );

        LOG.info("Export completed: {} bytes, format: {}", exportedData.length, format);

        return result;
    }

    protected List<TableRow> fetchData() {
        return repository.findAll();
    }

    protected String generateFileName(String format, String extension) {
        return "export_" + System.currentTimeMillis() + "." + extension;
    }

    public static class ExportResult {
        private final byte[] data;
        private final String contentType;
        private final String fileName;

        public ExportResult(byte[] data, String contentType, String fileName) {
            this.data = data;
            this.contentType = contentType;
            this.fileName = fileName;
        }

        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public String getFileName() { return fileName; }
    }
}