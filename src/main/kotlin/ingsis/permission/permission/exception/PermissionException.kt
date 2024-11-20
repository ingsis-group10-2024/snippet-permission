package ingsis.permission.permission.exception

import java.lang.RuntimeException

class PermissionException(permissionType: String) : RuntimeException("Permission error: $permissionType")
