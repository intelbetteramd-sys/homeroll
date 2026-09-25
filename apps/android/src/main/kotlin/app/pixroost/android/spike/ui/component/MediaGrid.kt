package app.pixroost.android.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.ui.model.GridCell
import app.pixroost.android.spike.ui.model.GridOptions
import coil3.ImageLoader

@Composable
fun MediaGrid(
    cells: List<GridCell>,
    options: GridOptions,
    imageLoader: ImageLoader,
    state: LazyGridState,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(UiConstants.CELL_MIN_SIZE),
        state = state,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(UiConstants.CELL_SPACING),
        verticalArrangement = Arrangement.spacedBy(UiConstants.CELL_SPACING),
    ) {
        items(cells, key = { it.key }, contentType = { UiConstants.CELL_CONTENT_TYPE }) { cell ->
            MediaCell(cell.item, options, imageLoader)
        }
    }
}
