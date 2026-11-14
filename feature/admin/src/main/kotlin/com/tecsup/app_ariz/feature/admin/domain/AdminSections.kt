package com.tecsup.app_ariz.feature.admin.domain

/**
 * Catálogo de secciones del panel de administración: qué endpoint lista, cómo se lee cada registro y qué acciones
 * ofrece. Agregar una sección es agregar un elemento a [AdminSections.All] (no hay pantallas nuevas).
 */
object AdminSections {
    private val Done = setOf("ACTIVE")
    private val Off = setOf("INACTIVE")

    private fun toggle(path: String, flag: String, activeLabel: String = "Activar", offLabel: String = "Desactivar") = listOf(
        AdminAction(offLabel, HttpVerb.PATCH, path, ActionBody(flags = mapOf(flag to false)), appliesTo = Done),
        AdminAction(activeLabel, HttpVerb.PATCH, path, ActionBody(flags = mapOf(flag to true)), appliesTo = Off),
    )

    private fun delete(path: String) = AdminAction("Eliminar", HttpVerb.DELETE, path, destructive = true)

    private fun review(base: String, label: String) = listOf(
        AdminAction("Publicar", HttpVerb.PATCH, "$base/{id}/publish", appliesTo = setOf("PENDING_REVIEW", "REJECTED")),
        AdminAction("Rechazar", HttpVerb.PATCH, "$base/{id}/reject", appliesTo = setOf("PENDING_REVIEW", "PUBLISHED")),
    )

    private fun reviewFilters() = listOf(
        AdminFilter("En revisión", mapOf("status" to "PENDING_REVIEW")),
        AdminFilter("Publicadas", mapOf("status" to "PUBLISHED")),
        AdminFilter("Rechazadas", mapOf("status" to "REJECTED")),
    )

    private fun referralStatusActions() = listOf(
        AdminAction("Marcar visita", HttpVerb.PATCH, "admin/referrals/{id}/status", ActionBody(mapOf("status" to "VISIT")), appliesTo = setOf("CONTACTED")),
        AdminAction("Operación concretada", HttpVerb.PATCH, "admin/referrals/{id}/status", ActionBody(mapOf("status" to "OPERATION_COMPLETED")), appliesTo = setOf("VISIT")),
    )

    private fun partner(key: String, title: String, description: String, base: String) = AdminSection(
        key = key, title = title, description = description, group = GROUP_ACCOUNTS, path = base,
        titlePaths = listOf("businessName", "user.firstName+user.lastName"),
        subtitlePaths = listOf("user.email"),
        statusPath = null,
        details = listOf(AdminDetailSpec("Teléfono", listOf("user.phone")), AdminDetailSpec("Ciudad", listOf("city"))),
        children = listOf(
            AdminChildSpec("Propiedades", "properties", listOf("title"), actions = review("$base/properties", "propiedad")),
            AdminChildSpec("Proyectos", "projects", listOf("name"), actions = review("$base/projects", "proyecto")),
        ),
        filters = reviewFilters(),
        actions = partnerAccountActions(key == "developers"),
    )

    /** Acciones sobre la cuenta (como en "Cuentas" del web): marca de desarrolladora oficial, aviso y baja. */
    private fun partnerAccountActions(official: Boolean) = buildList {
        if (official) {
            add(AdminAction("Marcar como oficial", HttpVerb.PATCH, "admin/professionals/{id}/official-developer", ActionBody(flags = mapOf("isOfficialDeveloper" to true))))
            add(AdminAction("Quitar marca oficial", HttpVerb.PATCH, "admin/professionals/{id}/official-developer", ActionBody(flags = mapOf("isOfficialDeveloper" to false))))
        }
        add(
            AdminAction(
                "Enviar aviso", HttpVerb.POST, "admin/professionals/{id}/notify",
                ActionBody(strings = mapOf("subject" to "Notificación administrativa de ARIZ")),
                reasonKey = "message", inputLabel = "Mensaje para el profesional (opcional)",
            ),
        )
        add(AdminAction("Eliminar cuenta", HttpVerb.DELETE, "admin/professionals/{id}", destructive = true))
    }

