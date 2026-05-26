package com.remenod.oop2_coursework.domain.model

import java.net.URI

data class GitHubRepositoryInfo(
    val owner: String,
    val repository: String,
    val branch: String? = null,
    val path: String? = null,
    val issueNumber: Int? = null,
    val pullRequestNumber: Int? = null
) {
    val fullName: String get() = "$owner/$repository"
    val canonicalUrl: String get() = "https://github.com/$owner/$repository"
    val cloneUrl: String get() = "$canonicalUrl.git"
    val issuesUrl: String get() = "$canonicalUrl/issues"
    val pullRequestsUrl: String get() = "$canonicalUrl/pulls"
    val commitsUrl: String get() = branch?.let { "$canonicalUrl/commits/$it" } ?: "$canonicalUrl/commits"
    val branchUrl: String? get() = branch?.let { "$canonicalUrl/tree/$it" }
}

object GitHubUrlParser {
    fun parse(raw: String, branchOverride: String? = null): GitHubRepositoryInfo? {
        val value = raw.trim()
        if (value.isBlank()) return null

        val normalized = when {
            value.startsWith("git@github.com:") -> {
                val path = value.removePrefix("git@github.com:").removeSuffix(".git")
                "https://github.com/$path"
            }
            value.startsWith("github.com/") -> "https://$value"
            else -> value
        }

        val uri = runCatching { URI(normalized) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (host != "github.com" && host != "www.github.com") return null

        val segments = uri.path
            .trim('/')
            .split('/')
            .filter { it.isNotBlank() }

        if (segments.size < 2) return null

        val owner = segments[0]
        val repository = segments[1].removeSuffix(".git")
        if (!isSafeSlug(owner) || !isSafeSlug(repository)) return null

        var branch: String? = branchOverride?.trim()?.takeIf { it.isNotBlank() }
        var path: String? = null
        var issueNumber: Int? = null
        var pullRequestNumber: Int? = null

        when {
            segments.size >= 4 && segments[2] == "tree" -> {
                branch = branch ?: segments[3]
                if (segments.size > 4) path = segments.drop(4).joinToString("/")
            }
            segments.size >= 4 && segments[2] == "blob" -> {
                branch = branch ?: segments[3]
                if (segments.size > 4) path = segments.drop(4).joinToString("/")
            }
            segments.size >= 4 && segments[2] == "issues" -> {
                issueNumber = segments[3].toIntOrNull()
            }
            segments.size >= 4 && segments[2] == "pull" -> {
                pullRequestNumber = segments[3].toIntOrNull()
            }
        }

        return GitHubRepositoryInfo(
            owner = owner,
            repository = repository,
            branch = branch,
            path = path,
            issueNumber = issueNumber,
            pullRequestNumber = pullRequestNumber
        )
    }

    fun isGitHubRepositoryUrl(raw: String): Boolean = parse(raw) != null

    private fun isSafeSlug(value: String): Boolean {
        return value.matches(Regex("[A-Za-z0-9_.-]+"))
    }
}
