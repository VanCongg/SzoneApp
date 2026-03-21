package com.app.szone.domain.repository

interface WelcomeRepository {
    fun saveOnBoardingState(completed: Boolean)
    fun getOnBoardingState(): Boolean
}
