package net.bortolan.tradinglog.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun Arrow(color: Color, modifier: Modifier = Modifier, inverse: Boolean = false) {
    val context = LocalContext.current
    AndroidView(
        { ArrowView(context, color) },
        modifier = modifier.then(
            Modifier
                .padding(horizontal = 16.dp)
                .height(8.dp)
                .rotate(if (inverse) 180f else 0f)
        )
    )
}

@SuppressLint("ViewConstructor")
class ArrowView(context: Context, color: Color) : View(context) {
    private val margin = 1.dp.value

    private val paint = Paint().apply {
        this.color = android.graphics.Color.rgb(
            color.red.times(255).toInt(),
            color.green.times(255).toInt(),
            color.blue.times(255).toInt()
        )
        strokeWidth = 3.dp.value
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.SQUARE
    }

    override fun onDraw(canvas: Canvas?) {
        val middle = measuredHeight / 2f
        val stopX = measuredWidth.toFloat() - margin
        canvas?.drawLine(0f + margin, middle, stopX, middle, paint)
        canvas?.drawLine(
            measuredWidth - middle,
            0f + margin,
            stopX,
            middle,
            paint
        )
        canvas?.drawLine(
            stopX - middle,
            measuredHeight.toFloat() - margin,
            stopX,
            middle,
            paint
        )
    }
}