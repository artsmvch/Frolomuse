package com.frolo.muse.glide

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.frolo.customdrawable.squircle.createFullSquirclePath
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*
import kotlin.math.min


class SquircleTransformation(
    private val curvature: Double
) : BitmapTransformation() {

    override fun hashCode(): Int {
        return Objects.hash(ID, curvature)
    }

    override fun equals(other: Any?): Boolean {
        return other is SquircleTransformation && this.curvature == other.curvature
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)

        val curvatureData = ByteBuffer.allocate(8).putDouble(curvature).array()
        messageDigest.update(curvatureData)
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val outBitmap = pool.get(outWidth, outHeight, toTransform.config ?: Bitmap.Config.ARGB_8888)

        val canvas = Canvas(outBitmap)
        val src = Rect(0, 0, toTransform.width, toTransform.width)
        val dst = Rect(0, 0, outWidth, outHeight)
        canvas.drawBitmap(toTransform, src, dst, null)

        val radius = min(outWidth, outHeight) / 2
        val path = createFullSquirclePath(radius, curvature).apply {
            fillType = Path.FillType.INVERSE_WINDING
        }
        val matrix = Matrix()
        matrix.setTranslate(outWidth / 2f - radius, outHeight / 2f - radius)
        path.transform(matrix)

        val paint = Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        canvas.drawPath(path, paint)

        return outBitmap
    }

    companion object {
        private const val ID = "com.frolo.muse.glide.SquircleTransformation"
        private val ID_BYTES = ID.toByteArray(CHARSET)
    }

}