package com.remenod.oop2_coursework.domain.service

import com.remenod.oop2_coursework.domain.model.GitHubRepositoryLink
import com.remenod.oop2_coursework.domain.model.GitHubRepositorySnapshot

interface GitHubRepositoryService {
    suspend fun sync(repository: GitHubRepositoryLink): GitHubRepositorySyncResult
}

data class GitHubRepositorySyncResult(
    val snapshot: GitHubRepositorySnapshot,
    val message: String
)
