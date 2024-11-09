package ingsis.permission.permission.service.implementation

import ingsis.permission.permission.exception.SnippetNotFoundException
import ingsis.permission.permission.model.dto.TestCaseDTO
import ingsis.permission.permission.persistance.entity.TestCaseEntity
import ingsis.permission.permission.persistance.repository.TestCaseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TestCaseService
    @Autowired
    constructor(
        private val permissionService: PermissionService,
        private val testCaseRepository: TestCaseRepository,
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
    }
