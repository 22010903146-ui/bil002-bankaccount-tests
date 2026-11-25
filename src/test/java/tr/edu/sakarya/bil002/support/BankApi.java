package tr.edu.sakarya.bil002.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** HTTP contract adapter. No banking implementation is substituted for the teacher's API. */
public class BankApi {
    public record Reply(int status, JsonNode json) { }
    public interface Transport { Reply send(String method, String path, String body); }
    public static final ObjectMapper JSON = new ObjectMapper();
    private final Transport transport;

    public BankApi() { this(new HttpTransport()); }
    public BankApi(Transport transport) { this.transport = transport; }

    public Reply send(String method, String path, String body) {
        return transport.send(method, path, body);
    }
    public static String json(Object value) {
        try { return JSON.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalArgumentException(e); }
    }
    public Reply createBody(String body) { return send("POST", "/api/accounts", body); }
    public String create(String type, Double balance) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ownerName", "Test-" + UUID.randomUUID());
        body.put("accountType", type);
        body.put("initialBalance", balance);
        Reply reply = createBody(json(body));
        code(reply, 201);
        String number = reply.json().path("accountNumber").asText();
        assertTrue(number.matches("[1-9][0-9]{9}"), "R-1.5: accountNumber=" + number);
        return number;
    }
    public JsonNode account(String number) {
        Reply reply = send("GET", "/api/accounts/" + number, null);
        code(reply, 200);
        assertTrue(reply.json().path("balance").isNumber(), "Account balance must be numeric");
        assertTrue(reply.json().path("status").isTextual(), "Account status must be present");
        assertEquals(number, reply.json().path("accountNumber").asText());
        return reply.json();
    }
    public double balance(String number) { return account(number).path("balance").asDouble(); }
    public JsonNode history(String number) {
        Reply reply = send("GET", "/api/accounts/" + number + "/transactions", null);
        code(reply, 200);
        assertTrue(reply.json().isArray(), "History must be an array");
        return reply.json();
    }
    public Reply operation(String number, String operation, Double amount) {
        String body = amount == null ? null : json(Map.of("amount", amount));
        return send("POST", "/api/accounts/" + number + "/" + operation, body);
    }
    public Reply transfer(String from, String to, double amount) {
        return send("POST", "/api/transactions/transfer", json(Map.of(
                "fromAccount", from, "toAccount", to, "amount", amount)));
    }
    public void state(String number, String target) {
        if (target.equals("FROZEN")) { code(operation(number, "freeze", null), 200); }
        else if (target.equals("CLOSED")) { code(operation(number, "close", null), 200); }
        else { assertEquals("ACTIVE", target); }
    }
    public static void code(Reply reply, int expected) {
        assertEquals(expected, reply.status(), "HTTP body: " + reply.json());
    }
    public static void error(Reply reply, int expected) {
        code(reply, expected);
        assertAll("Error JSON (requirements section 7)",
                () -> assertEquals(expected, reply.json().path("status").asInt()),
                () -> assertTrue(reply.json().path("message").isTextual()),
                () -> assertTrue(reply.json().path("error").isTextual()),
                () -> assertTrue(reply.json().hasNonNull("timestamp")),
                () -> assertTrue(reply.json().path("path").isTextual()));
    }
    public void unchanged(String number, JsonNode before) {
        JsonNode after = account(number);
        assertEquals(before.path("balance").asDouble(), after.path("balance").asDouble(), 0.001, "G-4 balance");
        assertEquals(before.path("status").asText(), after.path("status").asText(), "G-4 state");
    }
    public void rejected(String number, String operation, double amount, int status) {
        JsonNode before = account(number);
        Reply reply = operation(number, operation, amount);
        assertAll(() -> error(reply, status), () -> unchanged(number, before));
    }
    public void rejectedTransition(String number, String operation) {
        JsonNode before = account(number);
        Reply reply = operation(number, operation, null);
        assertAll(() -> error(reply, 409), () -> unchanged(number, before));
    }
    public void rejectedTransfer(String from, String to, double amount, int status) {
        JsonNode a = account(from);
        JsonNode b = account(to);
        Reply reply = transfer(from, to, amount);
        assertAll(() -> error(reply, status), () -> unchanged(from, a), () -> unchanged(to, b));
    }
    private static final class HttpTransport implements Transport {
        private final URI base;
        private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        private HttpTransport() {
            String configured = System.getProperty("baseUrl", System.getenv().getOrDefault("BANK_BASE_URL", "http://localhost:8080"));
            base = URI.create(configured.replaceAll("/+$", ""));
            if (base.getHost() == null || !(base.getScheme().equals("http") || base.getScheme().equals("https"))) {
                throw new IllegalArgumentException("BANK_BASE_URL must be an http(s) base URL");
            }
        }
        public Reply send(String method, String path, String body) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(base.toString() + path))
                    .timeout(Duration.ofSeconds(20)).header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 BIL002-Student-Test");
            if (body == null) { builder.method(method, HttpRequest.BodyPublishers.noBody()); }
            else { builder.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body)); }
            try {
                HttpResponse<String> result = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                String contentType = result.headers().firstValue("Content-Type").orElse("");
                assertTrue(contentType.toLowerCase().contains("application/json"), "G-3: expected JSON content type");
                return new Reply(result.statusCode(), JSON.readTree(result.body()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Request interrupted", e);
            } catch (java.io.IOException e) {
                // Deliberately omit URL and token from error output.
                throw new AssertionError("API connection/JSON failed. Start the original API and check BANK_BASE_URL. "
                        + e.getClass().getSimpleName());
            }
        }
    }
}
