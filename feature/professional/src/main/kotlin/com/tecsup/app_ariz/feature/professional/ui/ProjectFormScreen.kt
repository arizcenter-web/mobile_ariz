package com.tecsup.app_ariz.feature.professional.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.LotDraft
import com.tecsup.app_ariz.feature.professional.domain.ProjectDraft
import com.tecsup.app_ariz.feature.professional.domain.ProjectField
import com.tecsup.app_ariz.feature.professional.domain.ProjectFieldError

private val Stages = listOf("PRE_SALE", "UNDER_CONSTRUCTION", "DELIVERED")

@Composable
internal fun ProjectFormScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { onSaved() }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)) { uris ->
        viewModel.onAction(ProjectFormAction.PhotosPicked(uris.map { it.toString() }))
    }
    val draft = state.draft
    fun edit(change: (ProjectDraft) -> ProjectDraft) = viewModel.onAction(ProjectFormAction.Edit(change))
    val errorTexts = ProjectField.entries.associateWith { field -> state.errors[field]?.let { stringResource(it.textRes()) } }
    fun err(field: ProjectField): String? = errorTexts[field]

    PanelPage(
        eyebrow = stringResource(R.string.pro_panel_eyebrow),
        title = stringResource(if (state.isEditing) R.string.pro_project_form_title_edit else R.string.pro_project_form_title),
        subtitle = stringResource(R.string.pro_project_form_hint),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.pro_back),
        modifier = modifier,
    ) {
        if (state.isLoading) {
            PanelLoading()
            return@PanelPage
        }
        // Si no se puede editar aquí (departamentos, plano interactivo) solo se muestra el motivo.
        if (state.isEditing && state.error != null && draft == ProjectDraft()) {
            ArizAlert(message = state.error!!.message)
            return@PanelPage
        }
        Block(R.string.pro_form_section_basic) {
            if (state.isAdmin && !state.isEditing) {
                ArizDropdownField(
                    label = stringResource(R.string.pro_project_category),
                    selected = draft.category,
                    options = listOf("LOTIZACION", "DEPARTAMENTOS"),
                    optionLabel = { stringResource(if (it == "DEPARTAMENTOS") R.string.pro_project_cat_apts else R.string.pro_project_cat_lots) },
                    onSelected = { value -> edit { it.copy(category = value) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Field(R.string.pro_form_name, draft.name, err(ProjectField.NAME)) { v -> edit { it.copy(name = v) } }
            Field(R.string.pro_form_description, draft.description, err(ProjectField.DESCRIPTION), minLines = 4) { v -> edit { it.copy(description = v) } }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArizDropdownField(
                    label = stringResource(R.string.pro_project_stage),
                    selected = draft.stage,
                    options = Stages,
                    optionLabel = { stringResource(stageLabelRes(it)) },
                    onSelected = { value -> edit { it.copy(stage = value) } },
                    modifier = Modifier.weight(1f),
                )
                ArizDropdownField(
                    label = stringResource(R.string.pro_form_currency),
                    selected = Currency.entries.first { it.name == draft.currency },
                    options = Currency.entries,
                    optionLabel = { stringResource(if (it == Currency.USD) R.string.pro_currency_usd else R.string.pro_currency_pen) },
                    onSelected = { value -> edit { it.copy(currency = value.name) } },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Field(R.string.pro_project_price_from, draft.priceFrom, err(ProjectField.PRICE_FROM), KeyboardType.Decimal, Modifier.weight(1f)) { v -> edit { it.copy(priceFrom = v.numeric()) } }
                Field(R.string.pro_project_price_to, draft.priceTo, err(ProjectField.PRICE_TO), KeyboardType.Decimal, Modifier.weight(1f)) { v -> edit { it.copy(priceTo = v.numeric()) } }
            }
            Field(R.string.pro_project_delivery, draft.estimatedDelivery, err(ProjectField.DELIVERY)) { v -> edit { it.copy(estimatedDelivery = v.take(10)) } }
        }

        Block(R.string.pro_form_section_location) {
            Field(R.string.pro_form_department, draft.department, err(ProjectField.DEPARTMENT)) { v -> edit { it.copy(department = v) } }
            Field(R.string.pro_form_province, draft.province, err(ProjectField.PROVINCE)) { v -> edit { it.copy(province = v) } }
            Field(R.string.pro_form_district, draft.district, err(ProjectField.DISTRICT)) { v -> edit { it.copy(district = v) } }
            Field(R.string.pro_project_zone, draft.zone) { v -> edit { it.copy(zone = v) } }
            Field(R.string.pro_form_address, draft.street) { v -> edit { it.copy(street = v) } }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Field(R.string.pro_project_latitude, draft.latitude, err(ProjectField.LATITUDE), KeyboardType.Decimal, Modifier.weight(1f)) { v -> edit { it.copy(latitude = v.signedNumeric()) } }
                Field(R.string.pro_project_longitude, draft.longitude, err(ProjectField.LONGITUDE), KeyboardType.Decimal, Modifier.weight(1f)) { v -> edit { it.copy(longitude = v.signedNumeric()) } }
            }
            val pickerOpen = androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
            ArizSecondaryButton(onClick = { pickerOpen.value = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pro_map_pick), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
            }
            if (pickerOpen.value) {
                LocationPickerDialog(
                    initialLat = draft.latitude.toDoubleOrNull(),
                    initialLng = draft.longitude.toDoubleOrNull(),
                    onDismiss = { pickerOpen.value = false },
                    onPicked = { lat, lng ->
                        edit { it.copy(latitude = "%.6f".format(java.util.Locale.US, lat), longitude = "%.6f".format(java.util.Locale.US, lng)) }
                        pickerOpen.value = false
                    },
                )
            }
            Text(stringResource(R.string.pro_project_coords_hint), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
        }

        if (draft.managedByPlan || draft.category == "DEPARTAMENTOS") {
            Text(stringResource(R.string.pro_project_plan_hint), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
        } else Block(R.string.pro_project_lots_section) {
            err(ProjectField.LOTS)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            draft.lots.forEachIndexed { index, lot ->
                LotEditor(
                    index = index,
                    lot = lot,
                    onChange = { change -> viewModel.onAction(ProjectFormAction.EditLot(index, change)) },
                    onRemove = { viewModel.onAction(ProjectFormAction.RemoveLot(index)) },
                )
            }
            ArizSecondaryButton(onClick = { viewModel.onAction(ProjectFormAction.AddLot) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pro_project_add_lot), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
            }
        }

        Block(R.string.pro_form_section_photos) {
            ArizSecondaryButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.pro_form_add_photos), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
            if (state.uploading > 0) {
                Text(stringResource(R.string.pro_form_uploading, state.uploading), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
            }
            state.photoError?.let { ArizAlert(message = it) }
            err(ProjectField.PHOTOS)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            if (draft.photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(draft.photos, key = { _, url -> url }) { index, url ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(110.dp).clip(RoundedCornerShape(10.dp)).background(ArizColors.SurfaceMuted)) {
                                AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(110.dp), contentScale = ContentScale.Crop)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (index != 0) Text(stringResource(R.string.pro_form_make_cover), modifier = Modifier.clickable { viewModel.onAction(ProjectFormAction.MakeCover(url)) }, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand)
                                Text(stringResource(R.string.pro_form_remove_photo), modifier = Modifier.clickable { viewModel.onAction(ProjectFormAction.RemovePhoto(url)) }, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Error)
                            }
                        }
                    }
                }
            }
        }

        Block(R.string.pro_form_section_contact) {
            Field(R.string.pro_form_contact_name, draft.contactName) { v -> edit { it.copy(contactName = v) } }
            Field(R.string.pro_form_contact_email, draft.contactEmail, err(ProjectField.CONTACT_EMAIL), KeyboardType.Email) { v -> edit { it.copy(contactEmail = v) } }
            Field(R.string.pro_form_contact_phone, draft.contactPhone, null, KeyboardType.Phone) { v -> edit { it.copy(contactPhone = v) } }
            Field(R.string.pro_profile_whatsapp, draft.contactWhatsapp, null, KeyboardType.Phone) { v -> edit { it.copy(contactWhatsapp = v) } }
        }

        state.error?.let { ArizAlert(message = it.message) }
        ArizSecondaryButton(
            onClick = { viewModel.onAction(ProjectFormAction.Save(publish = false)) },
            enabled = !state.isSaving && state.uploading == 0,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.pro_form_save_draft), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
        ArizPrimaryButton(
            text = stringResource(R.string.pro_project_save_publish),
            onClick = { viewModel.onAction(ProjectFormAction.Save(publish = true)) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isSaving,
            enabled = state.uploading == 0,
        )
    }
}

@Composable
private fun LotEditor(index: Int, lot: LotDraft, onChange: ((LotDraft) -> LotDraft) -> Unit, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ArizColors.SurfaceMuted)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.pro_project_lot_n, index + 1),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            Text(stringResource(R.string.pro_form_remove_photo), modifier = Modifier.clickable(onClick = onRemove), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Error)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(R.string.pro_project_lot_number, lot.number, modifier = Modifier.weight(1f)) { v -> onChange { it.copy(number = v) } }
            Field(R.string.pro_project_lot_block, lot.block, modifier = Modifier.weight(1f)) { v -> onChange { it.copy(block = v) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(R.string.pro_project_lot_area, lot.area, keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)) { v -> onChange { it.copy(area = v.numeric()) } }
            Field(R.string.pro_project_lot_price, lot.price, keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)) { v -> onChange { it.copy(price = v.numeric()) } }
        }
    }
}

