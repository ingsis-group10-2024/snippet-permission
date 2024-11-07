package ingsis.permission.permission.controller

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.*
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.service.implementation.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
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
                service.createPermission(input)
                ResponseEntity.status(HttpStatus.CREATED).body("Permission created successfully")
            } catch (e: InvalidPermissionType) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid permission type")
            } catch (e: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error")
            }
        }

        @PostMapping("/permissions")
        fun getPermissions(
            @RequestBody request: PermissionRequest,
        ): ResponseEntity<List<PermissionTypeEnum>> {
            val permissions = service.getPermissions(request.userId, request.snippetId)
            return ResponseEntity.ok(permissions)
        }

        @PreAuthorize("hasAuthority('SCOPE_read:snippet')")
        @GetMapping("/snippets")
        fun listUserSnippets(
            principal: Principal,
            @RequestParam page: Int,
            @RequestParam pageSize: Int,
            @RequestHeader("Authorization") authorizationHeader: String,
        ): ResponseEntity<PaginatedSnippetResponse> {
            val snippets = service.listUserSnippets(principal.name, page, pageSize, authorizationHeader)
            return ResponseEntity.ok(snippets)
        }

        @PostMapping("/snippets/share/{snippetId}")
        fun shareSnippet(
            @PathVariable snippetId: String,
            @RequestBody request: ShareSnippetRequest,
            principal: Principal,
            @RequestHeader("Authorization") authorizationHeader: String,
            ): ResponseEntity<SnippetDescriptor> {
            return try {
                val sharedSnippet = service.shareSnippet(snippetId, principal.name,authorizationHeader, request.userId)
                ResponseEntity.status(HttpStatus.OK).body(sharedSnippet)
            } catch (e: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
            }
        }
    }
