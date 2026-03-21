//package com.app.szone.data.local
//
//import com.app.szone.data.local.dao.LoggedInUserDao
//import com.app.szone.data.local.entity.LoggedInUser
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//
//class UserLocalDataSourceImpl(
//    private val userDao: LoggedInUserDao,
//) : UserLocalDataSource {
//    override suspend fun saveUser(user: UserDto): Result<Unit> {
//        return try {
//            val userData = LoggedInUser(
//                _id = user._id,
//                email = user.email,
//                username = user.username,
//                firstName = user.firstName,
//                lastName = user.lastName,
//                phoneNumber = user.phoneNumber,
//                address = user.address,
//                dob = user.dob,
//                avatar = user.avatar,
//                roles = user.roles.joinToString(","),
//                status = user.status,
//                require2FA = user.require2FA,
//                createdAt = user.createdAt,
//                updatedAt = user.updatedAt,
//            )
//            userDao.insert(userData)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override fun getUser(): Flow<UserDto?> {
//        return userDao.getUser().map { userData ->
//            userData?.let {
//                UserDto(
//                    _id = it._id,
//                    email = it.email,
//                    username = it.username,
//                    firstName = it.firstName,
//                    lastName = it.lastName,
//                    phoneNumber = it.phoneNumber,
//                    address = it.address,
//                    dob = it.dob,
//                    avatar = it.avatar,
//                    roles = it.roles.split(",").map { role -> RoleDto(role) },
//                    status = it.status,
//                    require2FA = it.require2FA,
//                    createdAt = it.createdAt,
//                    updatedAt = it.updatedAt,
//                )
//            }
//        }
//    }
//
//    override suspend fun clear() {
//        userDao.clear()
//    }
//
//}
