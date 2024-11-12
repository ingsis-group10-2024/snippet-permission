package ingsis.permission.permission.service.implementation

import ingsis.permission.permission.exception.SnippetNotFoundException
import ingsis.permission.permission.model.dto.ExecutionResponse
import ingsis.permission.permission.model.dto.SnippetRequest
import ingsis.permission.permission.model.dto.TestCaseDTO
import ingsis.permission.permission.model.dto.TestCaseResult
import ingsis.permission.permission.persistance.entity.TestCaseEntity
import ingsis.permission.permission.persistance.repository.TestCaseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class TestCaseService
    @Autowired
    constructor(
        private val permissionService: PermissionService,
        private val testCaseRepository: TestCaseRepository,
        private val restTemplate: RestTemplate,
    ) {
        fun createTestCase(
            testCaseDTO: TestCaseDTO,
            authorizationHeader: String,
        ): TestCaseEntity {
            // Check if snippet exists
            val snippet =
                permissionService.getSnippet(testCaseDTO.id, authorizationHeader)
                    ?: throw SnippetNotFoundException("Snippet with ${testCaseDTO.id} not found")

            val testCaseEntity =
                TestCaseEntity(
                    name = testCaseDTO.name,
                    snippetId = testCaseDTO.id,
                    input = testCaseDTO.input,
                    output = testCaseDTO.output,
                )
            println("Test case created:$testCaseEntity")
            return testCaseRepository.save(testCaseEntity)
        }

        fun getTestCase(id: String): TestCaseEntity? {
            return testCaseRepository.findById(id).orElse(null)
        }

        fun getAllTestCases(): List<TestCaseEntity> {
            return testCaseRepository.findAll()
        }

        fun deleteTestCase(id: String) {
            val testCase = getTestCase(id) ?: throw RuntimeException("Test case not found")
            testCaseRepository.delete(testCase)
        }

        fun executeTestCase(
            testCaseDTO: TestCaseDTO,
            authorizationHeader: String,
        ): TestCaseResult {
            val snippet =
                permissionService.getSnippet(testCaseDTO.id, authorizationHeader)
                    ?: throw SnippetNotFoundException("Snippet with ${testCaseDTO.id} not found")

            val url = "http://snippet-runner:8080/runner/execute"

            val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
            headers.add("Authorization", authorizationHeader)
            headers.add("Content-Type", "application/json")

            // Create the SnippetRequest request for the runner microservice
            val snippetRequest =
                SnippetRequest(
                    name = testCaseDTO.name,
                    content = testCaseDTO.input.joinToString("\n"),
                    language = snippet.language,
                    languageVersion = snippet.languageVersion,
                )
            println("Executing test case: $snippetRequest")

            val requestEntity = HttpEntity(snippetRequest, headers)
            val response =
                restTemplate.postForEntity(
                    url,
                    requestEntity,
                    ExecutionResponse::class.java,
                )

            // Check if the response is valid
            if (response.statusCode != HttpStatus.OK || response.body == null) {
                throw RuntimeException("Error executing snippet")
            }

            val actualOutput = response.body!!.output
            println("Actual output: $actualOutput")

            val isSuccess = actualOutput == testCaseDTO.output
            return TestCaseResult(
                testCaseId = testCaseDTO.id,
                success = isSuccess,
                actualOutput = actualOutput,
                expectedOutput = testCaseDTO.output,
                message = if (isSuccess) "Test passed" else "Test failed",
            )
        }
    }
