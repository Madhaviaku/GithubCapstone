# Jira issue reader

This Java command-line application reads a Jira Cloud issue by key using the Jira
REST API v3. It prints the issue key, summary, status, and assignee.

Configure these environment variables before running:

- `JIRA_BASE_URL`: 'https://rd-training-oct-batch.atlassian.net'
- `JIRA_EMAIL`: 'Madhavi_Akula@epam.com'
- `JIRA_API_TOKEN`: 'ATATT3xFfGF0sXMiyJAzFaQmxc0tkqNTW5ixEY683Ho2pKIjaZXqeOt3EfxRocK291Q-KH7QK3v2ozmhNYhgQw2eMzjuFNcQlSwPmUW5q_a9RgCLOLj0sjAkAD7dyG84FiNjjdZzq8R1NcvY34qBBuKU6vygaCN5H_XutEhAeHkS7vYX4e9j9qs=F06D9AB4'


Do not commit the API token to source control. From the project directory, run:

```text
mvn compile exec:java -Dexec.mainClass=org.example.Main -Dexec.args="EPMCDMETST"
```

Replace `PROJ-123` with the issue key to read. The account used by the API token
must have permission to browse that issue.
