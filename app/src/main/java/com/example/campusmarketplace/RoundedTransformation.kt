package com.example.campusmarketplace

import android.graphics.Bitmap
import com.squareup.picasso.Transformation

class RoundedTransformation : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, source.config)

        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint()
        val shader = android.graphics.BitmapShader(
            squaredBitmap,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.isAntiAlias = true

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        return "rounded"
    }
}