    private val planForm = AdminFormSpec(
        title = "Plan",
        fields = listOf(
            AdminField("name", "Nombre", required = true),
            AdminField("price", "Precio", FieldKind.NUMBER, required = true),
            AdminField("currency", "Moneda", FieldKind.CHOICE, options = listOf("PEN", "USD"), default = "PEN"),
            AdminField("durationDays", "Duración (días)", FieldKind.INT, required = true),
            AdminField("propertyLimit", "Límite de propiedades", FieldKind.INT, required = true),
            AdminField("projectLimit", "Límite de proyectos", FieldKind.INT),
            AdminField("features", "Beneficios (uno por línea)", FieldKind.LIST),
            AdminField("isFree", "Plan gratuito", FieldKind.BOOL),
            AdminField("highlight", "Destacado", FieldKind.BOOL),
            AdminField("isActive", "Activo", FieldKind.BOOL, default = "true"),
        ),
    )

    private val promotionForm = AdminFormSpec(
        title = "Promoción",
        fields = listOf(
            AdminField("title", "Título", required = true),
            AdminField("imageUrl", "Imagen", FieldKind.IMAGE, required = true),
            AdminField("startsAt", "Inicio (AAAA-MM-DD)", FieldKind.DATE, required = true),
            AdminField("endsAt", "Fin (AAAA-MM-DD)", FieldKind.DATE, required = true),
        ),
    )

    private val incentiveForm = AdminFormSpec(
        title = "Incentivo",
        fields = listOf(
            AdminField("name", "Nombre", required = true),
            AdminField("description", "Descripción", FieldKind.MULTILINE, required = true),
            AdminField("promotion", "Promoción asociada"),
            AdminField("imageUrl", "Imagen", FieldKind.IMAGE),
            AdminField("pointsRequired", "Puntos requeridos", FieldKind.INT, required = true),
            AdminField("stock", "Stock", FieldKind.INT, required = true),
        ),
    )

    private val campaignForm = AdminFormSpec(
        title = "Campaña",
        fields = listOf(
            AdminField("name", "Nombre", required = true),
            AdminField("description", "Descripción", FieldKind.MULTILINE),
            AdminField("projectIds", "Proyectos de la campaña", FieldKind.MULTI, remote = RemoteOptions("admin/campaigns/ariz-projects")),
            AdminField("startDate", "Inicio (AAAA-MM-DD)", FieldKind.DATE, required = true),
            AdminField("endDate", "Fin (AAAA-MM-DD)", FieldKind.DATE, required = true),
            AdminField("points.CONTACTED", "Puntos por contacto", FieldKind.INT, required = true, createOnly = true, default = "0"),
            AdminField("points.VISIT", "Puntos por visita", FieldKind.INT, required = true, createOnly = true, default = "0"),
            AdminField("points.OPERATION_COMPLETED", "Puntos por operación concretada", FieldKind.INT, required = true, createOnly = true, default = "0"),
        ),
        emptyListsOnCreate = listOf("projectIds"),
    )

    private val investmentTypeForm = AdminFormSpec(
        title = "Tipo de inversión",
        fields = listOf(
            AdminField("name", "Nombre", required = true),
            AdminField("description", "Descripción"),
            AdminField("sortOrder", "Orden", FieldKind.INT),
            AdminField("isActive", "Activo", FieldKind.BOOL, default = "true"),
        ),
    )

    private val projectPointsForm = AdminFormSpec(
        title = "Puntos del proyecto",
        fields = listOf(
            AdminField("points.CONTACTED", "Puntos por contacto", FieldKind.INT, required = true),
            AdminField("points.VISIT", "Puntos por visita", FieldKind.INT, required = true),
            AdminField("points.OPERATION_COMPLETED", "Puntos por operación concretada", FieldKind.INT, required = true),
        ),
    )

