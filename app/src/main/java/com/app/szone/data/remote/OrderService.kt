package com.app.szone.data.remote

import com.app.szone.data.model.ApiResponse
import com.app.szone.data.model.ArrivedWarehouseRequest
import com.app.szone.data.model.OrderEnvelopeDto
import com.app.szone.data.model.ShipperInfoDto
import com.app.szone.data.model.SuccessRequest
import com.app.szone.data.model.WarehouseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderService {
    @POST("api/v1/orders/{orderId}/shipper")
    suspend fun getOrderToShipper(
        @Path("orderId") orderId: String,
        @Body shipperInfo: ShipperInfoDto
    ): ApiResponse<OrderEnvelopeDto>

    @GET("api/v1/scanners/{scannerId}/warehouse")
    suspend fun getWarehouseInfo(
        @Path("scannerId") scannerId: String
    ): ApiResponse<WarehouseResponse>

    @POST("api/v1/orders/{orderId}/arrived-warehouse")
    suspend fun arrivedWarehouse(
        @Path("orderId") orderId: String,
        @Body body: ArrivedWarehouseRequest
    ): ApiResponse<Unit>

    @POST("api/v1/orders/{orderId}/delivery-success")
    suspend fun deliverySuccess(
        @Path("orderId") orderId: String,
        @Body body: SuccessRequest
    ): ApiResponse<Unit>

    @POST("api/v1/orders/{orderId}/delivery-fail")
    suspend fun deliveryFail(
        @Path("orderId") orderId: String
    ): ApiResponse<Unit>
}
