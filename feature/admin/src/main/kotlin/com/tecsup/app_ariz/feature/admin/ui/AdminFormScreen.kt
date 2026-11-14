package com.tecsup.app_ariz.feature.admin.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.PanelSwitchRow
import com.tecsup.app_ariz.feature.admin.R
import com.tecsup.app_ariz.feature.admin.domain.AdminChoice
import com.tecsup.app_ariz.feature.admin.domain.AdminField
import com.tecsup.app_ariz.feature.admin.domain.FieldKind

/** Formulario de alta/edición dirigido por `AdminFormSpec` de la sección. */
@Composable
fun AdminFormScreen(onBackClick: () -> Unit, onSaved: () -> Unit, modifier: Modifier = Modifier, viewModel: AdminFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spec = state.section?.form
    LaunchedEffect(viewModel) { viewModel.saved.collect { onSaved() } }

    PanelPage(
        eyebrow = stringResource(R.string.adm_eyebrow),
        title = stringResource(if (state.isEditing) R.string.adm_form_edit else R.string.adm_form_new) + " " + spec?.title.orEmpty().lowercase(),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.adm_back),
        modifier = modifier,
    ) {
        if (spec == null) return@PanelPage
        when {
            state.isLoading -> PanelLoading()
            state.isEditing && state.error != null && state.values.values.all { it.isBlank() } ->
                PanelError(state.error!!.message, stringResource(R.string.adm_retry), onRetry = viewModel::load)
            else -> {
                PanelCard {
                    spec.fields.filterNot { state.isEditing && it.createOnly }.forEach { field ->
                        FieldInput(
                            field = field,
                            value = state.values[field.key].orEmpty(),
                            invalid = field.key in state.invalid,
                            choices = state.options[field.key].orEmpty(),
                            uploading = state.uploading,
                            onChange = { viewModel.set(field.key, it) },
                            onImage = { viewModel.imagePicked(field.key, it) },
                        )
                    }
                }
                state.error?.let { ArizAlert(message = it.message) }
                ArizPrimaryButton(
                    text = stringResource(R.string.adm_save),
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = state.isSaving,
                    enabled = !state.uploading,
                )
            }
        }
    }
}

@Composable
internal fun FieldInput(field: AdminField, value: String, invalid: Boolean, choices: List<AdminChoice>, uploading: Boolean, onChange: (String) -> Unit, onImage: (String) -> Unit) {
    val error = if (invalid) stringResource(R.string.adm_field_invalid) else null
    when (field.kind) {
        FieldKind.BOOL -> PanelSwitchRow(field.label, null, value == "true") { onChange(it.toString()) }
        FieldKind.CHOICE -> {
            // Valores y etiquetas: remotas (backend) o locales; con `noneLabel` hay una opción vacía.
            val pairs = (if (field.remote != null) choices.map { it.value to it.label } else field.options.map { it to (field.optionLabels[it] ?: it) })
                .let { list -> if (field.noneLabel != null) listOf("" to field.noneLabel) + list else list }
            if (pairs.isNotEmpty()) {
                val labels = pairs.toMap()
                ArizDropdownField(
                    label = field.label,
                    selected = if (field.noneLabel != null) value else value.ifBlank { pairs.first().first },
                    options = pairs.map { it.first },
                    optionLabel = { labels[it] ?: it },
                    onSelected = onChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        FieldKind.MULTI -> {
            val selected = value.lines().filter { it.isNotBlank() }.toSet()
            val pairs = if (field.remote != null) choices.map { it.value to it.label } else field.options.map { it to (field.optionLabels[it] ?: it) }
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(field.label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Brand)
                if (pairs.isEmpty()) Text(stringResource(R.string.adm_no_options), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                pairs.forEach { (optionValue, label) ->
                    PanelSwitchRow(label, null, optionValue in selected) { on ->
                        onChange((if (on) selected + optionValue else selected - optionValue).joinToString("\n"))
                    }
                }
            }
        }
        FieldKind.IMAGES -> {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) onImage(uri.toString()) }
            val urls = value.lines().filter { it.isNotBlank() }
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(field.label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Brand)
                urls.forEach { url ->
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        androidx.compose.material3.TextButton(onClick = { onChange((urls - url).joinToString("\n")) }) {
                            Text(stringResource(R.string.adm_remove), color = ArizColors.Error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ArizSecondaryButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth(), enabled = !uploading) {
                    Text(stringResource(if (uploading) R.string.adm_uploading else R.string.adm_add_image), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
                }
            }
        }
        FieldKind.IMAGE -> {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) onImage(uri.toString()) }
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(field.label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Brand)
                if (value.isNotBlank()) {
                    AsyncImage(
                        model = value,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
                ArizSecondaryButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth(), enabled = !uploading) {
                    Text(stringResource(if (uploading) R.string.adm_uploading else R.string.adm_choose_image), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
                }
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            }
        }
        else -> ArizTextField(
            value = value,
            onValueChange = onChange,
            label = field.label,
            modifier = Modifier.fillMaxWidth(),
            errorText = error,
            keyboardType = when (field.kind) {
                FieldKind.NUMBER -> KeyboardType.Decimal
                FieldKind.INT -> KeyboardType.Number
                else -> KeyboardType.Text
            },
            singleLine = field.kind != FieldKind.MULTILINE && field.kind != FieldKind.LIST,
            minLines = if (field.kind == FieldKind.MULTILINE || field.kind == FieldKind.LIST) 3 else 1,
        )
    }
}
