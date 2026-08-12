import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Test() {
    LazyColumn {
        items(listOf(1, 2, 3), key = { it }) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem())
        }
    }
}
