package ingsis.permission.permission.controller

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.dto.FileType
import ingsis.permission.permission.model.dto.PaginatedUsers
import ingsis.permission.permission.model.dto.PermissionRequest
import ingsis.permission.permission.model.dto.ShareSnippetRequest
import ingsis.permission.permission.model.dto.SnippetDescriptor
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.service.implementation.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/permission")
class PermissionController
    @Autowired
    constructor(
        private val service: PermissionService,
    ) {
        @PostMapping
        fun createPermission(
            @RequestBody input: CreatePermission,
        ): ResponseEntity<String> {
            return try {
                val permission = service.createPermission(input)
                println("Permission created successfully: $permission")
                ResponseEntity.status(HttpStatus.CREATED).body("Permission created successfully")
            } catch (e: InvalidPermissionType) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid permission type")
            } catch (e: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error")
            }
        }

        @GetMapping("/filetypes")
        fun getFileTypes(): ResponseEntity<List<FileType>> {
            // List of file types, in this case it's static, but it could be dynamic
            val fileTypes =
                listOf(
                    FileType(language = "JavaScript", extension = ".js"),
                    FileType(language = "TypeScript", extension = ".ts"),
                    FileType(language = "Python", extension = ".py"),
                    FileType(language = "Java", extension = ".java"),
                    FileType(language = "PrintScript", extension = ".ps"),
                )
            return ResponseEntity.ok(fileTypes)
        }

        @PostMapping("/permissions")
        fun getPermissions(
            @RequestBody request: PermissionRequest,
        ): ResponseEntity<List<PermissionTypeEnum>> {
            val permissions = service.getPermissions(request.userId, request.snippetId)
            return ResponseEntity.ok(permissions)
        }

        @PostMapping("/snippets/share/{snippetId}")
        fun shareSnippet(
            @PathVariable snippetId: String,
            @RequestBody request: ShareSnippetRequest,
            principal: Principal,
            @RequestHeader("Authorization") authorizationHeader: String,
        ): ResponseEntity<SnippetDescriptor> {
            return try {
                val sharedSnippet =
                    service.shareSnippet(
                        snippetId = snippetId,
                        userId = principal.name,
                        authorizationHeader = authorizationHeader,
                        targetUserId = request.targetUserId,
                    )
                ResponseEntity.status(HttpStatus.OK).body(sharedSnippet)
            } catch (e: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
            }
        }

        @GetMapping("/users")
        fun getUserFriends(
            @RequestParam name: String = "",
            @RequestParam page: Int = 0,
            @RequestParam pageSize: Int = 10,
            principal: Principal
        ): ResponseEntity<PaginatedUsers> {
            val paginatedUsers = service.getUserFriends(principal.name, page, pageSize)
            return ResponseEntity.ok(paginatedUsers)
        }
    }
