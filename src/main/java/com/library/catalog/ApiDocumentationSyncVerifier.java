package com.library.catalog;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiDocumentationSyncVerifier {

    public static void main(String[] args) throws Exception {
        String expected = ApiDocumentationGenerator.generateMarkdown();
        Path docsPath = Path.of("docs", "api.md");
        if (!Files.exists(docsPath)) {
            throw new IllegalStateException("Missing API documentation file: " + docsPath.toAbsolutePath());
        }

        String actual = Files.readString(docsPath, StandardCharsets.UTF_8);
        if (!expected.equals(actual)) {
            throw new IllegalStateException(
                    "ERROR: API documentation is out of sync. Run the API doc generator to update docs/api.md."
            );
        }

        System.out.println("API documentation is in sync.");
    }
}
