package service

import ingsis.permission.permission.exception.SnippetNotFoundException
import ingsis.permission.permission.model.dto.ExecutionResponse
import ingsis.permission.permission.model.dto.SnippetDescriptor
import ingsis.permission.permission.model.dto.TestCaseDTO
import ingsis.permission.permission.persistance.entity.TestCaseEntity
import ingsis.permission.permission.persistance.repository.TestCaseRepository
import ingsis.permission.permission.service.implementation.PermissionService
import ingsis.permission.permission.service.implementation.TestCaseService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.Optional

class TestCaseServiceTest {
    @Mock
    private lateinit var permissionService: PermissionService

    @Mock
    private lateinit var testCaseRepository: TestCaseRepository

    @Mock
    private lateinit var restTemplate: RestTemplate

    @InjectMocks
    private lateinit var testCaseService: TestCaseService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `createTestCase should create and save test case when snippet exists`() {
        // Arrange
        val snippetId = "snippet1"
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        val snippet =
            SnippetDescriptor(
                snippetId,
                "snippet",
                "currentUser",
                LocalDateTime.now(),
                "Snippet content",
                "PrintScript",
                "1.0",
                true,
            )

        `when`(permissionService.getSnippet("snippet1", authHeader)).thenReturn(snippet)

        val savedTestCase =
            TestCaseEntity(
                name = "Test Case",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        `when`(testCaseRepository.save(any())).thenReturn(savedTestCase)

        // Act
        val result = testCaseService.createTestCase(testCaseDTO, authHeader)

        // Assert
        assertNotNull(result)
        assertEquals("Test Case", result.name)
        verify(testCaseRepository).save(any())
    }

    @Test
    fun `createTestCase should throw SnippetNotFoundException when snippet not found`() {
        // Arrange
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        `when`(permissionService.getSnippet("snippet1", authHeader)).thenReturn(null)

        // Assert
        assertThrows(SnippetNotFoundException::class.java) {
            testCaseService.createTestCase(testCaseDTO, authHeader)
        }
    }

    @Test
    fun `getTestCase should return test case when exists`() {
        // Arrange
        val testCase =
            TestCaseEntity(
                id = "test1",
                name = "Test Case",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.of(testCase))

        // Act
        val result = testCaseService.getTestCase("test1")

        // Assert
        assertNotNull(result)
        assertEquals("Test Case", result?.name)
    }

    @Test
    fun `deleteTestCase should throw exception when test case not found`() {
        // Arrange
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.empty())

        // Assert
        assertThrows(RuntimeException::class.java) {
            testCaseService.deleteTestCase("test1")
        }
    }

    @Test
    fun `getTestCase should return null when test case does not exist`() {
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.empty())

        val result = testCaseService.getTestCase("test1")

        assertNull(result)
    }

    @Test
    fun `getAllTestCases should return all test cases`() {
        val testCase1 =
            TestCaseEntity(
                id = "test1",
                name = "Test Case 1",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val testCase2 =
            TestCaseEntity(
                id = "test2",
                name = "Test Case 2",
                snippetId = "snippet2",
                input = listOf("print('World')"),
                output = listOf("World"),
            )
        `when`(testCaseRepository.findAll()).thenReturn(listOf(testCase1, testCase2))

        val result = testCaseService.getAllTestCases()

        assertEquals(2, result.size)
        assertTrue(result.contains(testCase1))
        assertTrue(result.contains(testCase2))
    }

    @Test
    fun `deleteTestCase should delete test case when exists`() {
        val testCase =
            TestCaseEntity(
                id = "test1",
                name = "Test Case",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.of(testCase))

        testCaseService.deleteTestCase("test1")

        verify(testCaseRepository).delete(testCase)
    }

    @Test
    fun `executeTestCase should return success when output matches`() {
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        val snippet =
            SnippetDescriptor(
                "snippet1",
                "snippet",
                "currentUser",
                LocalDateTime.now(),
                "Snippet content",
                "PrintScript",
                "1.0",
                true,
            )
        `when`(permissionService.getSnippet("snippet1", authHeader)).thenReturn(snippet)

        val executionResponse = ExecutionResponse(output = listOf("Hello"), emptyList())
        `when`(restTemplate.postForEntity(anyString(), any(), eq(ExecutionResponse::class.java)))
            .thenReturn(ResponseEntity(executionResponse, HttpStatus.OK))

        val result = testCaseService.executeTestCase(testCaseDTO, authHeader)

        assertTrue(result.success)
        assertEquals("Hello", result.actualOutput[0])
    }

    @Test
    fun `executeTestCase should return failure when output does not match`() {
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        val snippet =
            SnippetDescriptor(
                "snippet1",
                "snippet",
                "currentUser",
                LocalDateTime.now(),
                "Snippet content",
                "PrintScript",
                "1.0",
                true,
            )
        `when`(permissionService.getSnippet("snippet1", authHeader)).thenReturn(snippet)

        val executionResponse = ExecutionResponse(output = listOf("Hi"), listOf("error"))
        `when`(restTemplate.postForEntity(anyString(), any(), eq(ExecutionResponse::class.java)))
            .thenReturn(ResponseEntity(executionResponse, HttpStatus.OK))

        val result = testCaseService.executeTestCase(testCaseDTO, authHeader)

        assertFalse(result.success)
        assertEquals("Hi", result.actualOutput[0])
    }

    @Test
    fun `executeTestCase should throw exception when snippet not found`() {
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        `when`(permissionService.getSnippet("snippet1", authHeader)).thenReturn(null)

        assertThrows(SnippetNotFoundException::class.java) {
            testCaseService.executeTestCase(testCaseDTO, authHeader)
        }
    }

    @Test
    fun `executeTestCase should throw exception when response is not OK`() {
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        val snippet =
            SnippetDescriptor(
                "snippet1",
                "snippet",
                "currentUser",
                LocalDateTime.now(),
                "Snippet content",
                "PrintScript",
                "1.0",
                true,
            )
        `when`(permissionService.getSnippet("snippet1", authHeader)).thenReturn(snippet)

        `when`(restTemplate.postForEntity(anyString(), any(), eq(ExecutionResponse::class.java)))
            .thenReturn(ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR))

        assertThrows(RuntimeException::class.java) {
            testCaseService.executeTestCase(testCaseDTO, authHeader)
        }
    }
}
