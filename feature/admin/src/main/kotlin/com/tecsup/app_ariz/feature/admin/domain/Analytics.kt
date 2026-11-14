package com.tecsup.app_ariz.feature.admin.domain

import java.time.YearMonth

/** Filtro por tipo de perfil del dashboard (igual que el web). */
enum class ProfileFilter { ALL, AGENCIES, AGENTS, USERS }

/** Ventana de tiempo del dashboard: 1, 6 o 12 meses. */
enum class RangeFilter(val months: Int) { ONE(1), SIX(6), TWELVE(12) }

/** Una fila de datos con fecha (`AAAA-MM-DD...`) y tipo de perfil de su dueño. */
data class AnalyticsRow(val date: String?, val type: String?)

data class AnalyticsProject(val row: AnalyticsRow, val name: String, val lots: Int)

data class ProfileSlice(val type: String, val count: Int)

/** Datos en bruto de los reportes; los filtros se aplican en el dispositivo con [buildAnalytics]. */
data class AnalyticsRaw(
    val professionals: List<AnalyticsRow> = emptyList(),
    val properties: List<AnalyticsRow> = emptyList(),
    val projects: List<AnalyticsProject> = emptyList(),
    val subscriptions: List<AnalyticsRow> = emptyList(),
    val distribution: List<ProfileSlice> = emptyList(),
)

data class MonthPoint(val label: String, val value: Int)

data class TopProject(val name: String, val lots: Int)

data class AnalyticsView(
    val accounts: List<MonthPoint>,
    val properties: List<MonthPoint>,
    val projects: List<MonthPoint>,
    val memberships: List<MonthPoint>,
    /** Propiedades + proyectos publicados por mes. */
    val publications: List<MonthPoint>,
    val topProjects: List<TopProject>,
    val distribution: List<ProfileSlice>,
) {
    val isEmpty: Boolean get() = listOf(accounts, properties, projects, memberships).all { list -> list.all { it.value == 0 } }
}

private val MonthNames = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

private const val AGENCY = "REAL_ESTATE_AGENCY"
private const val AGENT = "INDEPENDENT_AGENT"
private const val DEVELOPER = "DEVELOPER"
private const val OWNER = "INDIVIDUAL_OWNER"

/** Las cuentas profesionales: "inmobiliarias" no incluye desarrolladoras y "usuarios" no tiene profesionales. */
private fun matchesAccount(type: String?, filter: ProfileFilter) = when (filter) {
    ProfileFilter.ALL -> true
    ProfileFilter.AGENCIES -> type == AGENCY
    ProfileFilter.AGENTS -> type == AGENT
    ProfileFilter.USERS -> false
}

/** Publicaciones y membresías: "inmobiliarias" incluye a las desarrolladoras. */
private fun matchesListing(type: String?, filter: ProfileFilter) = when (filter) {
    ProfileFilter.ALL -> true
    ProfileFilter.AGENCIES -> type == AGENCY || type == DEVELOPER
    ProfileFilter.AGENTS -> type == AGENT
    ProfileFilter.USERS -> type == OWNER
}

private fun monthOf(date: String?): YearMonth? =
    date?.takeIf { it.length >= 7 }?.let { runCatching { YearMonth.parse(it.substring(0, 7)) }.getOrNull() }

/**
 * Cuenta por mes los últimos `range.months` meses terminando en [today]. Si no hay ninguna fecha usable pero sí filas,
 * las pone todas en el mes actual (como el web) para que la gráfica no quede en cero.
 */
internal fun monthlyTrend(dates: List<String?>, range: RangeFilter, today: YearMonth): List<MonthPoint> {
    val months = (range.months - 1 downTo 0).map { today.minusMonths(it.toLong()) }
    val counts = months.associateWith { 0 }.toMutableMap()
    dates.forEach { date -> monthOf(date)?.let { month -> if (month in counts) counts[month] = counts.getValue(month) + 1 } }
    if (counts.values.all { it == 0 } && dates.isNotEmpty()) counts[today] = dates.size
    return months.map { MonthPoint(MonthNames[it.monthValue - 1], counts.getValue(it)) }
}

fun buildAnalytics(raw: AnalyticsRaw, profile: ProfileFilter, range: RangeFilter, today: YearMonth = YearMonth.now()): AnalyticsView {
    val accounts = raw.professionals.filter { matchesAccount(it.type, profile) }
    val properties = raw.properties.filter { matchesListing(it.type, profile) }
    val projects = raw.projects.filter { matchesListing(it.row.type, profile) }
    val memberships = raw.subscriptions.filter { matchesListing(it.type, profile) }
    val propertyTrend = monthlyTrend(properties.map { it.date }, range, today)
    val projectTrend = monthlyTrend(projects.map { it.row.date }, range, today)
    return AnalyticsView(
        accounts = monthlyTrend(accounts.map { it.date }, range, today),
        properties = propertyTrend,
        projects = projectTrend,
        memberships = monthlyTrend(memberships.map { it.date }, range, today),
        publications = propertyTrend.zip(projectTrend) { a, b -> MonthPoint(a.label, a.value + b.value) },
        topProjects = projects.filter { it.lots > 0 }.sortedByDescending { it.lots }.take(10).map { TopProject(it.name, it.lots) },
        distribution = raw.distribution,
    )
}
