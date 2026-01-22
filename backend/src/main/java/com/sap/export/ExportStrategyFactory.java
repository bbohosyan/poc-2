package com.sap.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExportStrategyFactory {

    private final Map<String, ExportStrategy> strategies;

    @Autowired
    public ExportStrategyFactory(Map<String, ExportStrategy> strategies) {
        this.strategies = strategies;
    }

    public ExportStrategy getStrategy(String format) {
        String beanName = format.toLowerCase() + "Export";

        ExportStrategy strategy = strategies.get(beanName);

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        return strategy;
    }
}