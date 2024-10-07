package ingsis.permission.permission.controller

import ingsis.permission.permission.exception.InvalidPermissionType
import ingsis.permission.permission.model.dto.CreatePermission
import ingsis.permission.permission.model.dto.PermissionRequest
import ingsis.permission.permission.model.enums.PermissionTypeEnum
import ingsis.permission.permission.persistance.entity.Permission
import ingsis.permission.permission.service.implementation.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        ): ResponseEntity<Permission> {
            return try {
                val createdPermission = service.createPermission(input)
                ResponseEntity.status(HttpStatus.CREATED).body(createdPermission)
            } catch (e: InvalidPermissionType) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
            } catch (e: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
            }
        }

        @PostMapping("/permissions")
        fun getPermissions(
            @RequestBody request: PermissionRequest,
        ): ResponseEntity<List<PermissionTypeEnum>> {
            val permissions = service.getPermissions(request.userId, request.snippetId)
            return ResponseEntity.ok(permissions)
        }

        // @GetMapping("/permissions")
        // fun getPermissions(
        //    @RequestBody request: PermissionRequest // Usar el DTO aquí
        // ): ResponseEntity<List<PermissionTypeEnum>> {
        //    return try {
        //        val permissions = service.getPermissions(request.userId, request.snippetId)
        //        ResponseEntity.ok(permissions)
        //    } catch (e: Exception) {
        //        ResponseEntity.notFound().build()
        //    }
        // }
        // }
    }
