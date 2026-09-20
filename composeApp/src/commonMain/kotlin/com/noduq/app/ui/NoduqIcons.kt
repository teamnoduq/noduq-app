package com.noduq.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Stroke icons in the Lucide / Feather 24×24, 2px round-cap language. */
object NoduqIcons {
    val Receipt: ImageVector by lazy {
        strokeIcon("noduq.receipt") {
            moveTo(4f, 2f)
            lineTo(4f, 22f)
            lineTo(6f, 21f)
            lineTo(8f, 22f)
            lineTo(10f, 21f)
            lineTo(12f, 22f)
            lineTo(14f, 21f)
            lineTo(16f, 22f)
            lineTo(18f, 21f)
            lineTo(20f, 22f)
            lineTo(20f, 2f)
            lineTo(18f, 3f)
            lineTo(16f, 2f)
            lineTo(14f, 3f)
            lineTo(12f, 2f)
            lineTo(10f, 3f)
            lineTo(8f, 2f)
            lineTo(6f, 3f)
            close()
            moveTo(16f, 8f)
            horizontalLineTo(8f)
            moveTo(16f, 12f)
            horizontalLineTo(8f)
            moveTo(12f, 16f)
            horizontalLineTo(8f)
        }
    }

    val People: ImageVector by lazy {
        strokeIcon("noduq.people") {
            moveTo(16f, 21f)
            verticalLineTo(19f)
            arcToRelative(4f, 4f, 0f, false, false, -4f, -4f)
            horizontalLineTo(6f)
            arcToRelative(4f, 4f, 0f, false, false, -4f, 4f)
            verticalLineTo(21f)
            moveTo(13f, 7f)
            arcToRelative(4f, 4f, 0f, true, true, -8f, 0f)
            arcToRelative(4f, 4f, 0f, true, true, 8f, 0f)
            moveTo(22f, 21f)
            verticalLineTo(19f)
            arcToRelative(4f, 4f, 0f, false, false, -3f, -3.87f)
            moveTo(16f, 3.13f)
            arcToRelative(4f, 4f, 0f, false, true, 0f, 7.75f)
        }
    }

    val Profile: ImageVector by lazy {
        strokeIcon("noduq.profile") {
            moveTo(17f, 8f)
            arcToRelative(5f, 5f, 0f, true, true, -10f, 0f)
            arcToRelative(5f, 5f, 0f, true, true, 10f, 0f)
            moveTo(20f, 21f)
            arcToRelative(8f, 8f, 0f, false, false, -16f, 0f)
        }
    }

    val Search: ImageVector by lazy {
        strokeIcon("noduq.search") {
            moveTo(19f, 11f)
            arcToRelative(8f, 8f, 0f, true, true, -16f, 0f)
            arcToRelative(8f, 8f, 0f, true, true, 16f, 0f)
            moveTo(21f, 21f)
            lineTo(16.65f, 16.65f)
        }
    }

    val Sliders: ImageVector by lazy {
        strokeIcon("noduq.sliders") {
            moveTo(4f, 21f)
            verticalLineTo(14f)
            moveTo(4f, 10f)
            verticalLineTo(3f)
            moveTo(12f, 21f)
            verticalLineTo(12f)
            moveTo(12f, 8f)
            verticalLineTo(3f)
            moveTo(20f, 21f)
            verticalLineTo(16f)
            moveTo(20f, 12f)
            verticalLineTo(3f)
            moveTo(2f, 14f)
            horizontalLineTo(6f)
            moveTo(10f, 8f)
            horizontalLineTo(14f)
            moveTo(18f, 16f)
            horizontalLineTo(22f)
        }
    }

    val Check: ImageVector by lazy {
        strokeIcon("noduq.check") {
            moveTo(20f, 6f)
            lineTo(9f, 17f)
            lineTo(4f, 12f)
        }
    }

