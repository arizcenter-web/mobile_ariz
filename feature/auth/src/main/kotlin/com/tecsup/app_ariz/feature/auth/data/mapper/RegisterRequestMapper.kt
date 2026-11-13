package com.tecsup.app_ariz.feature.auth.data.mapper

import com.tecsup.app_ariz.feature.auth.data.remote.dto.RegisterCustomerRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.RegisterProfessionalRequestDto
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterCustomerData
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData

// `acceptTerms = true`: la UI no permite enviar el formulario sin aceptar los términos.

fun RegisterCustomerData.toRequest() = RegisterCustomerRequestDto(
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    password = password,
    acceptTerms = true,
)

fun RegisterProfessionalData.toRequest() = RegisterProfessionalRequestDto(
    type = type.name,
    firstName = firstName,
    lastName = lastName,
    dni = dni,
    businessName = businessName,
    ruc = ruc,
    email = email,
    phone = phone,
    whatsapp = whatsapp,
    city = city,
    districts = districts,
    address = address,
    website = website,
    socialLinks = socialLinks,
    password = password,
    acceptTerms = true,
)
