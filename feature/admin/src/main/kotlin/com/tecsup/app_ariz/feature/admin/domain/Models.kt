package com.tecsup.app_ariz.feature.admin.domain

/** Marca los errores de validación locales del formulario: el texto que sigue son las claves inválidas. */
const val INVALID_PREFIX = "invalid:"

enum class HttpVerb { GET, POST, PATCH, DELETE }

/** Cuerpo JSON sencillo de una acción: textos y banderas. */
data class ActionBody(
    val strings: Map<String, String> = emptyMap(),
    val flags: Map<String, Boolean> = emptyMap(),
    val numbers: Map<String, Int> = emptyMap(),
)

/**
 * Acción que el administrador puede ejecutar sobre un registro.
 *
 * @param path ruta relativa; `{id}` se reemplaza por el id del registro.
 * @param appliesTo estados en los que se ofrece (null = siempre).
 * @param reasonKey si no es null, se pide un motivo y se envía en ese campo del cuerpo.
 */
data class AdminAction(
    val label: String,
    val verb: HttpVerb,
    val path: String,
    val body: ActionBody = ActionBody(),
    val appliesTo: Set<String>? = null,
    val reasonKey: String? = null,
    val destructive: Boolean = false,
    /** Texto del campo que se pide antes de ejecutar (por ejemplo, el mensaje de un aviso). */
    val inputLabel: String? = null,
    /** Si no es null, tras el éxito se muestra el valor de esta clave de la respuesta (p. ej. un código generado). */
    val resultKey: String? = null,
    val resultTitle: String? = null,
) {
    fun isAvailableFor(status: String?): Boolean = appliesTo == null || (status != null && status in appliesTo)
    fun pathFor(id: String): String = path.replace("{id}", id)
}

/** Tipo de campo de un formulario de alta/edición. */
enum class FieldKind { TEXT, MULTILINE, NUMBER, INT, DATE, BOOL, IMAGE, IMAGES, CHOICE, MULTI, LIST }

/** Opciones que se piden al backend (por ejemplo, los tipos de inversión o los proyectos de ARIZ). */
data class RemoteOptions(val path: String, val valueKey: String = "id", val labelKey: String = "name")

data class AdminChoice(val value: String, val label: String)

/**
 * @param key nombre en el JSON; con punto (`points.VISIT`) va anidado.
 * @param createOnly solo se envía al crear (el backend no deja editarlo).
 */
data class AdminField(
    val key: String,
    val label: String,
    val kind: FieldKind = FieldKind.TEXT,
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val createOnly: Boolean = false,
    val default: String = "",
    /** Etiqueta en español de cada valor de [options] (si falta, se muestra el valor). */
    val optionLabels: Map<String, String> = emptyMap(),
    /** Opciones traídas del backend (CHOICE o MULTI). */
    val remote: RemoteOptions? = null,
    /** CHOICE opcional: texto de la opción vacía ("No mostrar", "Sin tipo"). */
    val noneLabel: String? = null,
)

/** Formulario de una sección: crear (POST a `section.path`) y editar (PATCH a `section.path/{id}`). */
data class AdminFormSpec(
    val title: String,
    val fields: List<AdminField>,
    /** Campos de lista que el backend exige aunque vayan vacíos al crear (p. ej. `projectIds`). */
    val emptyListsOnCreate: List<String> = emptyList(),
)

/** Descarga de un archivo (Excel/PDF) que genera el backend para una sección. */
data class AdminExport(val label: String, val path: String, val fileName: String)

data class AdminDetail(val label: String, val value: String)

/** Registros hijos del registro (por ejemplo, propiedades y proyectos pendientes de una inmobiliaria). */
data class AdminGroup(val label: String, val items: List<AdminItem>)

data class AdminItem(
    val id: String,
    val title: String,
    val subtitle: String?,
    /** Estado en bruto (`PUBLISHED`, `ACTIVE`, ...); la UI lo traduce. */
    val status: String?,
    val details: List<AdminDetail>,
    val actions: List<AdminAction>,
    val groups: List<AdminGroup> = emptyList(),
    /** URL de un comprobante o archivo asociado (se abre en el navegador). */
    val link: String? = null,
    /** Valores en bruto del registro (para precargar el formulario de edición). */
    val values: Map<String, String> = emptyMap(),
    /** Todos los campos del registro (para "Ver detalle"). */
    val fields: List<AdminDetail> = emptyList(),
)

/** Cómo leer un dato de un JSON: primera ruta con valor; `a.b+a.c` une varias rutas con un espacio. */
data class AdminDetailSpec(val label: String, val paths: List<String>)

data class AdminChildSpec(
    val label: String,
    val jsonKey: String,
    val titlePaths: List<String>,
    val statusPath: String = "status",
    val actions: List<AdminAction> = emptyList(),
    val details: List<AdminDetailSpec> = emptyList(),
    /** Ruta del identificador del hijo (algunos, como los puntos por campaña, no traen `id` propio). */
    val idPath: String = "id",
)

data class AdminFilter(val label: String, val query: Map<String, String> = emptyMap(), val path: String? = null)

data class AdminSection(
    val key: String,
    val title: String,
    val description: String,
    val group: String,
    val path: String,
    val titlePaths: List<String>,
    val subtitlePaths: List<String> = emptyList(),
    /** El alta/edición se abre en otra pantalla (la app la conecta); la sección solo muestra los botones. */
    val externalForm: Boolean = false,
    /** Con [externalForm], si además se puede editar (los proyectos solo se crean: editar reemplazaría los lotes). */
    val externalEdit: Boolean = true,
    val statusPath: String? = "status",
    val details: List<AdminDetailSpec> = emptyList(),
    val actions: List<AdminAction> = emptyList(),
    val children: List<AdminChildSpec> = emptyList(),
    /** Al abrir "Ver detalle" se pide este endpoint (`{id}`) y se muestran sus campos y [detailChildren]. */
    val detailPath: String? = null,
    val detailChildren: List<AdminChildSpec> = emptyList(),
    val filters: List<AdminFilter> = emptyList(),
    val linkPath: String? = null,
    val form: AdminFormSpec? = null,
    /** Ruta base de las altas/ediciones si difiere de [path] (por ejemplo, el listado es de solo lectura). */
    val formPath: String? = null,
    /** Sufijo tras el id al editar (por ejemplo `/points`). */
    val formSuffix: String = "",
    /** Si es false, el formulario solo sirve para editar (no se ofrece "Nuevo"). */
    val canCreate: Boolean = true,
    /** El listado es un objeto único de configuración: filas `rowsKey` con clave `keyPath` y valor `valuePath`. */
    val singleton: SingletonSpec? = null,
    /** El PATCH del formulario va a [formPath] sin id (configuración global). */
    val formIdless: Boolean = false,
    val exports: List<AdminExport> = emptyList(),
    /** Cada registro tiene un plano (inventario de lotes o departamentos) que se edita en su propia pantalla. */
    val sitePlan: Boolean = false,
)

data class SingletonSpec(val rowsKey: String, val keyPath: String, val valuePath: String)

/** Conteos del dashboard (null si no se pudieron leer). */
data class AdminOverview(
    val pendingProfessionals: Int?,
    val pendingSubscriptions: Int?,
    val pendingRedemptions: Int?,
    val professionals: Int?,
    val projects: Int?,
    val claims: Int?,
)
