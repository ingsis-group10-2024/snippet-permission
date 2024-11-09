package ingsis.permission.permission.model.dto

import org.jetbrains.annotations.NotNull

data class TestCaseDTO(
    @NotNull
    val id: String,
    @NotNull
    val name: String,
    @NotNull
    val input: List<String>,
    @NotNull
    val output: List<String>,
)
