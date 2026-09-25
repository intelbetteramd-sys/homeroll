package app.pixroost.android.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        columns = GridCells.Adaptive(SpikeConstants.CELL_MIN_SIZE),
        state = state,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpikeConstants.CELL_SPACING),
        verticalArrangement = Arrangement.spacedBy(SpikeConstants.CELL_SPACING),
    ) {
        items(cells, key = { it.key }, contentType = { SpikeConstants.CELL_CONTENT_TYPE }) { cell ->
            MediaCell(cell.item, options, imageLoader)
        }
    }
}
