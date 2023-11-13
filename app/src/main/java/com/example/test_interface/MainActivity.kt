package com.example.test_interface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test_interface.ui.theme.Test_interfaceTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test_interfaceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, onContinueClicked: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenue sur notre pagge!")
        /*Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = { /* Handle button click here */ }
        ) {
            Text("Continue")
        }*/
    }
}


@Composable
fun MyApp(modifier: Modifier = Modifier) {
    var shouldShowOnboarding by remember { mutableStateOf(true) }

    Surface(modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen {}
        } else {
            Greetings()
        }
    }
}

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    names: List<String> = List(1000) { "$it" }
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
            Greeting(name = name)
        }
    }
}

private fun ColumnScope.run(function: ColumnScope.(Any?) -> Unit) {
    TODO("Not yet implemented")
}

@Composable
fun Greeting(name: String) {
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier.weight(1f).padding(bottom = if (expanded1) 48.dp else 0.dp)) {
                Text(text = "Hello, ")
                Text(text = name)
            }

            Column {
                ElevatedButton(
                    onClick = { expanded1 = !expanded1 },
                ) {
                    Text(if (expanded1) "Show less" else "Show more")
                }

                ElevatedButton(
                    onClick = { expanded2 = !expanded2 },
                ) {
                    Text(if (expanded2) "Button 2 less" else "Button 2 more")
                }
            }
        }
    }
    // Appel du deuxième composant
    SecondComponent(expanded2)
}

@Composable
fun SecondComponent(expanded: Boolean) {
    if (expanded) {
        // Affichez ici le contenu de votre deuxième composant lorsque expanded est vrai
        Text(text = "Second Component Content")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Test_interfaceTheme {
        Greeting("Zirael")
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    Test_interfaceTheme{
        OnboardingScreen {}
    }
}

@Preview
@Composable
fun MyAppPreview() {
    Test_interfaceTheme {
        MyApp(Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun DefaultPreview() {
    Test_interfaceTheme {
        Greetings()
    }
}
