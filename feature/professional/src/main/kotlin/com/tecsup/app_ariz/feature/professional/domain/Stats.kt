package com.tecsup.app_ariz.feature.professional.domain

import java.util.TimeZone

private const val DayMillis = 86_400_000L

/** Consultas de un día (las 14 barras de "Consultas - últimos 14 días"). */
data class DayCount(val dayStartMillis: Long, val count: Int)

data class SourceCount(val source: String, val count: Int)

data class PropertyActivity(val propertyId: String, val title: String, val leads: Int)

/** Números del dashboard y de la pantalla de estadísticas, calculados a partir de las listas del panel. */
data class ProfessionalStats(
    val publishedCount: Int,
    val pausedCount: Int,
    val otherCount: Int,
    val activeProjects: Int,
    val totalProjects: Int,
    val totalLeads: Int,
    val newLeads: Int,
    val leadsLast14Days: List<DayCount>,
    val leadsBySource: List<SourceCount>,
    val topProperties: List<PropertyActivity>,
    val daysRemaining: Int?,
) {
    val leadsInTrend: Int get() = leadsLast14Days.sumOf { it.count }
}

/** Reglas del web (`owner/page.tsx`, `chart-data.ts`): publicadas = PUBLISHED, pausadas = PAUSED, el resto "otras". */
object StatsCalculator {

    fun calculate(
        properties: List<OwnerProperty>,
        projects: List<OwnerProject>,
        leads: List<OwnerLead>,
        subscription: Subscription?,
        nowMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): ProfessionalStats {
        val published = properties.count { it.status == PropertyStatus.PUBLISHED }
        val paused = properties.count { it.status == PropertyStatus.PAUSED }
        val newLeads = leads.count { lead -> lead.createdAtMillis?.let { (nowMillis - it) / DayMillis <= 7 } == true }
        val daysRemaining = subscription?.takeIf { it.isActive }?.expiresAtMillis?.let {
            maxOf(0L, Math.ceil((it - nowMillis).toDouble() / DayMillis).toLong()).toInt()
        }
        return ProfessionalStats(
            publishedCount = published,
            pausedCount = paused,
            otherCount = properties.size - published - paused,
            activeProjects = projects.count { it.status == ProjectStatus.PUBLISHED },
            totalProjects = projects.size,
            totalLeads = leads.size,
            newLeads = newLeads,
            leadsLast14Days = leadsPerDay(leads, 14, nowMillis, timeZone),
            leadsBySource = leads.groupingBy { it.source.ifBlank { "OTHER" } }.eachCount()
                .map { SourceCount(it.key, it.value) }.sortedByDescending { it.count },
            topProperties = properties
                .map { property -> PropertyActivity(property.id, property.title, leads.count { it.targetId == property.id }) }
                .sortedByDescending { it.leads },
            daysRemaining = daysRemaining,
        )
    }

    /** Un cubo por día, el último es hoy; los días sin consultas valen 0. */
    fun leadsPerDay(leads: List<OwnerLead>, days: Int, nowMillis: Long, timeZone: TimeZone): List<DayCount> {
        fun dayStart(millis: Long): Long {
            val offset = timeZone.getOffset(millis)
            return Math.floorDiv(millis + offset, DayMillis) * DayMillis - offset
        }
        val today = dayStart(nowMillis)
        val starts = (days - 1 downTo 0).map { today - it * DayMillis }
        val counts = leads.mapNotNull { it.createdAtMillis }.groupingBy { dayStart(it) }.eachCount()
        return starts.map { DayCount(it, counts[it] ?: 0) }
    }
}
