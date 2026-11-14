package com.tecsup.app_ariz.feature.professional.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizDropdownField
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizSecondaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.PanelSwitchRow
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.PropertyDraft
import com.tecsup.app_ariz.feature.professional.domain.PropertyField
import com.tecsup.app_ariz.feature.professional.domain.PropertyFieldError

/** Servicios que ofrece el web (`AMENITY_OPTIONS`): código del backend y su texto. */
private val Amenities = listOf(
    "PISCINA" to "Piscina", "JARDIN" to "Jardín", "AREA_BBQ" to "Área BBQ", "MASCOTAS_PERMITIDAS" to "Mascotas permitidas",
    "TERRAZA" to "Terraza", "ASCENSOR" to "Ascensor", "GIMNASIO" to "Gimnasio", "AMOBLADO" to "Amoblado",
    "BALCON" to "Balcón", "SEGURIDAD_24_7" to "Seguridad 24/7", "SALON_SOCIAL" to "Salón social",
)

private val OperationOptions = listOf("SALE", "RENT")
private val TypeOptions = listOf("APARTMENT", "HOUSE", "LAND", "OFFICE", "COMMERCIAL", "WAREHOUSE", "ROOM")

@Composable
internal fun PropertyFormScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PropertyFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PropertyFormEvent.Saved -> onSaved()
            }
        }
    }
    PropertyFormContent(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, modifier = modifier)
}

