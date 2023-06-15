package com.unpas.elektronik.ui.periferal

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PeriferalApi {
    @POST("periferal")
    suspend fun addPeriferal(@Body periferalData: PeriferalData): Response<ResponseBody>
}