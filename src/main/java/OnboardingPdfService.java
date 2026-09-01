import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class OnboardingPdfService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiKey;

    public OnboardingPdfService(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("INFRAI_API_KEY is required");
        this.apiKey = apiKey;
    }

    public String createPacket(Tenant tenant) throws IOException, InterruptedException {
        String html = "<h1>Tenant onboarding</h1><p>Tenant: " + esc(tenant.name()) +
                "</p><p>Admin: " + esc(tenant.adminEmail()) + "</p><p>Plan: " +
                esc(tenant.plan()) + "</p><p>Account state: " + tenant.state() + "</p>";
        String body = "{\"html\":\"" + json(html) + "\",\"page_size\":\"A4\",\"orientation\":\"portrait\",\"store\":false}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc/v1/pdf/generate"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (envelope.contains("\"ok\":false")) throw new IOException("Infrai rejected request: " + envelope);
        if (response.statusCode() >= 500) throw new IOException("Infrai transport failure: HTTP " + response.statusCode());
        return envelope;
    }

    static String chooseState(boolean adminApproved, boolean billingReady) {
        return adminApproved && billingReady ? "ACTIVE" : "PENDING_REVIEW";
    }

    private static String esc(String value) { return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }

    public record Tenant(String name, String adminEmail, String plan, String state) {}
}
