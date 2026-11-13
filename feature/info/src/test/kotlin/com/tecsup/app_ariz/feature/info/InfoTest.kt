package com.tecsup.app_ariz.feature.info

import com.tecsup.app_ariz.core.common.ArizContact
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.info.data.toBody
import com.tecsup.app_ariz.feature.info.domain.ContactFieldError
import com.tecsup.app_ariz.feature.info.domain.ContactReport
import com.tecsup.app_ariz.feature.info.domain.ContactRepository
import com.tecsup.app_ariz.feature.info.domain.ContactValidator
import com.tecsup.app_ariz.feature.info.ui.ContactAction
import com.tecsup.app_ariz.feature.info.ui.ContactTopic
import com.tecsup.app_ariz.feature.info.ui.ContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContactValidatorTest {

    @Test
    fun `nombre y correo son obligatorios y el correo debe ser valido`() {
        assertEquals(ContactFieldError.REQUIRED, ContactValidator.validateName(" "))
        assertEquals(ContactFieldError.REQUIRED, ContactValidator.validateEmail(""))
        assertEquals(ContactFieldError.INVALID_EMAIL, ContactValidator.validateEmail("ana@correo"))
        assertNull(ContactValidator.validateEmail("ana@correo.com"))
    }

    @Test
    fun `el celular es opcional pero si se escribe debe ser peruano de 9 digitos`() {
        assertNull(ContactValidator.validatePhone(""))
        assertNull(ContactValidator.validatePhone("987654321"))
        assertEquals(ContactFieldError.INVALID_PHONE, ContactValidator.validatePhone("887654321"))
        assertEquals(ContactFieldError.INVALID_PHONE, ContactValidator.validatePhone("9876"))
    }

    @Test
    fun `el mensaje necesita al menos 5 caracteres`() {
        assertEquals(ContactFieldError.REQUIRED, ContactValidator.validateMessage(" "))
        assertEquals(ContactFieldError.MESSAGE_TOO_SHORT, ContactValidator.validateMessage("hola"))
        assertNull(ContactValidator.validateMessage("Hola!!"))
    }

    @Test
    fun `el cuerpo recorta espacios, omite el telefono vacio y limita el asunto a 80`() {
        val body = ContactReport(" Ana ", " ana@correo.com ", " ", "x".repeat(100), " Hola ARIZ ").toBody()

        assertEquals("Ana", body.name)
        assertEquals("ana@correo.com", body.email)
        assertNull(body.phone)
        assertEquals(80, body.topic.length)
        assertEquals("Hola ARIZ", body.message)
    }
}

class ArizContactTest {

    @Test
    fun `el enlace de WhatsApp lleva el numero y el mensaje codificado sin signos mas`() {
        val url = ArizContact.whatsappUrl("Hola ARIZ, quiero cotizar")

        assertEquals("https://wa.me/51958283952?text=Hola%20ARIZ%2C%20quiero%20cotizar", url)
        assertFalse(url.contains("+"))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelTest {

    private class FakeRepo : ContactRepository {
        var sent: ContactReport? = null
        var result: AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun send(report: ContactReport): AppResult<Unit> {
            sent = report
            return result
        }
    }

    private val repo = FakeRepo()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun filled() = ContactViewModel(repo).apply {
        onAction(ContactAction.NameChanged("Ana"))
        onAction(ContactAction.EmailChanged("ana@correo.com"))
        onAction(ContactAction.MessageChanged("Quiero información"))
    }

    @Test
    fun `enviar vacio marca los errores y no llama al backend`() {
        val viewModel = ContactViewModel(repo)

        viewModel.onAction(ContactAction.Submit("Quiero información"))

        val state = viewModel.state.value
        assertEquals(ContactFieldError.REQUIRED, state.nameError)
        assertEquals(ContactFieldError.REQUIRED, state.emailError)
        assertEquals(ContactFieldError.REQUIRED, state.messageError)
        assertNull(repo.sent)
    }

    @Test
    fun `un envio valido manda el tema traducido, confirma y deja el formulario limpio`() {
        val viewModel = filled()
        viewModel.onAction(ContactAction.TopicChanged(ContactTopic.INVEST))
        viewModel.onAction(ContactAction.PhoneChanged("987 654 321"))

        viewModel.onAction(ContactAction.Submit("Invertir con ARIZ"))

        assertEquals("Invertir con ARIZ", repo.sent?.topic)
        assertEquals("987654321", repo.sent?.phone)
        assertTrue(viewModel.state.value.sent)
        assertEquals("", viewModel.state.value.message)
        assertEquals(ContactTopic.INFORMATION, viewModel.state.value.topic)

        viewModel.onAction(ContactAction.SentDismissed)
        assertFalse(viewModel.state.value.sent)
    }

    @Test
    fun `un error del backend se muestra sin confirmar y conserva lo escrito`() {
        repo.result = AppResult.Failure(AppError.Network())
        val viewModel = filled()

        viewModel.onAction(ContactAction.Submit("Quiero información"))

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.sent)
        assertEquals("Ana", viewModel.state.value.name)
    }
}
