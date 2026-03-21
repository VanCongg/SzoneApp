package com.app.szone.presentation.screen.home.common


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun NavBottomBar(navController: NavController) {
//    val items = remember {
//        listOf(
//            NavBottomBarItem.Voca,
//            NavBottomBarItem.Novel,
//            NavBottomBarItem.News,
//            NavBottomBarItem.Toeic,
//            NavBottomBarItem.Chat
//        )
//    }
//
//    val currentDestination = navController
//        .currentBackStackEntryAsState().value?.destination
//
//    NavigationBar {
//        items.forEach { item ->
//            val selected = currentDestination?.route == item.route::class.qualifiedName
//
//            val composition by rememberLottieComposition(
//                LottieCompositionSpec.RawRes(item.icon)
//            )
//            val progress = remember { Animatable(0f) }
//
//            // Play animation when selected
//            LaunchedEffect(selected) {
//                if (selected) {
//                    progress.snapTo(0f)
//                    progress.animateTo(
//                        targetValue = 1f,
//                        animationSpec = tween(
//                            durationMillis = 1500
//                        )
//                    )
//                    progress.snapTo(0f)
//                } else {
//                    progress.snapTo(0f)
//                }
//            }
//
//            NavigationBarItem(
//                selected = selected,
//                onClick = {
//                    navController.navigate(item.route) {
//                        popUpTo(navController.graph.startDestinationId) { saveState = true }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                },
//                icon = {
//                    LottieAnimation(
//                        composition = composition,
//                        progress = { progress.value },
//                        modifier = Modifier.size(32.dp),
//                        enableMergePaths = true
//                    )
//                },
//                label = {
//                    Text(text = stringResource(id = item.title))
//                }
//            )
//        }
//    }
}
