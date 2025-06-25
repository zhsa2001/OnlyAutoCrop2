package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

/**
 * Экран с результатом
 * @param message сообщение с резульатом
 * @param returnToStart функция возвращения к началу действий
 */
@Composable
fun ResultOfCropping(message: String, returnToStart:()->Unit){
    val returnToStartText = "Вернуться в начало"
    Column {
        Column{
            Button(onClick = returnToStart) {
                Text(returnToStartText)
            }
        }
        Column{
            val state = rememberScrollState()
            Text(message, Modifier.verticalScroll(state))
        }
    }
}