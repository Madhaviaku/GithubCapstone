package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Pattern;

public final class JiraClient {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern ISSUE_KEY = Pattern.compile("[A-Za-z][A-Za-z0-9_]*-\\d+");

    private final URI baseUri;
    private final String authorization;
    private final HttpClient httpClient;

    public JiraClient(String baseUrl, String email, String apiToken) {
        this.baseUri = validateBaseUrl(baseUrl);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Jira email must not be blank.");
        }
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalArgumentException("Jira API token must not be blank.");
        }

        String credentials = email + ":" + apiToken;
        this.authorization = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Issue getIssue(String issueKey) throws IOException, InterruptedException {
        if (issueKey == null || !ISSUE_KEY.matcher(issueKey).matches()) {
            throw new IllegalArgumentException("Invalid Jira issue key.");
        }

        URI issueUri = URI.create(baseUri + "/rest/api/3/issue/" + issueKey
                + "?fields=summary,status,assignee");
        HttpRequest request = HttpRequest.newBuilder(issueUri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("Authorization", authorization)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Jira returned HTTP " + response.statusCode()
                    + responseDetails(response.body()));
        }

        JsonNode root = JSON.readTree(response.body());
        JsonNode fields = root.path("fields");
        String key = requiredText(root.path("key"), "key");
        String summary = requiredText(fields.path("summary"), "fields.summary");
        String status = requiredText(fields.path("status").path("name"), "fields.status.name");
        JsonNode assigneeNode = fields.path("assignee");
        String assignee = assigneeNode.isMissingNode() || assigneeNode.isNull()
                ? "Unassigned"
                : requiredText(assigneeNode.path("displayName"), "fields.assignee.displayName");

        return new Issue(key, summary, status, assignee);
    }

    private static URI validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Jira base URL must not be blank.");
        }

        URI uri;
        try {
            uri = URI.create(baseUrl.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Jira base URL is not a valid URL.", exception);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || (uri.getPath() != null && !uri.getPath().isEmpty() && !"/".equals(uri.getPath()))
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            throw new IllegalArgumentException(
                    "Jira base URL must be an HTTPS origin, such as https://example.atlassian.net.");
        }
        return URI.create("https://" + uri.getRawAuthority());
    }

    private static String requiredText(JsonNode node, String fieldName) throws IOException {
        if (!node.isTextual() || node.asText().isBlank()) {
            throw new IOException("Jira response is missing " + fieldName + ".");
        }
        return node.asText();
    }

    private static String responseDetails(String body) {
        if (body == null || body.isBlank()) {
            return ".";
        }
        String details = body.replaceAll("\\s+", " ").trim();
        return ": " + details.substring(0, Math.min(details.length(), 500));
    }

    public record Issue(String key, String summary, String status, String assignee) {
    }
}
