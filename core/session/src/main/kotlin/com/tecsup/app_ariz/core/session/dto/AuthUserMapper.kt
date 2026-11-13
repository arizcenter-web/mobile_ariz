package com.tecsup.app_ariz.core.session.dto

import com.tecsup.app_ariz.core.model.ApprovalStatus
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User

fun AuthUserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    // Un rol desconocido (backend más nuevo que la app) cae al de menor privilegio.
    role = Role.entries.firstOrNull { it.name == role } ?: Role.USER,
    professionalStatus = professionalStatus?.let { status ->
        ApprovalStatus.entries.firstOrNull { it.name == status }
    },
)
