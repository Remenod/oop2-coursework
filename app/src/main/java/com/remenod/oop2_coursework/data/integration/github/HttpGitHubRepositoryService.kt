package com.remenod.oop2_coursework.data.integration.github

import com.remenod.oop2_coursework.domain.model.GitHubRepositoryLink
import com.remenod.oop2_coursework.domain.model.GitHubRepositorySnapshot
import com.remenod.oop2_coursework.domain.model.GitHubWorkCandidate
import com.remenod.oop2_coursework.domain.model.GitHubWorkCandidateState
import com.remenod.oop2_coursework.domain.model.GitHubWorkCandidateType
import com.remenod.oop2_coursework.domain.service.GitHubRepositoryService
import com.remenod.oop2_coursework.domain.service.GitHubRepositorySyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class HttpGitHubRepositoryService(
    private val client: GitHubHttpClient = UrlConnectionGitHubHttpClient()
) : GitHubRepositoryService {

    override suspend fun sync(repository: GitHubRepositoryLink): GitHubRepositorySyncResult {
        val info = repository.repositoryInfo ?: error("Invalid GitHub repository URL")
        val baseApiUrl = "https://api.github.com/repos/${info.owner}/${info.repository}"

        val repoJson = client.get(baseApiUrl)
        val issuesJson = client.get("$baseApiUrl/issues?state=open&per_page=100")
        val pullsJson = client.get("$baseApiUrl/pulls?state=open&per_page=100")

        val defaultBranch = stringValue(repoJson, "default_branch")
            ?: repository.effectiveBranch
            ?: repository.repositorySnapshot.defaultBranch
            ?: "main"
        val lastActivityAt = stringValue(repoJson, "pushed_at")
            ?.toLocalDateTimeOrNull()
            ?: stringValue(repoJson, "updated_at")?.toLocalDateTimeOrNull()

        val issueCandidates = arrayObjects(issuesJson)
            .filterNot { it.contains("\"pull_request\"") }
            .mapNotNull { it.toCandidate(GitHubWorkCandidateType.ISSUE) }

        val pullRequestCandidates = arrayObjects(pullsJson)
            .mapNotNull { it.toCandidate(GitHubWorkCandidateType.PULL_REQUEST) }

        val snapshot = GitHubRepositorySnapshot(
            activeIssuesCount = issueCandidates.size,
            openPullRequestsCount = pullRequestCandidates.size,
            defaultBranch = defaultBranch,
            lastRepositoryActivityAt = lastActivityAt,
            syncedAt = LocalDateTime.now(),
            workCandidates = issueCandidates + pullRequestCandidates
        )

        val message = "GitHub sync complete: ${snapshot.activeIssuesCount} active issue(s), " +
                "${snapshot.openPullRequestsCount} open PR(s)"
        return GitHubRepositorySyncResult(snapshot, message)
    }

    private fun String.toCandidate(type: GitHubWorkCandidateType): GitHubWorkCandidate? {
        val number = intValue(this, "number") ?: return null
        val title = stringValue(this, "title") ?: return null
        val url = stringValue(this, "html_url") ?: return null
        val state = when {
            type == GitHubWorkCandidateType.PULL_REQUEST && booleanValue(this, "draft") == true -> GitHubWorkCandidateState.DRAFT
            stringValue(this, "state") == "closed" -> GitHubWorkCandidateState.CLOSED
            else -> GitHubWorkCandidateState.OPEN
        }

        return GitHubWorkCandidate(
            type = type,
            number = number,
            title = title,
            url = url,
            state = state,
            createdAt = stringValue(this, "created_at")?.toLocalDateTimeOrNull(),
            updatedAt = stringValue(this, "updated_at")?.toLocalDateTimeOrNull()
        )
    }

    private fun stringValue(json: String, key: String): String? {
        val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")
        return pattern.find(json)?.groupValues?.get(1)?.unescapeJson()
    }

    private fun intValue(json: String, key: String): Int? {
        val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(\\d+)")
        return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun booleanValue(json: String, key: String): Boolean? {
        val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(true|false)")
        return pattern.find(json)?.groupValues?.get(1)?.toBooleanStrictOrNull()
    }

    private fun arrayObjects(json: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var start = -1
        var inString = false
        var escaped = false

        json.forEachIndexed { index, char ->
            when {
                escaped -> escaped = false
                char == '\\' && inString -> escaped = true
                char == '"' -> inString = !inString
                !inString && char == '{' -> {
                    if (depth == 0) start = index
                    depth++
                }
                !inString && char == '}' -> {
                    depth--
                    if (depth == 0 && start >= 0) {
                        objects += json.substring(start, index + 1)
                        start = -1
                    }
                }
            }
        }

        return objects
    }

    private fun String.unescapeJson(): String {
        return replace("\\\"", "\"")
            .replace("\\/", "/")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\r", "\r")
            .replace("\\\\", "\\")
    }

    private fun String.toLocalDateTimeOrNull(): LocalDateTime? {
        return runCatching {
            OffsetDateTime.parse(this)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
        }.getOrNull()
    }
}

interface GitHubHttpClient {
    suspend fun get(url: String): String
}

class UrlConnectionGitHubHttpClient : GitHubHttpClient {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        connection.setRequestProperty("User-Agent", "oop2-coursework")

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()

        if (code !in 200..299) {
            error("GitHub API request failed with HTTP $code")
        }

        body
    }
}
