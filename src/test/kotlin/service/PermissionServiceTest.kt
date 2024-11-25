package service

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.dto.SnippetDescriptor
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.persistance.repository.PermissionRepository
import ingsis.permission.permission.service.implementation.PermissionService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

class PermissionServiceTest {
    @Mock
    private lateinit var repository: PermissionRepository

    @Mock
    private lateinit var restTemplate: RestTemplate

    @InjectMocks
    private lateinit var permissionService: PermissionService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `createPermission should create new permission when not exists`() {
        // Arrange
        val input =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "READ",
            )
        `when`(repository.findByUserIdAndSnippetId("user1", "snippet1")).thenReturn(null)

        val savedPermission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                permissions = listOf(PermissionTypeEnum.READ),
            )
        `when`(repository.save(any())).thenReturn(savedPermission)

        // Act
        val result = permissionService.createPermission(input)

        // Assert
        assertNotNull(result)
        assertEquals("user1", result.userId)
        verify(repository).save(any())
    }

    @Test
    fun `createPermission should update existing permission`() {
        // Arrange
        val existingPermission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                permissions = listOf(PermissionTypeEnum.READ),
            )
        val input =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "WRITE",
            )
        `when`(repository.findByUserIdAndSnippetId("user1", "snippet1")).thenReturn(existingPermission)

        val updatedPermission =
            existingPermission.copy(
                permissions = listOf(PermissionTypeEnum.READ, PermissionTypeEnum.WRITE),
            )
        `when`(repository.save(any())).thenReturn(updatedPermission)

        // Act
        val result = permissionService.createPermission(input)

        // Assert
        assertEquals(2, result.permissions.size)
        assertTrue(result.permissions.contains(PermissionTypeEnum.WRITE))
        verify(repository).save(any())
    }

    @Test
    fun `getPermissions should return user permissions`() {
        // Arrange
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                permissions = listOf(PermissionTypeEnum.READ, PermissionTypeEnum.WRITE),
            )
        `when`(repository.findByUserIdAndSnippetId("user1", "snippet1")).thenReturn(permission)

        // Act
        val result = permissionService.getPermissions("user1", "snippet1")

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.contains(PermissionTypeEnum.READ))
    }

    @Test
    fun `getSnippet should retrieve snippet from snippet manager`() {
        // Arrange
        val snippetId = "snippet1"
        val mockResponse = mock(ResponseEntity::class.java) as ResponseEntity<SnippetDescriptor>
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

        `when`(mockResponse.body).thenReturn(snippet)

        `when`(
            restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(SnippetDescriptor::class.java),
            ),
        ).thenReturn(mockResponse as ResponseEntity<SnippetDescriptor>)

        // Act
        val result = permissionService.getSnippet("snippet1", "auth-header")

        // Assert
        assertNotNull(result)
        assertEquals("snippet1", result?.id)
    }

    @Test
    fun createPermission_shouldUpdateExistingPermission() {
        // Arrange
        val existingPermission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                permissions = listOf(PermissionTypeEnum.READ),
            )
        val input =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "WRITE",
            )
        `when`(repository.findByUserIdAndSnippetId("user1", "snippet1")).thenReturn(existingPermission)

        val updatedPermission =
            existingPermission.copy(
                permissions = listOf(PermissionTypeEnum.READ, PermissionTypeEnum.WRITE),
            )
        `when`(repository.save(any(Permission::class.java))).thenReturn(updatedPermission)

        // Act
        val result = permissionService.createPermission(input)

        // Assert
        assertEquals(2, result.permissions.size)
        assertTrue(result.permissions.contains(PermissionTypeEnum.WRITE))
        verify(repository).save(any(Permission::class.java))
    }

    @Test
    fun createPermission_shouldThrowExceptionForInvalidPermissionType() {
        val input =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "INVALID",
            )
        assertThrows(InvalidPermissionType::class.java) {
            permissionService.createPermission(input)
        }
    }

    @Test
    fun createPermission_shouldNotDuplicateExistingPermissionType() {
        val existingPermission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                permissions = listOf(PermissionTypeEnum.READ),
            )
        val input =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "READ",
            )
        `when`(repository.findByUserIdAndSnippetId("user1", "snippet1")).thenReturn(existingPermission)
        `when`(repository.save(any(Permission::class.java))).thenReturn(existingPermission)

        val result = permissionService.createPermission(input)

        assertEquals(1, result.permissions.size)
        assertTrue(result.permissions.contains(PermissionTypeEnum.READ))
        verify(repository).save(any(Permission::class.java))
    }

    @Test
    fun shareSnippet_shouldThrowExceptionWhenUserDoesNotHaveOwnerPermission() {
        val snippetId = "snippet1"
        val userId = "user1"
        val targetUserId = "user2"
        val authorizationHeader = "auth-header"
        val snippet =
            SnippetDescriptor(snippetId, "snippet", "currentUser", LocalDateTime.now(), "Snippet content", "PrintScript", "1.0", true)

        `when`(
            repository.findByUserIdAndSnippetId(userId, snippetId),
        ).thenReturn(Permission(snippetId, userId, snippetId, LocalDateTime.now(), LocalDateTime.now(), listOf(PermissionTypeEnum.READ)))
        `when`(
            restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(SnippetDescriptor::class.java)),
        ).thenReturn(ResponseEntity.ok(snippet))

        assertThrows(InvalidPermissionType::class.java) {
            permissionService.shareSnippet(snippetId, userId, authorizationHeader, targetUserId)
        }
    }
}