    private val statusPointsForm = AdminFormSpec(
        title = "Puntos por estado",
        fields = listOf(
            AdminField("CONTACTED", "Puntos por contacto", FieldKind.INT, required = true),
            AdminField("VISIT", "Puntos por visita", FieldKind.INT, required = true),
            AdminField("OPERATION_COMPLETED", "Puntos por operación concretada", FieldKind.INT, required = true),
        ),
    )

    private val recordPointsForm = AdminFormSpec(
        title = "Puntos del registro",
        fields = listOf(AdminField("points", "Puntos", FieldKind.INT, required = true)),
    )

    private val referrerForm = AdminFormSpec(
        title = "Referidor",
        fields = listOf(
            AdminField("name", "Nombre", required = true),
            AdminField("email", "Correo", required = true),
            AdminField("phone", "Teléfono", required = true),
            AdminField("bankAccount", "Cuenta bancaria"),
            AdminField("yape", "Número de Yape", FieldKind.TEXT),
            AdminField("plin", "Número de Plin", FieldKind.TEXT),
        ),
    )

    private val investmentForm = AdminFormSpec(
        title = "Oportunidad de inversión",
        fields = listOf(
            AdminField("name", "Nombre del proyecto", required = true),
            AdminField("location", "Ubicación visible", required = true),
            AdminField("summary", "Resumen para las tarjetas", required = true),
            AdminField("investmentTypeId", "Tipo de inversión", FieldKind.CHOICE, remote = RemoteOptions("admin/investment-opportunities/types"), noneLabel = "Sin tipo asignado"),
            AdminField("investmentKind", "Tipo visible", required = true, default = "Inversión inmobiliaria"),
            AdminField("assetType", "Tipo de activo (terreno, departamento, proyecto, local)", required = true, default = "Proyecto"),
            AdminField("areaLabel", "Área / metraje (ej. Desde 120 m²)", required = true),
            AdminField("investmentGoal", "Objetivo de inversión", FieldKind.CHOICE, options = listOf("plusvalia", "construccion", "renta"), optionLabels = mapOf("plusvalia" to "Plusvalía", "construccion" to "Construcción", "renta" to "Renta futura"), default = "plusvalia"),
            AdminField("initialInvestment", "Inversión inicial desde (S/)", FieldKind.NUMBER, required = true),
            AdminField("roiPercent", "ROI estimado (%)", FieldKind.NUMBER, required = true, default = "0"),
            AdminField("estimatedReturnLabel", "Rentabilidad estimada (ej. 12% anual)"),
            AdminField("estimatedTermMonths", "Plazo estimado (meses)", FieldKind.INT),
            AdminField("status", "Estado comercial", FieldKind.CHOICE, options = listOf("AVAILABLE", "COMING_SOON", "CLOSED"), optionLabels = mapOf("AVAILABLE" to "Disponible", "COMING_SOON" to "Próximamente", "CLOSED" to "Cerrada"), default = "AVAILABLE"),
            AdminField("riskLevel", "Nivel de riesgo", FieldKind.CHOICE, options = listOf("LOW", "MEDIUM", "HIGH"), optionLabels = mapOf("LOW" to "Bajo", "MEDIUM" to "Medio", "HIGH" to "Alto"), noneLabel = "No mostrar"),
            AdminField("featured", "Destacada", FieldKind.BOOL),
            AdminField("sortOrder", "Orden", FieldKind.INT, default = "0"),
            AdminField("ctaLabel", "Texto del botón de contacto", required = true, default = "Solicitar información"),
            AdminField("department", "Región / departamento", required = true),
            AdminField("province", "Provincia", required = true),
            AdminField("district", "Distrito", required = true),
            AdminField("latitude", "Latitud", FieldKind.NUMBER, required = true, default = "-3.5669"),
            AdminField("longitude", "Longitud", FieldKind.NUMBER, required = true, default = "-80.4515"),
            AdminField("description", "Descripción", FieldKind.MULTILINE, required = true),
            AdminField("highlights", "Puntos destacados (uno por línea)", FieldKind.LIST),
            AdminField("coverImageUrl", "Imagen de portada", FieldKind.IMAGE, required = true),
            AdminField("galleryImages", "Galería (hasta 12 imágenes)", FieldKind.IMAGES),
            AdminField("isPublished", "Publicada", FieldKind.BOOL, default = "true"),
        ),
    )

