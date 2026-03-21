package com.app.szone.domain.usecase

import com.app.szone.domain.repository.WelcomeRepository
import com.app.szone.presentation.navigation.NavScreen

class GetStartScreenUseCase(
    private val welcomeRepository: WelcomeRepository,
) {
    operator fun invoke(): NavScreen {
        return NavScreen.AuthNavScreen
    }

}
