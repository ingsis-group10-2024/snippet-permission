package ingsis.permission.permission.controller

import ingsis.permission.permission.model.dto.TestCaseDTO
import ingsis.permission.permission.service.implementation.TestCaseService
import ingsis.permission.permission.utils.toDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permission/testcases")
class TestCaseController(
    private val testCaseService: TestCaseService,
) {
    @PostMapping
    fun createTestCase(
        @RequestBody testCaseDTO: TestCaseDTO,
    ): ResponseEntity<TestCaseDTO> {
        val createdTestCase = testCaseService.createTestCase(testCaseDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTestCase.toDTO())
    }

    @GetMapping("/{id}")
    fun getTestCase(
        @PathVariable id: String,
    ): ResponseEntity<TestCaseDTO> {
        val testCase =
            testCaseService.getTestCase(id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        return ResponseEntity.ok(testCase.toDTO())
    }

    @GetMapping
    fun getAllTestCases(): ResponseEntity<List<TestCaseDTO>> {
        val testCases = testCaseService.getAllTestCases()
        return ResponseEntity.ok(testCases.map { it.toDTO() })
    }

    @DeleteMapping("/{id}")
    fun deleteTestCase(
        @PathVariable id: String,
    ): ResponseEntity<String> {
        return try {
            testCaseService.deleteTestCase(id)
            ResponseEntity.status(HttpStatus.NO_CONTENT).body("Test case deleted successfully")
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test case not found")
        }
    }
}
