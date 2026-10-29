package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar GithubCapstone.jar <ISSUE-KEY>");
            System.exit(2);
            return;
        }

        try {
            JiraClient jira = new JiraClient(
                    requiredEnvironmentVariable("JIRA_BASE_URL"),
                    requiredEnvironmentVariable("JIRA_EMAIL"),
                    requiredEnvironmentVariable("JIRA_API_TOKEN"));
            JiraClient.Issue issue = jira.getIssue(args[0]);

            System.out.println("Key: " + issue.key());
            System.out.println("Summary: " + issue.summary());
            System.out.println("Status: " + issue.status());
            System.out.println("Assignee: " + issue.assignee());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Jira request was interrupted.");
            System.exit(1);
        } catch (IOException | IllegalArgumentException | IllegalStateException exception) {
            System.err.println("Jira request failed: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is not set: " + name);
        }
        return value;
    }
}
