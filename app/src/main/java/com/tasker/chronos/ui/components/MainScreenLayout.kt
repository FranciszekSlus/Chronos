package com.tasker.chronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource // <-- DODAJ TEN IMPORT
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tasker.chronos.R

@Composable
fun MainScreenLayout(
    navController: NavController,
    activeScreen: Screen,
    screenTitle: String,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // --- OSTATECZNA POPRAWKA ---
            // Ustawiamy kolor tła bezpośrednio z pliku colors.xml
            .background(colorResource(id = R.color.zolty_background)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        TopNavigationBar(
            navController = navController,
            activeScreen = activeScreen,
            screenTitle = screenTitle
        )

        Spacer(modifier = Modifier.height(16.dp))

        content()

        Button(
            onClick = { navController.navigate("menu") { popUpTo("menu") { inclusive = false } } },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        ) {
            Text("Powrót", color = Color.Black, fontFamily = FontFamily(Font(R.font.blackops)))
        }
    }
}