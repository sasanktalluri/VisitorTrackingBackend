package com.visitortracker.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.visitortracker.model.dto.ExtractedPayment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/ocrss")
@CrossOrigin(origins = "http://localhost:3000")
public class OcrScreenshotController {

    /*
     * application.yml example:
     *
     * docai:
     *   endpoint: "https://us-documentai.googleapis.com"
     *   processor-name: "projects/442256500910/locations/us/processors/8d40f496a65ded91"
     *
     * gcp:
     *   credentials:
     *     file: "/absolute/secure/path/docai-sa.json"
     *     # OR:
     *     # jsonBase64: "BASE64_OF_SERVICE_ACCOUNT_JSON"
     */

    // --- pulled from application.yml ---
    @Value("${docai.endpoint}")
    private String docAiEndpoint; // e.g., https://us-documentai.googleapis.com

    @Value("${docai.processor-name}")
    private String processorName; // e.g., projects/.../locations/us/processors/...

    // Credentials (no env vars): pick file OR base64 via application.yml
    @Value("${gcp.credentials.file:}")
    private String credentialsFile;

    @Value("${gcp.credentials.jsonBase64:}")
    private String credentialsJsonBase64;

    private static final List<String> SCOPES =
            List.of("https://www.googleapis.com/auth/cloud-platform");

    private final RestTemplate restTemplate = new RestTemplate();

    /* ------------------------ Utilities ------------------------- */

    private String sanitize(String input) {
        return input == null ? null : input.replaceAll("\\r?\\n", " ").replaceAll("\\s{2,}", " ").trim();
    }

    private String extractFirst(String text, Pattern pattern) {
        if (text == null) return null;
        Matcher m = pattern.matcher(text);
        return m.find() ? sanitize(m.group(1)) : null;
    }

