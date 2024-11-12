package ingsis.permission.permission.model.dto

import org.jetbrains.annotations.NotNull

data class SnippetRequest(
    @NotNull
    var name: String,
    @NotNull
    val content: String,
    @NotNull
    val language: String,
    val languageVersion: String,
)