@Composable
private fun Block(titleRes: Int, content: @Composable () -> Unit) {
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

private fun String.numeric(): String {
    val clean = filter { it.isDigit() || it == '.' }
    val firstDot = clean.indexOf('.')
    return if (firstDot < 0) clean else clean.substring(0, firstDot + 1) + clean.substring(firstDot + 1).replace(".", "")
}

/** Coordenadas: permiten un `-` inicial (Perú está en latitudes y longitudes negativas). */
private fun String.signedNumeric(): String = (if (startsWith("-")) "-" else "") + numeric()

private fun stageLabelRes(stage: String) = when (stage) {
    "UNDER_CONSTRUCTION" -> R.string.pro_stage_construction
    "DELIVERED" -> R.string.pro_stage_delivered
    else -> R.string.pro_stage_presale
}

private fun ProjectFieldError.textRes() = when (this) {
    ProjectFieldError.REQUIRED -> R.string.pro_form_error_required
    ProjectFieldError.INVALID_NUMBER -> R.string.pro_form_error_number
    ProjectFieldError.TOO_LONG -> R.string.pro_form_error_long
    ProjectFieldError.INVALID_EMAIL -> R.string.pro_form_error_email
    ProjectFieldError.INVALID_DATE -> R.string.pro_project_error_date
    ProjectFieldError.NEEDS_PHOTO -> R.string.pro_form_error_photo
    ProjectFieldError.PRICE_RANGE -> R.string.pro_project_error_range
    ProjectFieldError.NEEDS_LOT -> R.string.pro_project_error_lot_needed
    ProjectFieldError.LOT_INVALID -> R.string.pro_project_error_lot_invalid
    ProjectFieldError.LOT_DUPLICATE -> R.string.pro_project_error_lot_duplicate
}
