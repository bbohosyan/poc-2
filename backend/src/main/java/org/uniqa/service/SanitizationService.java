package org.uniqa.service;

import org.springframework.stereotype.Service;

@Service
public class SanitizationService {

    public String sanitize(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replaceAll("<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]*>", "")
                .replaceAll("javascript:", "")
                .replaceAll("on\\w+\\s*=", "");
    }
}
