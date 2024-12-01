package ingsis.permission.permission.model.dto

import sca.StaticCodeAnalyzerError
import java.time.LocalDateTime

data class SnippetDescriptor(
    val id: String,
    val name: String,
    val authorId: String,
    val createdAt: LocalDateTime,
    val content: String,
    val language: String,
    val languageVersion: String,
    val isValid: Boolean,
    val validationErrors: List<StaticCodeAnalyzerError>? = null,
)