    val BellOff: ImageVector by lazy {
        strokeIcon("noduq.bell-off") {
            moveTo(13.73f, 21f)
            arcToRelative(2f, 2f, 0f, false, true, -3.46f, 0f)
            moveTo(18.63f, 13f)
            curveTo(18.87f, 12.4f, 19f, 11.74f, 19f, 11f)
            curveTo(19f, 7.13f, 16.87f, 4f, 13f, 4f)
            curveTo(12.3f, 4f, 11.64f, 4.12f, 11.03f, 4.34f)
            moveTo(8.67f, 3.01f)
            curveTo(7.07f, 3.7f, 5.78f, 5.06f, 5.23f, 6.77f)
            moveTo(6.26f, 6.26f)
            curveTo(6.09f, 6.8f, 6f, 7.39f, 6f, 8f)
            curveTo(6f, 11.09f, 4.72f, 12.78f, 3.61f, 14.11f)
            curveTo(3.16f, 14.66f, 3f, 15.33f, 3f, 16f)
            curveTo(3f, 16.55f, 3.45f, 17f, 4f, 17f)
            horizontalLineTo(15.73f)
            moveTo(2f, 2f)
            lineTo(22f, 22f)
        }
    }

    val Pencil: ImageVector by lazy {
        strokeIcon("noduq.pencil") {
            moveTo(12f, 20f)
            horizontalLineTo(21f)
            moveTo(16.5f, 3.5f)
            lineTo(20.5f, 7.5f)
            lineTo(8f, 20f)
            lineTo(4f, 21f)
            lineTo(5f, 17f)
            close()
        }
    }

    val Key: ImageVector by lazy {
        strokeIcon("noduq.key") {
            moveTo(12.5f, 8.5f)
            lineTo(21f, 17f)
            moveTo(16f, 12f)
            lineTo(18f, 14f)
            moveTo(12f, 15.5f)
            arcToRelative(4.5f, 4.5f, 0f, true, true, -9f, 0f)
            arcToRelative(4.5f, 4.5f, 0f, true, true, 9f, 0f)
        }
    }

    val Pause: ImageVector by lazy {
        strokeIcon("noduq.pause") {
            moveTo(8f, 5f)
            verticalLineTo(19f)
            moveTo(16f, 5f)
            verticalLineTo(19f)
        }
    }

    val Play: ImageVector by lazy {
        strokeIcon("noduq.play") {
            moveTo(7f, 5f)
            lineTo(19f, 12f)
            lineTo(7f, 19f)
            close()
        }
    }

    val Copy: ImageVector by lazy {
        strokeIcon("noduq.copy") {
            moveTo(8f, 8f)
            horizontalLineTo(20f)
            verticalLineTo(22f)
            horizontalLineTo(8f)
            close()
            moveTo(16f, 8f)
            verticalLineTo(4f)
            horizontalLineTo(4f)
            verticalLineTo(16f)
            horizontalLineTo(8f)
        }
    }

    val Trash: ImageVector by lazy {
        strokeIcon("noduq.trash") {
            moveTo(3f, 6f)
            horizontalLineTo(21f)
            moveTo(8f, 6f)
            verticalLineTo(4f)
            horizontalLineTo(16f)
            verticalLineTo(6f)
            moveTo(19f, 6f)
            verticalLineTo(20f)
            horizontalLineTo(5f)
            verticalLineTo(6f)
            moveTo(10f, 11f)
            verticalLineTo(17f)
            moveTo(14f, 11f)
            verticalLineTo(17f)
        }
    }

    val Mail: ImageVector by lazy {
        strokeIcon("noduq.mail") {
            moveTo(20f, 4f)
            horizontalLineTo(4f)
            arcToRelative(2f, 2f, 0f, false, false, -2f, 2f)
            verticalLineTo(18f)
            arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
            horizontalLineTo(20f)
            arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
            verticalLineTo(6f)
            arcToRelative(2f, 2f, 0f, false, false, -2f, -2f)
            close()
            moveTo(22f, 7f)
            lineTo(13.03f, 12.7f)
            arcToRelative(1.94f, 1.94f, 0f, false, true, -2.06f, 0f)
            lineTo(2f, 7f)
        }
    }
}

private fun strokeIcon(
    name: String,
    block: PathBuilder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ) {
        block()
    }
}.build()
