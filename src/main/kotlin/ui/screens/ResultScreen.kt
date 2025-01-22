package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier


@Composable
fun ResultOfCropping(message: String, returnToStart:()->Unit){
    Column {
        Column{
            Button(onClick = {
                returnToStart()
            }
            ) {
                Text("Вернуться в начало")
            }
        }
        Column{
            val state = rememberScrollState()
            LaunchedEffect(Unit) { state.animateScrollTo(100) }
            Text(message, Modifier.verticalScroll(state))
        }
    }
}