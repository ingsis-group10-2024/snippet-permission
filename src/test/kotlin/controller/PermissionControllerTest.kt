package controller
import ingsis.permission.permission.controller.PermissionController
import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.dto.PaginatedUsers
import ingsis.permission.permission.model.dto.PermissionRequest
import ingsis.permission.permission.model.dto.ShareSnippetRequest
import ingsis.permission.permission.model.dto.SnippetDescriptor
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.service.implementation.PermissionService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import java.security.Principal
import java.time.LocalDateTime

class PermissionControllerTest {
    @Mock
    private lateinit var permissionService: PermissionService

    @Mock
    private lateinit var principal: Principal

    @InjectMocks
    private lateinit var permissionController: PermissionController

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `createPermission should return INTERNAL_SERVER_ERROR when an unexpected error occurs`() {
        val createPermission =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "READ",
            )
        `when`(permissionService.createPermission(createPermission)).thenThrow(RuntimeException("Unexpected error"))

        val response = permissionController.createPermission(createPermission)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Internal server error", response.body)
    }

    @Test
    fun `shareSnippet should return INTERNAL_SERVER_ERROR when an unexpected error occurs`() {
        val snippetId = "snippet1"
        val shareRequest = ShareSnippetRequest("targetUser")
        val authHeader = "Bearer token123"
        `when`(principal.name).thenReturn("currentUser")
        `when`(
            permissionService.shareSnippet(
                snippetId = snippetId,
                userId = "currentUser",
                authorizationHeader = authHeader,
                targetUserId = "targetUser",
            ),
        ).thenThrow(RuntimeException("Unexpected error"))

        val response =
            permissionController.shareSnippet(
                snippetId,
                shareRequest,
                principal,
                authHeader,
            )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `getFileTypes should return empty list when no file types are available`() {
        val response = permissionController.getFileTypes()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertFalse(response.body?.isEmpty() ?: false)
    }

    @Test
    fun `getPermissions should return empty list when no permissions are found`() {
        val request =
            PermissionRequest(
                userId = "user1",
                snippetId = "snippet1",
            )
        `when`(permissionService.getPermissions("user1", "snippet1")).thenReturn(emptyList())

        val response = permissionController.getPermissions(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.isEmpty() ?: false)
    }

    @Test
    fun `shareSnippet should return INTERNAL_SERVER_ERROR when target user is invalid`() {
        val snippetId = "snippet1"
        val shareRequest = ShareSnippetRequest("invalidUser")
        val authHeader = "Bearer token123"
        `when`(principal.name).thenReturn("currentUser")
        `when`(
            permissionService.shareSnippet(
                snippetId = snippetId,
                userId = "currentUser",
                authorizationHeader = authHeader,
                targetUserId = "invalidUser",
            ),
        ).thenThrow(InvalidPermissionType("Invalid target user"))

        val response =
            permissionController.shareSnippet(
                snippetId,
                shareRequest,
                principal,
                authHeader,
            )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `createPermission should return BAD_REQUEST when permission type is invalid`() {
        val createPermission =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "INVALID",
            )
        `when`(permissionService.createPermission(createPermission)).thenThrow(InvalidPermissionType("Invalid permission type"))

        val response = permissionController.createPermission(createPermission)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid permission type", response.body)
    }

    @Test
    fun `getFileTypes should return list of file types`() {
        val response = permissionController.getFileTypes()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.isNotEmpty())
    }

    @Test
    fun `getPermissions should return list of permissions when permissions are found`() {
        val request =
            PermissionRequest(
                userId = "user1",
                snippetId = "snippet1",
            )
        val permissions = listOf(PermissionTypeEnum.READ, PermissionTypeEnum.WRITE)
        `when`(permissionService.getPermissions("user1", "snippet1")).thenReturn(permissions)

        val response = permissionController.getPermissions(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(permissions, response.body)
    }

    @Test
    fun `shareSnippet should return OK when snippet is shared successfully`() {
        val snippetId = "snippet1"
        val shareRequest = ShareSnippetRequest("targetUser")
        val authHeader = "Bearer token123"
        val snippetDescriptor =
            SnippetDescriptor(snippetId, "snippet", "currentUser", LocalDateTime.now(), "Snippet content", "PrintScript", "1.0", true)
        `when`(principal.name).thenReturn("currentUser")
        `when`(
            permissionService.shareSnippet(
                snippetId = snippetId,
                userId = "currentUser",
                authorizationHeader = authHeader,
                targetUserId = "targetUser",
            ),
        ).thenReturn(snippetDescriptor)

        val response =
            permissionController.shareSnippet(
                snippetId,
                shareRequest,
                principal,
                authHeader,
            )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(snippetDescriptor, response.body)
    }

    @Test
    fun `createPermission should return CREATED when permission is created successfully`() {
        val createPermission =
            CreatePermission(
                userId = "user1",
                snippetId = "snippet1",
                permissionType = "READ",
            )

        val response = permissionController.createPermission(createPermission)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Permission created successfully", response.body)
    }

    @Test
    fun `getUserFriends should return paginated users`() {
        val paginatedUsers = PaginatedUsers(listOf("user1", "user2"), 2)
        `when`(principal.name).thenReturn("currentUser")
        `when`(permissionService.getUserFriends("currentUser", 0, 10)).thenReturn(paginatedUsers)

        val response = permissionController.getUserFriends("", 0, 10, principal)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(paginatedUsers, response.body)
    }

    @Test
    fun `getUserFriends should return empty list when no friends are found`() {
        val paginatedUsers = PaginatedUsers(emptyList(), 0)
        `when`(principal.name).thenReturn("currentUser")
        `when`(permissionService.getUserFriends("currentUser", 0, 10)).thenReturn(paginatedUsers)

        val response = permissionController.getUserFriends("", 0, 10, principal)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.users?.isEmpty() ?: false)
    }
}
