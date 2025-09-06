package com.visitortracker.controller;

import com.visitortracker.model.dto.ExtractedPayment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.Base64;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "http://localhost:3000")
public class OcrController {

    @Value("${google.vision.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private String sanitize(String input) {
        return input == null ? null : input.replaceAll("\\n", "").trim();
    }

    @PostMapping("/extract-payments")
    public ResponseEntity<List<ExtractedPayment>> extractPayments(@RequestParam("file") MultipartFile file) throws IOException {
        // Convert image to base64
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        // Prepare request payload for Google Vision API
        String requestJson = """
        {
          "requests": [
            {
              "image": {
                "content": "%s"
              },
              "features": [
                {
                  "type": "DOCUMENT_TEXT_DETECTION"
                }
              ]
            }
          ]
        }
        """.formatted(base64Image);

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare entity
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        // Build URL
        String url = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;

        // Send POST request
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            // Parse OCR text
            String ocrText = (String) ((Map<?, ?>) ((Map<?, ?>) ((List<?>) response.getBody().get("responses")).get(0)).get("fullTextAnnotation")).get("text");
            //System.out.println(ocrText);
            // Split into chunks by each payment block
            String[] blocks = ocrText.split("(?=Name:)");
            List<ExtractedPayment> payments = new ArrayList<>();

            for (String block : blocks) {
                String name = sanitize(extractField(block, "(?i)Name\\s*[:\\-]?\\s*(\\w+)"));
                String phone = sanitize(extractField(block, "(?i)Phone\\s*[:\\-]?\\s*([\\d\\s]+)"));
                String service = sanitize(extractField(block, "(?i)Service\\s*[:\\-]?\\s*(\\w+)"));
                String category = sanitize(extractField(block, "(?i)Category\\s*[:\\-]?\\s*([\\w\\s]+)"));
                String amountStr = sanitize(extractField(block, "(?i)Amount\\s*[:\\-]?\\s*\\$?([\\d\\s,\\.]+)"));
                String date = sanitize(extractField(block, "(?i)Date\\s*[:\\-]?\\s*(\\d{2}/\\d{2}/\\d{4})"));
                try {
                    if (name != null && amountStr != null) {
                        double amount = Double.parseDouble(amountStr.replaceAll("[^\\d.]", ""));
                        payments.add(new ExtractedPayment(
                                name.trim(),
                                phone,
                                service,
                                category,
                                amount,
                                date
                        ));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    private String extractField(String input, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }
}
