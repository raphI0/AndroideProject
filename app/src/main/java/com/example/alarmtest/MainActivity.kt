package com.example.alarmtest

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.alarmtest.ui.theme.AlarmTestTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scheduler = AndroidAlarmSystem(this)
        setContent {
            AlarmTestTheme {
                Greeting(scheduler)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(scheduler : AndroidAlarmSystem, modifier: Modifier = Modifier) {

    var alarmItem: AlarmItem? = null

    var secondsText by remember {
        mutableStateOf("")
    }
    var message by remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = secondsText,
            onValueChange = { secondsText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Trigger alarm in secs")
            }
        )
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Message")
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                alarmItem = AlarmItem(
                    time = LocalDateTime.now()
                        .plusSeconds(secondsText.toLong()),
                    message = message
                )
                alarmItem?.let(scheduler::schedule)
                secondsText = ""
                message = ""

            }) {
                Text(text = "Schedule Alarm")
            }
            Button(onClick = {
                alarmItem?.let(scheduler::cancel)
            }) {
                Text(text = "Cancel")
            }
        }


    }
}

