package ingsis.permission.permission.exception

import java.lang.RuntimeException

class InvalidPermissionType(permissionType: String) : RuntimeException("Invalid type: $permissionType")
