package com.tecsup.app_ariz.feature.customer.data

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/** Endpoints del panel de cliente (`USER`); usan la cookie de sesión. */
internal interface CustomerApi {
    @GET("customer-account/me")
    suspend fun profile(): CustomerProfileDto

    @PATCH("customer-account/me")
    suspend fun updateProfile(@Body body: CustomerProfileBody): CustomerProfileDto

    @PATCH("customer-account/me/password")
    suspend fun changePassword(@Body body: CustomerPasswordBody): ResponseBody

    @GET("properties/owner/me")
    suspend fun myProperties(): List<CustomerPropertyDto>

    @GET("customer/membership")
    suspend fun membership(): MembershipDto

    @POST("customer/membership")
    suspend fun requestMembership(@Body body: MembershipRequestBody): ResponseBody
}
