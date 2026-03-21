package com.app.szone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.szone.data.local.dao.OrderDao
import com.app.szone.data.local.dao.PendingDeliveryActionDao
import com.app.szone.data.local.dao.UserDao
import com.app.szone.data.local.dao.WarehouseDao
import com.app.szone.data.local.entity.OrderEntity
import com.app.szone.data.local.entity.PendingDeliveryActionEntity
import com.app.szone.data.local.entity.UserEntity
import com.app.szone.data.local.entity.WarehouseEntity

@Database(
    entities = [UserEntity::class, WarehouseEntity::class, OrderEntity::class, PendingDeliveryActionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun orderDao(): OrderDao
    abstract fun pendingDeliveryActionDao(): PendingDeliveryActionDao
}
