package ingsis.permission.permission.controller

import ingsis.permission.permission.service.implementation.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController

@RestController
class PermissionController
    @Autowired
    constructor(
        private val service: PermissionService,
    )
