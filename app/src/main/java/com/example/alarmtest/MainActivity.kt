package com.example.alarmtest

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.alarmtest.ui.theme.AlarmTestTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.LocalTime
import androidx.compose.material3.Switch
import java.time.temporal.Temporal

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
    var showAlarmCard by remember { mutableStateOf(false) }

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


                showAlarmCard = true

                secondsText = ""
                message = ""

            }) {
                Text(text = "Schedule Alarm")
            }
            Button(onClick = {
                alarmItem?.let(scheduler::cancel)
                showAlarmCard = false
            }) {
                Text(text = "Cancel")
            }
            if (showAlarmCard) {
                AlarmCard(alarmItem?.message ?: "", alarmItem?.time ?: LocalTime.now())
            }
        }



    }
}

    @Composable
    fun AlarmCard(nameAlarm:String, time: Temporal) {
        var checked by remember { mutableStateOf(true) }

        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically){

            Column (modifier = Modifier.padding(16.dp)){
                Text(text = "$nameAlarm")
                Text(text = "$time")
            }

            Switch(checked = checked, onCheckedChange = {checked = it})
        }
    }