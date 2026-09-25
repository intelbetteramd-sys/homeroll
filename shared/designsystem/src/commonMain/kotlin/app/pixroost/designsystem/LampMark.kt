package app.pixroost.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The "light in the window" mark: a lit window with a dark outline.
 * Drawn on a 16-unit grid, the same as the mockups (docs/design/README.md).
 */
@Composable
fun LampMark(outline: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension / GRID
        val stroke = STROKE * unit
        val topLeft = Offset(INSET * unit, INSET * unit)
        val square = Size((GRID - 2 * INSET) * unit, (GRID - 2 * INSET) * unit)
        val corner = CornerRadius(CORNER * unit)
        drawRoundRect(color = LampColor, topLeft = topLeft, size = square, cornerRadius = corner)
        drawRoundRect(color = outline, topLeft = topLeft, size = square, cornerRadius = corner, style = Stroke(stroke))

        val center = GRID / 2 * unit
        val crossStart = CROSS_INSET * unit
        val crossEnd = (GRID - CROSS_INSET) * unit
        drawLine(outline, Offset(center, crossStart), Offset(center, crossEnd), stroke)
        drawLine(outline, Offset(crossStart, center), Offset(crossEnd, center), stroke)
    }
}

internal val LampColor = Color(0xFFF2A93B)

// Sizes in units of the 16-unit grid.
private const val GRID = 16f
private const val INSET = 1f
private const val CORNER = 4f
private const val STROKE = 1.5f

// The window cross ends just inside the outline, so its ends hide under the stroke.
private const val CROSS_INSET = 1.8f
