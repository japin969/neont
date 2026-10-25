package com.neont.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.neont.messenger.ui.navigation.NeontNavGraph
import com.neont.messenger.ui.theme.NeontTheme
import com.neont.messenger.ui.theme.TelegramDarkBG
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