    const val GROUP_ACCOUNTS = "Cuentas"
    const val GROUP_LISTINGS = "Publicaciones"
    const val GROUP_FINANCE = "Membresías y planes"
    const val GROUP_REWARDS = "Referidos y premios"
    const val GROUP_SUPPORT = "Atención al cliente"
    const val GROUP_INVEST = "Inversiones"

    val All: List<AdminSection> = listOf(
        AdminSection(
            key = "professionals", title = "Profesionales", description = "Aprobar o rechazar cuentas de agentes, inmobiliarias y desarrolladoras",
            group = GROUP_ACCOUNTS, path = "admin/professionals",
            titlePaths = listOf("businessName", "user.firstName+user.lastName"),
            subtitlePaths = listOf("user.email"),
            details = listOf(
                AdminDetailSpec("Tipo", listOf("type")),
                AdminDetailSpec("Teléfono", listOf("user.phone")),
                AdminDetailSpec("Ciudad", listOf("city")),
                AdminDetailSpec("RUC", listOf("ruc")),
            ),
            actions = listOf(
                AdminAction("Aprobar", HttpVerb.PATCH, "admin/professionals/{id}/status", ActionBody(mapOf("status" to "APPROVED")), appliesTo = setOf("PENDING", "REJECTED")),
                AdminAction("Rechazar", HttpVerb.PATCH, "admin/professionals/{id}/status", ActionBody(mapOf("status" to "REJECTED")), appliesTo = setOf("PENDING", "APPROVED")),
                delete("admin/professionals/{id}"),
            ),
            filters = listOf(
                AdminFilter("Pendientes", mapOf("status" to "PENDING")),
                AdminFilter("Aprobados", mapOf("status" to "APPROVED")),
                AdminFilter("Rechazados", mapOf("status" to "REJECTED")),
                AdminFilter("Todos"),
            ),
        ),
        partner("agencies", "Inmobiliarias", "Revisar las propiedades y proyectos de cada inmobiliaria", "admin/agencies"),
        partner("developers", "Desarrolladoras", "Revisar las propiedades y proyectos de cada desarrolladora", "admin/developers"),
        AdminSection(
            key = "properties", title = "Propiedades de Ariz", description = "Propiedades publicadas por el equipo de Ariz",
            group = GROUP_LISTINGS, path = "admin/properties", externalForm = true,
            titlePaths = listOf("title"), subtitlePaths = listOf("location.district+location.department"),
            details = listOf(AdminDetailSpec("Precio", listOf("price.amount")), AdminDetailSpec("Moneda", listOf("price.currency"))),
            actions = listOf(AdminAction("Publicar", HttpVerb.PATCH, "admin/properties/{id}/publish", appliesTo = setOf("DRAFT", "PENDING_REVIEW", "PAUSED", "REJECTED"))),
        ),
        AdminSection(
            key = "projects", title = "Proyectos", description = "Publicar o eliminar proyectos inmobiliarios",
            group = GROUP_LISTINGS, path = "admin/projects/all", externalForm = true, sitePlan = true,
            titlePaths = listOf("name"), subtitlePaths = listOf("location.district+location.department"),
            details = listOf(AdminDetailSpec("Lotes", listOf("lotsCount")), AdminDetailSpec("Precio desde", listOf("priceFrom"))),
            actions = listOf(
                AdminAction("Publicar", HttpVerb.PATCH, "admin/projects/{id}/publish", appliesTo = setOf("DRAFT", "PENDING_REVIEW", "PAUSED", "REJECTED")),
                delete("admin/projects/{id}"),
            ),
        ),
        AdminSection(
            key = "subscriptions", title = "Suscripciones", description = "Revisar comprobantes de pago de los planes",
            group = GROUP_FINANCE, path = "admin/subscriptions/pending",
            titlePaths = listOf("agentProfile.businessName", "agentProfile.user.firstName+agentProfile.user.lastName"),
            subtitlePaths = listOf("plan.name"),
            details = listOf(
                AdminDetailSpec("Correo", listOf("agentProfile.user.email")),
                AdminDetailSpec("Precio", listOf("plan.price")),
                AdminDetailSpec("Días", listOf("plan.durationDays")),
                AdminDetailSpec("Vence", listOf("expiresAt")),
            ),
            actions = listOf(
                AdminAction("Aprobar", HttpVerb.PATCH, "admin/subscriptions/{id}/review", ActionBody(mapOf("status" to "ACTIVE")), appliesTo = setOf("PENDING_PAYMENT", "REJECTED")),
                AdminAction("Rechazar", HttpVerb.PATCH, "admin/subscriptions/{id}/review", ActionBody(mapOf("status" to "REJECTED")), appliesTo = setOf("PENDING_PAYMENT", "ACTIVE"), reasonKey = "rejectionReason"),
            ),
            filters = listOf(AdminFilter("Pendientes"), AdminFilter("Todas", path = "admin/subscriptions")),
            linkPath = "receiptUrl",
        ),
        AdminSection(
            key = "plans", title = "Planes", description = "Activar o desactivar los planes disponibles",
            group = GROUP_FINANCE, path = "admin/plans",
            titlePaths = listOf("name"), subtitlePaths = listOf("price+currency"), statusPath = "isActive",
            details = listOf(
                AdminDetailSpec("Duración (días)", listOf("durationDays")),
                AdminDetailSpec("Propiedades", listOf("propertyLimit")),
                AdminDetailSpec("Proyectos", listOf("projectLimit")),
            ),
            actions = toggle("admin/plans/{id}", "isActive"),
            form = planForm,
        ),
        AdminSection(
            key = "referrals", title = "Ranking de referidos", description = "Miembros Ariz con más puntos y referidos",
            group = GROUP_REWARDS, path = "admin/referrals/ranking",
            titlePaths = listOf("name"), subtitlePaths = listOf("code"), statusPath = null,
            details = listOf(
                AdminDetailSpec("Puesto", listOf("rank")),
                AdminDetailSpec("Puntos", listOf("points")),
                AdminDetailSpec("Referidos", listOf("referralsCount")),
                AdminDetailSpec("Concretados", listOf("completedCount")),
                AdminDetailSpec("Correo", listOf("email")),
                AdminDetailSpec("Teléfono", listOf("phone")),
            ),
            actions = listOf(delete("admin/referrals/referrers/{id}")),
            // Detalle del miembro (como la página del web): sus referidos con cambio de estado y los puntos por campaña.
            detailPath = "admin/referrals/referrers/{id}",
            detailChildren = listOf(
                AdminChildSpec(
                    "Referidos", "referrals", listOf("name"),
                    actions = referralStatusActions(),
                    details = listOf(
                        AdminDetailSpec("Puntos", listOf("points")),
                        AdminDetailSpec("Interés", listOf("interest")),
                        AdminDetailSpec("Proyecto", listOf("project.name")),
                        AdminDetailSpec("Fecha", listOf("createdAt")),
                    ),
                ),
                AdminChildSpec(
                    "Puntos por campaña", "campaignPoints", listOf("campaign.name"), statusPath = "none", idPath = "campaign.id",
                    details = listOf(AdminDetailSpec("Puntos de la campaña", listOf("totalPoints"))),
                ),
            ),
            exports = listOf(
                AdminExport("Ranking (Excel)", "admin/referrals/ranking/export/excel", "ranking-referidos.xlsx"),
                AdminExport("Miembros (Excel)", "admin/referrals/export/excel", "miembros-y-puntos.xlsx"),
                AdminExport("Miembros (PDF)", "admin/referrals/export/pdf", "miembros-y-puntos.pdf"),
            ),
            form = referrerForm, formPath = "admin/referrals/referrers", canCreate = false,
        ),
        AdminSection(
            key = "referral-registrations", title = "Registros por invitación", description = "Personas que se registraron con el código de un miembro",
            group = GROUP_REWARDS, path = "admin/referrals/registrations",
            titlePaths = listOf("newReferrer.name"), subtitlePaths = listOf("inviter.name"), statusPath = null,
            details = listOf(
                AdminDetailSpec("Invitó", listOf("inviter.name")),
                AdminDetailSpec("Código", listOf("inviterCode")),
                AdminDetailSpec("Puntos", listOf("pointsAwarded")),
                AdminDetailSpec("Correo", listOf("newReferrer.email")),
                AdminDetailSpec("Teléfono", listOf("newReferrer.phone")),
                AdminDetailSpec("Fecha", listOf("createdAt")),
            ),
        ),
        AdminSection(
            key = "project-points", title = "Puntos por proyecto", description = "Puntos que gana el referidor según el estado, por proyecto de Ariz",
            group = GROUP_REWARDS, path = "admin/campaigns/project-points",
            titlePaths = listOf("name"), subtitlePaths = listOf("slug"), statusPath = null,
            details = listOf(
                AdminDetailSpec("Contacto", listOf("points.CONTACTED")),
                AdminDetailSpec("Visita", listOf("points.VISIT")),
                AdminDetailSpec("Concretado", listOf("points.OPERATION_COMPLETED")),
            ),
            form = projectPointsForm, canCreate = false,
        ),
        AdminSection(
            key = "access-codes", title = "Códigos de acceso a planos", description = "Códigos temporales para ver el plano de un proyecto",
            group = GROUP_LISTINGS, path = "admin/projects/site-plan-access-codes",
            titlePaths = listOf("name"), subtitlePaths = listOf("slug"), statusPath = "configured",
            details = listOf(AdminDetailSpec("Vence", listOf("expiresAt"))),
            actions = listOf(1, 7, 30).map { days ->
                AdminAction(
                    if (days == 1) "Generar código (1 día)" else "Generar código ($days días)",
                    HttpVerb.POST, "admin/projects/{id}/site-plan/access-code", ActionBody(numbers = mapOf("expiresInDays" to days)),
                    resultKey = "code", resultTitle = "Código de acceso",
                )
            } + AdminAction("Revocar código", HttpVerb.DELETE, "admin/projects/{id}/site-plan/access-code", appliesTo = setOf("ACTIVE"), destructive = true),
        ),
        AdminSection(
            key = "status-points", title = "Puntos por estado", description = "Puntos que gana un referido en cada etapa (valor general)",
            group = GROUP_REWARDS, path = "admin/referrals", statusPath = null,
            titlePaths = listOf("title"), singleton = SingletonSpec("statusPoints", "status", "points"),
            details = listOf(
                AdminDetailSpec("Contacto", listOf("CONTACTED")),
                AdminDetailSpec("Visita", listOf("VISIT")),
                AdminDetailSpec("Operación concretada", listOf("OPERATION_COMPLETED")),
            ),
            form = statusPointsForm, formPath = "admin/referrals/status-points", formIdless = true, canCreate = false,
        ),
        AdminSection(
            key = "referral-records", title = "Registros de referidos", description = "Validar y avanzar los referidos que envían los miembros",
            group = GROUP_REWARDS, path = "admin/referrals/records",
            titlePaths = listOf("name"), subtitlePaths = listOf("referrer.name"),
            details = listOf(
                AdminDetailSpec("Puntos", listOf("points")),
                AdminDetailSpec("Interés", listOf("interestLabel")),
                AdminDetailSpec("Disponibilidad", listOf("availabilityLabel")),
                AdminDetailSpec("Proyecto", listOf("project.name")),
                AdminDetailSpec("Correo", listOf("email")),
                AdminDetailSpec("Teléfono", listOf("phone")),
                AdminDetailSpec("Fecha", listOf("createdAt")),
            ),
            actions = listOf(
                AdminAction("Aprobar", HttpVerb.PATCH, "admin/referrals/{id}/approve", appliesTo = setOf("PENDING_VALIDATION")),
                AdminAction("Rechazar", HttpVerb.PATCH, "admin/referrals/{id}/reject", appliesTo = setOf("PENDING_VALIDATION")),
                AdminAction("Marcar visita", HttpVerb.PATCH, "admin/referrals/{id}/status", ActionBody(mapOf("status" to "VISIT")), appliesTo = setOf("CONTACTED")),
                AdminAction("Operación concretada", HttpVerb.PATCH, "admin/referrals/{id}/status", ActionBody(mapOf("status" to "OPERATION_COMPLETED")), appliesTo = setOf("VISIT")),
                delete("admin/referrals/records/{id}"),
            ),
            filters = listOf(
                AdminFilter("Por validar", mapOf("status" to "PENDING_VALIDATION")),
                AdminFilter("Contactados", mapOf("status" to "CONTACTED")),
                AdminFilter("Visitas", mapOf("status" to "VISIT")),
                AdminFilter("Concretados", mapOf("status" to "OPERATION_COMPLETED")),
                AdminFilter("Todos"),
            ),
            exports = listOf(AdminExport("Exportar Excel", "admin/referrals/records/export/excel", "referidos-ariz.xlsx")),
            form = recordPointsForm, formPath = "admin/referrals/records", formSuffix = "/points", canCreate = false,
        ),
        AdminSection(
            key = "redemptions", title = "Canjes", description = "Aprobar, rechazar y entregar canjes de puntos",
            group = GROUP_REWARDS, path = "admin/redemptions",
            titlePaths = listOf("incentiveName"), subtitlePaths = listOf("referrer.name"),
            details = listOf(
                AdminDetailSpec("Puntos", listOf("pointsSpent")),
                AdminDetailSpec("Código", listOf("referralCode")),
                AdminDetailSpec("Solicitado", listOf("requestedAt")),
                AdminDetailSpec("Motivo", listOf("rejectionReason")),
            ),
            actions = listOf(
                AdminAction("Aprobar", HttpVerb.PATCH, "admin/redemptions/{id}/approve", appliesTo = setOf("PENDING")),
                AdminAction("Rechazar", HttpVerb.PATCH, "admin/redemptions/{id}/reject", appliesTo = setOf("PENDING"), reasonKey = "reason"),
                AdminAction("Marcar entregado", HttpVerb.PATCH, "admin/redemptions/{id}/deliver", appliesTo = setOf("APPROVED")),
            ),
            filters = listOf(
                AdminFilter("Pendientes", mapOf("status" to "PENDING")),
                AdminFilter("Aprobados", mapOf("status" to "APPROVED")),
                AdminFilter("Entregados", mapOf("status" to "DELIVERED")),
                AdminFilter("Rechazados", mapOf("status" to "REJECTED")),
            ),
        ),
        AdminSection(
            key = "incentives", title = "Incentivos", description = "Premios que los miembros pueden canjear",
            group = GROUP_REWARDS, path = "admin/incentives",
            titlePaths = listOf("name"), subtitlePaths = listOf("promotion"),
            details = listOf(AdminDetailSpec("Puntos requeridos", listOf("pointsRequired")), AdminDetailSpec("Stock", listOf("stock"))),
            actions = listOf(
                AdminAction("Desactivar", HttpVerb.PATCH, "admin/incentives/{id}/status", ActionBody(mapOf("status" to "INACTIVE")), appliesTo = Done),
                AdminAction("Activar", HttpVerb.PATCH, "admin/incentives/{id}/status", ActionBody(mapOf("status" to "ACTIVE")), appliesTo = Off),
                delete("admin/incentives/{id}"),
            ),
            form = incentiveForm,
        ),
        AdminSection(
            key = "campaigns", title = "Campañas", description = "Campañas de referidos activas",
            group = GROUP_REWARDS, path = "admin/campaigns",
            titlePaths = listOf("name"), subtitlePaths = listOf("code"), statusPath = "isActive",
            details = listOf(AdminDetailSpec("Inicio", listOf("startDate")), AdminDetailSpec("Fin", listOf("endDate"))),
            detailPath = "admin/campaigns/{id}",
            actions = toggle("admin/campaigns/{id}", "isActive") + delete("admin/campaigns/{id}"),
            form = campaignForm,
        ),
        AdminSection(
            key = "promotions", title = "Promociones", description = "Promociones que se muestran en el inicio",
            group = GROUP_REWARDS, path = "admin/promotions",
            titlePaths = listOf("title"), statusPath = "isActive",
            details = listOf(AdminDetailSpec("Inicio", listOf("startsAt")), AdminDetailSpec("Fin", listOf("endsAt"))),
            actions = toggle("admin/promotions/{id}", "isActive") + delete("admin/promotions/{id}"),
            form = promotionForm,
        ),
        AdminSection(
            key = "claims", title = "Libro de reclamaciones", description = "Reclamos y quejas de los usuarios",
            group = GROUP_SUPPORT, path = "admin/claims",
            exports = listOf(AdminExport("Exportar PDF", "admin/claims/export/pdf", "libro-reclamaciones.pdf")),
            titlePaths = listOf("claimCode"), subtitlePaths = listOf("fullName"), statusPath = null,
            details = listOf(
                AdminDetailSpec("Tipo", listOf("type")),
                AdminDetailSpec("Correo", listOf("email")),
                AdminDetailSpec("Teléfono", listOf("phone")),
                AdminDetailSpec("Monto reclamado", listOf("claimedAmount")),
                AdminDetailSpec("Detalle", listOf("claimDetails")),
                AdminDetailSpec("Pedido", listOf("consumerRequest")),
                AdminDetailSpec("Fecha", listOf("createdAt")),
            ),
            actions = listOf(delete("admin/claims/{id}")),
        ),
        AdminSection(
            key = "contact-reports", title = "Reportes de contacto", description = "Mensajes enviados desde el formulario de contacto",
            group = GROUP_SUPPORT, path = "admin/contact-reports",
            titlePaths = listOf("topic"), subtitlePaths = listOf("name"), statusPath = null,
            details = listOf(
                AdminDetailSpec("Correo", listOf("email")),
                AdminDetailSpec("Teléfono", listOf("phone")),
                AdminDetailSpec("Mensaje", listOf("message")),
                AdminDetailSpec("Fecha", listOf("createdAt")),
            ),
            actions = listOf(delete("admin/contact-reports/{id}")),
        ),
        AdminSection(
            key = "investments", title = "Oportunidades de inversión", description = "Oportunidades de inversión publicadas",
            group = GROUP_INVEST, path = "admin/investment-opportunities",
            titlePaths = listOf("name"), subtitlePaths = listOf("location"), statusPath = "isPublished",
            details = listOf(
                AdminDetailSpec("Inversión inicial", listOf("initialInvestment")),
                AdminDetailSpec("Retorno (%)", listOf("roiPercent")),
                AdminDetailSpec("Riesgo", listOf("riskLevel")),
            ),
            actions = listOf(delete("admin/investment-opportunities/{id}")),
            form = investmentForm,
        ),
        AdminSection(
            key = "investment-types", title = "Tipos de inversión", description = "Categorías para clasificar las oportunidades",
            group = GROUP_INVEST, path = "admin/investment-opportunities/types",
            titlePaths = listOf("name"), subtitlePaths = listOf("description"), statusPath = "isActive",
            details = listOf(AdminDetailSpec("Orden", listOf("sortOrder"))),
            actions = toggle("admin/investment-opportunities/types/{id}", "isActive") + delete("admin/investment-opportunities/types/{id}"),
            form = investmentTypeForm,
        ),
    )

    fun byKey(key: String): AdminSection? = All.firstOrNull { it.key == key }

    /** Secciones agrupadas en el orden del catálogo. */
    fun grouped(): List<Pair<String, List<AdminSection>>> = All.groupBy { it.group }.toList()
}
