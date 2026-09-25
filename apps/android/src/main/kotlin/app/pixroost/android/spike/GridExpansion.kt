package app.pixroost.android.spike

/** Repeats the gallery until the grid has at least [target] cells, so a small gallery still gives 10 000+. */
fun expandToTarget(items: List<MediaItem>, target: Int): List<GridCell> {
    if (items.isEmpty()) return emptyList()
    val copies = if (target <= items.size) 1 else (target + items.size - 1) / items.size
    return (0 until copies).flatMap { copy -> items.map { GridCell("${it.id}-$copy", it) } }
}
