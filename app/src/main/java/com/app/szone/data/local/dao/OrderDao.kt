package com.app.szone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.szone.data.local.entity.OrderEntity
import com.app.szone.data.local.entity.PendingDeliveryActionEntity

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("UPDATE orders SET localStatus = :status WHERE id = :orderId")
    suspend fun updateLocalStatus(orderId: String, status: String)
}

@Dao
interface PendingDeliveryActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: PendingDeliveryActionEntity)

    @Query("SELECT * FROM pending_delivery_actions ORDER BY createdAt ASC")
    suspend fun getPendingActions(): List<PendingDeliveryActionEntity>

    @Query("DELETE FROM pending_delivery_actions WHERE id = :id")
    suspend fun deleteAction(id: Long)
}

