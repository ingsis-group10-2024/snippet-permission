package ingsis.permission.permission.service

import ingsis.permission.permission.persistance.repository.PermissionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PermissionService
    @Autowired
    constructor(
        private val repository: PermissionRepository,
    )
