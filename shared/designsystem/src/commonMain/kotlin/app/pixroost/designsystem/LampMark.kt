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
        val unit = size.minDimension / 16f
        val stroke = 1.5f * unit
        val topLeft = Offset(unit, unit)
        val square = Size(14f * unit, 14f * unit)
        val corner = CornerRadius(4f * unit)
        drawRoundRect(color = LampColor, topLeft = topLeft, size = square, cornerRadius = corner)
        drawRoundRect(color = outline, topLeft = topLeft, size = square, cornerRadius = corner, style = Stroke(stroke))
        drawLine(outline, Offset(8f * unit, 1.8f * unit), Offset(8f * unit, 14.2f * unit), stroke)
        drawLine(outline, Offset(1.8f * unit, 8f * unit), Offset(14.2f * unit, 8f * unit), stroke)
    }
}

internal val LampColor = Color(0xFFF2A93B)
