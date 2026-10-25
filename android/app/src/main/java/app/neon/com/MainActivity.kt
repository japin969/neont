package app.neon.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.neon.com.ui.navigation.NeontNavGraph
import app.neon.com.ui.theme.NeontTheme
import app.neon.com.ui.theme.TelegramDarkBG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NeontTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TelegramDarkBG
                ) {
                    NeontNavGraph()
                }
            }
        }
    }
}
