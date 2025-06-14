package com.aivoiceclassifier.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aivoiceclassifier.presentation.dashboard.DashboardScreen
import com.aivoiceclassifier.presentation.interview.InterviewScreen
import com.aivoiceclassifier.presentation.interview.CompanyInterviewScreen
import com.aivoiceclassifier.presentation.login.LoginScreen
import com.aivoiceclassifier.presentation.session.InterviewSessionScreen
import com.aivoiceclassifier.presentation.translator.TranslatorScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    onToggleTheme: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onNavigateToInterview = { companyId ->
                    navController.navigate("company_interview/$companyId")
                },
                onNavigateToTranslator = {
                    navController.navigate("translator")
                },
                onToggleTheme = onToggleTheme
            )
        }
        
        composable("interview") {
            InterviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSession = { companyId, companyName ->
                    navController.navigate("session/$companyId/$companyName")
                }
            )
        }
        
        composable("company_interview/{companyId}") { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
            // For demo purposes, we'll use the company ID as the name
            val companyName = when (companyId) {
                "google" -> "Google"
                "microsoft" -> "Microsoft"
                else -> "Company"
            }
            
            CompanyInterviewScreen(
                companyId = companyId,
                companyName = companyName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("translator") {
            TranslatorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("session/{companyId}/{companyName}") { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId")?.toLongOrNull() ?: 0L
            val companyName = backStackEntry.arguments?.getString("companyName") ?: ""
            
            InterviewSessionScreen(
                companyId = companyId,
                companyName = companyName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 