package com.app.szone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.szone.data.local.entity.WarehouseEntity

@Dao
interface WarehouseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWarehouse(warehouse: WarehouseEntity)

    @Query("SELECT * FROM warehouse LIMIT 1")
    suspend fun getWarehouse(): WarehouseEntity?

    @Query("DELETE FROM warehouse")
    suspend fun clearWarehouse()
}

