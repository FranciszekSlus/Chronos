package com.tasker.chronos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tasker.chronos.R

@Composable
fun MenuScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2E3440)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Chronos!",
            fontFamily = FontFamily(Font(R.font.greekfreak)),
            fontSize = 32.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("start") },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.moj_niebieski)
            ),
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        ) {
            Text("Start", color = Color.Black, fontFamily = FontFamily(Font(R.font.greekfreak)), fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("calendar") },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.moj_niebieski)
            ),
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        ) {
            Text("Kalendarz", color = Color.Black, fontFamily = FontFamily(Font(R.font.greekfreak)), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("options") },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.moj_niebieski)
            ),
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        ) {
            Text("Opcje", color = Color.Black, fontFamily = FontFamily(Font(R.font.greekfreak)), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("exit") },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.moj_niebieski)
            ),
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        ) {
            Text("Wyjście", color = Color.Black, fontFamily = FontFamily(Font(R.font.greekfreak)), fontSize = 16.sp)
        }

    }
}