/** Formulario de publicación del web (`owner/listings/new`), adaptado a una sola columna. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PropertyFormContent(
    state: PropertyFormUiState,
    onAction: (PropertyFormAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = state.draft
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)) { uris ->
        onAction(PropertyFormAction.PhotosPicked(uris.map { it.toString() }))
    }
    fun edit(change: (PropertyDraft) -> PropertyDraft) = onAction(PropertyFormAction.Edit(change))
    val errorTexts = PropertyField.entries.associateWith { field -> state.errors[field]?.let { stringResource(it.textRes()) } }
    fun err(field: PropertyField): String? = errorTexts[field]

    PanelPage(
        eyebrow = stringResource(R.string.pro_panel_eyebrow),
        title = stringResource(if (state.isEditing) R.string.pro_form_title_edit else R.string.pro_form_title_new),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.pro_back),
        modifier = modifier,
    ) {
        if (state.isLoading) {
            PanelLoading()
            return@PanelPage
        }
        if (state.isEditing && draft.title.isBlank() && state.error != null) {
            PanelError(state.error.message, stringResource(R.string.pro_retry)) { onAction(PropertyFormAction.Retry) }
            return@PanelPage
        }

        Section(R.string.pro_form_section_basic) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArizDropdownField(
                    label = stringResource(R.string.pro_form_operation),
                    selected = draft.operation,
                    options = OperationOptions,
                    optionLabel = { stringResource(if (it == "RENT") R.string.pro_op_rent else R.string.pro_op_sale) },
                    onSelected = { value -> edit { it.copy(operation = value) } },
                    modifier = Modifier.weight(1f),
                )
                ArizDropdownField(
                    label = stringResource(R.string.pro_form_currency),
                    selected = draft.currency,
                    options = Currency.entries,
                    optionLabel = { stringResource(if (it == Currency.USD) R.string.pro_currency_usd else R.string.pro_currency_pen) },
                    onSelected = { value -> edit { it.copy(currency = value) } },
                    modifier = Modifier.weight(1f),
                )
            }
            ArizDropdownField(
                label = stringResource(R.string.pro_form_type),
                selected = draft.type,
                options = TypeOptions,
                optionLabel = { stringResource(typeLabelRes(it)) },
                onSelected = { value -> edit { it.copy(type = value) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Field(R.string.pro_form_title, draft.title, err(PropertyField.TITLE)) { v -> edit { it.copy(title = v) } }
            Field(R.string.pro_form_description, draft.description, err(PropertyField.DESCRIPTION), minLines = 4) { v -> edit { it.copy(description = v) } }
            Field(R.string.pro_form_price, draft.price, err(PropertyField.PRICE), KeyboardType.Decimal) { v -> edit { it.copy(price = v.numeric()) } }
            Field(R.string.pro_form_maintenance, draft.maintenanceFee, err(PropertyField.MAINTENANCE_FEE), KeyboardType.Decimal) { v ->
                edit { it.copy(maintenanceFee = v.numeric()) }
            }
            PanelSwitchRow(stringResource(R.string.pro_form_negotiable), null, draft.negotiable) { v -> edit { it.copy(negotiable = v) } }
        }

        Section(R.string.pro_form_section_location) {
            Field(R.string.pro_form_department, draft.department) { v -> edit { it.copy(department = v) } }
            Field(R.string.pro_form_province, draft.province) { v -> edit { it.copy(province = v) } }
            Field(R.string.pro_form_district, draft.district, err(PropertyField.DISTRICT)) { v -> edit { it.copy(district = v) } }
            Field(R.string.pro_form_address, draft.address, err(PropertyField.ADDRESS)) { v -> edit { it.copy(address = v) } }
            Field(R.string.pro_form_reference, draft.reference) { v -> edit { it.copy(reference = v) } }
            PanelSwitchRow(stringResource(R.string.pro_form_approximate), null, draft.approximateLocation) { v ->
                edit { it.copy(approximateLocation = v) }
            }
        }

        Section(R.string.pro_form_section_details) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Field(R.string.pro_form_total_area, draft.totalArea, err(PropertyField.TOTAL_AREA), KeyboardType.Decimal, Modifier.weight(1f)) { v ->
                    edit { it.copy(totalArea = v.numeric()) }
                }
                Field(R.string.pro_form_constructed_area, draft.constructedArea, err(PropertyField.CONSTRUCTED_AREA), KeyboardType.Decimal, Modifier.weight(1f)) { v ->
                    edit { it.copy(constructedArea = v.numeric()) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Field(R.string.pro_form_bedrooms, draft.bedrooms, null, KeyboardType.Number, Modifier.weight(1f)) { v -> edit { it.copy(bedrooms = v.digits()) } }
                Field(R.string.pro_form_bathrooms, draft.bathrooms, null, KeyboardType.Number, Modifier.weight(1f)) { v -> edit { it.copy(bathrooms = v.digits()) } }
                Field(R.string.pro_form_parking, draft.parkingSpaces, null, KeyboardType.Number, Modifier.weight(1f)) { v -> edit { it.copy(parkingSpaces = v.digits()) } }
            }
        }

        Section(R.string.pro_form_section_amenities) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Amenities.forEach { (code, label) ->
                    val selected = code in draft.amenities
                    Text(
                        text = label,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) ArizColors.Brand else MaterialTheme.colorScheme.surface)
                            .clickable { edit { it.copy(amenities = if (selected) it.amenities - code else it.amenities + code) } }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selected) Color.White else ArizColors.Brand,
                    )
                }
            }
        }

        Section(R.string.pro_form_section_photos) {
            ArizSecondaryButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.pro_form_add_photos), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
            if (state.uploading > 0) {
                Text(stringResource(R.string.pro_form_uploading, state.uploading), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
            }
            state.photoError?.let { ArizAlert(message = it) }
            err(PropertyField.PHOTOS)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            if (draft.photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(draft.photos, key = { _, url -> url }) { index, url ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(110.dp).clip(RoundedCornerShape(10.dp)).background(ArizColors.SurfaceMuted)) {
                                AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(110.dp), contentScale = ContentScale.Crop)
                                if (index == 0) {
                                    Text(
                                        stringResource(R.string.pro_form_cover),
                                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp).background(ArizColors.Accent, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                        color = ArizColors.OnAccent,
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (index != 0) Text(stringResource(R.string.pro_form_make_cover), modifier = Modifier.clickable { onAction(PropertyFormAction.MakeCover(url)) }, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand)
                                Text(stringResource(R.string.pro_form_remove_photo), modifier = Modifier.clickable { onAction(PropertyFormAction.RemovePhoto(url)) }, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Error)
                            }
                        }
                    }
                }
            }
        }

        Section(R.string.pro_form_section_contact) {
            Field(R.string.pro_form_contact_name, draft.contactName) { v -> edit { it.copy(contactName = v) } }
            Field(R.string.pro_form_contact_email, draft.contactEmail, err(PropertyField.CONTACT_EMAIL), KeyboardType.Email) { v -> edit { it.copy(contactEmail = v) } }
            Field(R.string.pro_form_contact_phone, draft.contactPhone, null, KeyboardType.Phone) { v -> edit { it.copy(contactPhone = v) } }
        }

        state.error?.let { ArizAlert(message = it.message) }
        ArizSecondaryButton(
            onClick = { onAction(PropertyFormAction.Save(publish = false)) },
            enabled = !state.isSaving && state.uploading == 0,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(if (state.isEditing) R.string.pro_form_save_changes else R.string.pro_form_save_draft), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
        ArizPrimaryButton(
            text = stringResource(R.string.pro_form_save_publish),
            onClick = { onAction(PropertyFormAction.Save(publish = true)) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isSaving,
            enabled = state.uploading == 0,
        )
    }
}

@Composable
private fun Section(titleRes: Int, content: @Composable () -> Unit) {
    PanelCard {
        Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun Field(
    labelRes: Int,
    value: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    onChange: (String) -> Unit,
) {
    ArizTextField(
        value = value,
        onValueChange = onChange,
        label = stringResource(labelRes),
        modifier = modifier,
        errorText = error,
        keyboardType = keyboardType,
        singleLine = minLines == 1,
        minLines = minLines,
    )
}

/** Deja solo dígitos y un punto decimal (precios y áreas). */
private fun String.numeric(): String {
    val clean = filter { it.isDigit() || it == '.' }
    val firstDot = clean.indexOf('.')
    return if (firstDot < 0) clean else clean.substring(0, firstDot + 1) + clean.substring(firstDot + 1).replace(".", "")
}

private fun String.digits(): String = filter { it.isDigit() }.take(3)

private fun typeLabelRes(type: String) = when (type) {
    "HOUSE" -> R.string.pro_type_house
    "LAND" -> R.string.pro_type_land
    "OFFICE" -> R.string.pro_type_office
    "COMMERCIAL" -> R.string.pro_type_commercial
    "WAREHOUSE" -> R.string.pro_type_warehouse
    "ROOM" -> R.string.pro_type_room
    else -> R.string.pro_type_apartment
}

private fun PropertyFieldError.textRes() = when (this) {
    PropertyFieldError.REQUIRED -> R.string.pro_form_error_required
    PropertyFieldError.INVALID_NUMBER -> R.string.pro_form_error_number
    PropertyFieldError.TOO_LONG -> R.string.pro_form_error_long
    PropertyFieldError.INVALID_EMAIL -> R.string.pro_form_error_email
    PropertyFieldError.NEEDS_PHOTO -> R.string.pro_form_error_photo
}
