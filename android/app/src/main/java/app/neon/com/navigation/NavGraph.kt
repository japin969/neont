// Neont Messenger - NavGraph.kt (Forwarder to ui.navigation.NeontNavGraph)
package app.neon.com.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.neon.com.ui.navigation.NeontNavGraph as UiNavGraph

@Composable
fun NeontNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: NeontAppViewModel = hiltViewModel()
) {
    UiNavGraph(
        modifier = modifier,
        navController = navController,
        viewModel = viewModel
    )
}
