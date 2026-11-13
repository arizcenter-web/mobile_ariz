package com.tecsup.app_ariz.core.network

import com.tecsup.app_ariz.core.network.di.JsonMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonMediaTypeTest {

    @Test
    fun `el charset va como UTF-8 con guion porque el body-parser de Express rechaza UTF8`() {
        val header = JsonMediaType.toString()

        assertTrue(header.startsWith("application/json"))
        assertTrue(header.contains("charset=UTF-8"))
        assertFalse(header.contains("UTF8"))
        assertEquals("json", JsonMediaType.subtype)
    }
}