    private String detectMime(MultipartFile file) {
        String c = file.getContentType();
        if (c != null && !c.isBlank()) return c;
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".tif") || name.endsWith(".tiff")) return "image/tiff";
        if (name.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    /** Accepts: "9 Sep 2025", "09 Sep 2025, 10:22 AM", "09/09/2025 10:22 AM", "2025-09-09 10:22" */
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d MMM uuuu")
                    .optionalStart().appendLiteral(',').optionalEnd()
                    .optionalStart().appendLiteral(' ').appendPattern("h:mm a").optionalEnd()
                    .toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d/M/uuuu")
                    .optionalStart().appendLiteral(' ').appendPattern("h:mm a").optionalEnd()
                    .toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("uuuu-M-d")
                    .optionalStart().appendLiteral(' ').appendPattern("H:mm[:ss]").optionalEnd()
                    .toFormatter(Locale.ENGLISH)
    );

    private String parseDateFlexible(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim().replaceAll("\\s{2,}", " ");
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                TemporalAccessor ta = f.parse(s);
                LocalDate date = LocalDate.of(
                        ta.isSupported(ChronoField.YEAR) ? ta.get(ChronoField.YEAR) : LocalDate.now().getYear(),
                        ta.isSupported(ChronoField.MONTH_OF_YEAR) ? ta.get(ChronoField.MONTH_OF_YEAR) : LocalDate.now().getMonthValue(),
                        ta.isSupported(ChronoField.DAY_OF_MONTH) ? ta.get(ChronoField.DAY_OF_MONTH) : LocalDate.now().getDayOfMonth()
                );
                LocalTime time = LocalTime.of(
                        ta.isSupported(ChronoField.HOUR_OF_DAY) ? ta.get(ChronoField.HOUR_OF_DAY)
                                : (ta.isSupported(ChronoField.CLOCK_HOUR_OF_AMPM) ? (ta.get(ChronoField.CLOCK_HOUR_OF_AMPM) % 12) : 0),
                        ta.isSupported(ChronoField.MINUTE_OF_HOUR) ? ta.get(ChronoField.MINUTE_OF_HOUR) : 0,
                        ta.isSupported(ChronoField.SECOND_OF_MINUTE) ? ta.get(ChronoField.SECOND_OF_MINUTE) : 0
                );
                return DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").format(LocalDateTime.of(date, time));
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    // --------------- Credentials without environment variables ---------------
    private GoogleCredentials loadCredentials() throws IOException {
        if (credentialsJsonBase64 != null && !credentialsJsonBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(credentialsJsonBase64);
            try (InputStream in = new ByteArrayInputStream(decoded)) {
                return GoogleCredentials.fromStream(in).createScoped(SCOPES);
            }
        }
        if (credentialsFile != null && !credentialsFile.isBlank()) {
            try (InputStream in = new FileInputStream(credentialsFile)) {
                return GoogleCredentials.fromStream(in).createScoped(SCOPES);
            }
        }
        // If you want to enforce config-only, throw instead:
        // throw new IllegalStateException("No GCP credentials configured (file/jsonBase64).");
        return GoogleCredentials.getApplicationDefault().createScoped(SCOPES);
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials credentials = loadCredentials();
        synchronized (credentials) {
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        }
    }

    // --------------- Entity extraction from Document AI response ---------------

    /** Extracts entities like Amount, Date, From, confirmation_id directly from document.entities */
    @SuppressWarnings("unchecked")
    private Map<String, String> extractDocAiEntities(Map<?, ?> document) {
        Map<String, String> out = new LinkedHashMap<>();
        Object entsObj = document.get("entities");
        if (!(entsObj instanceof List<?> ents)) return out;

        for (Object o : ents) {
            if (!(o instanceof Map<?, ?> e)) continue;

            String type = Optional.ofNullable(e.get("type")).map(Object::toString).orElse("").trim();
            if (type.isEmpty()) continue;

            String value = bestEntityValue(e);
            if (value == null || value.isBlank()) continue;

            // Key normalized to lower case (e.g., "amount", "date", "from", "confirmation_id")
            out.put(type.toLowerCase(Locale.ROOT), value.trim());
        }
        return out;
    }

    /** Prefer normalizedValue (text/dateValue/moneyValue), else mentionText */
    @SuppressWarnings("unchecked")
    private String bestEntityValue(Map<?, ?> entity) {
        Object nvObj = entity.get("normalizedValue");
        if (nvObj instanceof Map<?, ?> nv) {
            // 1) normalizedValue.text (already normalized by DocAI)
            String text = Optional.ofNullable(nv.get("text")).map(Object::toString).orElse(null);
            if (text != null && !text.isBlank()) return text;

            // 2) dateValue { year, month, day }
            Object dvObj = nv.get("dateValue");
            if (dvObj instanceof Map<?, ?> dv) {
                Integer y = asInt(dv.get("year"));
                Integer m = asInt(dv.get("month"));
                Integer d = asInt(dv.get("day"));
                if (y != null && m != null && d != null) {
                    return String.format("%04d-%02d-%02d", y, m, d); // ISO date
                }
            }

            // 3) moneyValue { currencyCode, units, nanos }
            Object mvObj = nv.get("moneyValue");
            if (mvObj instanceof Map<?, ?> mv) {
                String currency = Optional.ofNullable(mv.get("currencyCode")).map(Object::toString).orElse("");
                BigDecimal amount = moneyToBigDecimal(mv.get("units"), mv.get("nanos"));
                if (amount != null) {
                    return (currency.isBlank() ? "" : (currency + " ")) + amount.toPlainString();
                }
            }
        }

        // Fallback: mentionText
        return Optional.ofNullable(entity.get("mentionText")).map(Object::toString).orElse(null);
    }

    private Integer asInt(Object o) {
        if (o == null) return null;
        try { return Integer.parseInt(o.toString()); } catch (NumberFormatException e) { return null; }
    }

    private BigDecimal moneyToBigDecimal(Object unitsObj, Object nanosObj) {
        try {
            long units = unitsObj == null ? 0L : Long.parseLong(unitsObj.toString());
            long nanos = nanosObj == null ? 0L : Long.parseLong(nanosObj.toString());
            BigDecimal bd = new BigDecimal(units).add(new BigDecimal(nanos).movePointLeft(9));
            return bd.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private double parseAmount(String s) {
        String cleaned = s.replaceAll("[^0-9.,]", "");
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.replaceAll(",", "");
        } else if (cleaned.contains(",") && !cleaned.contains(".")) {
            cleaned = cleaned.replace(',', '.');
        }
        try { return Double.parseDouble(cleaned); } catch (NumberFormatException e) { return 0.0; }
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... vals) {
        for (T v : vals) if (v != null && !(v instanceof String s && s.isBlank())) return v;
        return null;
    }

    /* ------------------------ Endpoint ------------------------- */

    @PostMapping("/extract-payment")
    public ResponseEntity<List<ExtractedPayment>> extractPayments(@RequestParam("file") MultipartFile file) {
        try {
            // Prepare request
            String mime = detectMime(file);
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String token = getAccessToken();
            String url = docAiEndpoint + "/v1/" + processorName + ":process";

            String requestJson = """
            {
              "skipHumanReview": true,
              "rawDocument": {
                "mimeType": "%s",
                "content": "%s"
              }
            }
            """.formatted(mime, base64);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(requestJson, headers), Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Collections.emptyList());
            }

            // Parse response
            Object docObj = response.getBody().get("document");
            if (!(docObj instanceof Map<?, ?> document)) return ResponseEntity.ok(Collections.emptyList());

            String ocrText = sanitize((String) document.get("text"));
            Map<String, String> ents = extractDocAiEntities(document);

            // Entities (prefer DocAI values; fallback to text heuristics)
            String amountEnt = ents.get("amount");
            String dateEnt   = ents.get("date");
            String fromEnt   = ents.get("from");
            String confEnt   = ents.get("confirmation_id");

            String sender = firstNonNull(
                    fromEnt,
                    extractFirst(ocrText, Pattern.compile("(?i)From\\s*[:\\-]?\\s*(.+)")),
                    extractFirst(ocrText, Pattern.compile("(?i)Paid\\s*to\\s*[:\\-]?\\s*(.+)")),
                    extractFirst(ocrText, Pattern.compile("(?i)To\\s*[:\\-]?\\s*(.+)"))
            );

            String amountStr = firstNonNull(
                    amountEnt,
                    extractFirst(ocrText, Pattern.compile("[₹\\u20B9]\\s*([0-9]+(?:[.,][0-9]{2})?)")),
                    extractFirst(ocrText, Pattern.compile("\\$\\s*([0-9]+(?:[.,][0-9]{2})?)")),
                    extractFirst(ocrText, Pattern.compile("(?i)(?:Amount|Total|Paid)\\s*[:=]?\\s*([0-9]+(?:[.,][0-9]{1,2})?)"))
            );

            String rawDate = firstNonNull(
                    dateEnt,
                    extractFirst(ocrText, Pattern.compile("(\\d{1,2}\\s+(?:[A-Za-z]{3,9})\\s+\\d{4}(?:,?\\s+\\d{1,2}:\\d{2}\\s*[APMapm]{2})?)")),
                    extractFirst(ocrText, Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{2,4}(?:\\s+\\d{1,2}:\\d{2}\\s*[APMapm]{2})?)"))
            );

            String txnId = firstNonNull(
                    confEnt,
                    extractFirst(ocrText, Pattern.compile("(?i)Google\\s*transaction\\s*ID\\s*[:#\\-]?\\s*([\\w\\-]+)")),
                    extractFirst(ocrText, Pattern.compile("(?i)UPI\\s*transaction\\s*ID\\s*[:#\\-]?\\s*([\\w\\-]+)")),
                    extractFirst(ocrText, Pattern.compile("(?i)(?:Ref(?:\\.|erence)?|Txn|Transaction)\\s*[:#\\-]?\\s*([A-Za-z0-9\\-]+)"))
            );

            double amount = amountStr != null ? parseAmount(amountStr) : 0.0;
            String formattedDate = parseDateFlexible(rawDate);

            List<ExtractedPayment> results = new ArrayList<>();
            if (sender != null && amount > 0) {
                ExtractedPayment p = new ExtractedPayment();
                p.setName(sender);
                p.setPhoneNumber(txnId);             // you were storing txnId here
                p.setServiceType("UNKNOWN");
                p.setCategory("UPI/ZELLE");    // change if needed (e.g., "Bank Transfer")
                p.setAmount(amount);
                p.setTimestamp(formattedDate);
                results.add(p);
            }

            return ResponseEntity.ok(results);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
