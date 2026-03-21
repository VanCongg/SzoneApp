package com.app.szone.data.mapping

import com.app.szone.data.local.entity.WarehouseEntity
import com.app.szone.data.model.WarehouseResponse
import com.app.szone.domain.model.WarehouseModel

fun WarehouseResponse.toEntity(): WarehouseEntity {
    return WarehouseEntity(id = id, name = name, address = address)
}

fun WarehouseResponse.toDomain(): WarehouseModel {
    return WarehouseModel(id = id, name = name, address = address)
}

fun WarehouseEntity.toDomain(): WarehouseModel {
    return WarehouseModel(id = id, name = name, address = address)
}

