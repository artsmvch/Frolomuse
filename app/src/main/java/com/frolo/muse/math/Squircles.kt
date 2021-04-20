package com.frolo.muse.math

import android.graphics.Path
import androidx.collection.LruCache
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

const val MIN_SQUIRCLE_CURVATURE = 2.0
const val DEFAULT_SQUIRCLE_CURVATURE = 3.2

/**
 * Cache for full squircle paths as building a new squircle path is too expensive.
 */
private val fullSquirclePathLruCache: LruCache<FullSquircleParams, Path> = FullSquirclePathLruCache(10)

private data class FullSquircleParams(
    val radius: Int,
    val curvature: Double
)

private class FullSquirclePathLruCache(size: Int) : LruCache<FullSquircleParams, Path>(size) {
    override fun create(key: FullSquircleParams): Path {
        return createFullSquirclePath(key.radius, key.curvature)
    }
}

internal fun validateSquircleCurvature(curvature: Double) {
    if (curvature < MIN_SQUIRCLE_CURVATURE) {
        throw IllegalArgumentException("Curvature must be >= $MIN_SQUIRCLE_CURVATURE: $curvature")
    }
}

/**
 * Calculates the root of [this] number of the given [degree].
 */
internal fun Double.root(degree: Double): Double = this.pow(1.0 / degree)

/**
 * Calculates the root of [this] number of the given [degree].
 */
internal fun Float.root(degree: Float): Float = this.pow(1f / degree)

/**
 * Creates a full path for the squircle shape.
 */
fun createFullSquirclePath(radius: Int, curvature: Double): Path {
    val path = Path()

    if (radius <= 0) {
        return path
    }

    val poweredRadius = radius.toDouble().pow(curvature)

    path.moveTo(-radius.toFloat(), 0f)
    for (x in -radius..radius) {
        val poweredY = poweredRadius - abs(x.toDouble()).pow(curvature)
        val y = poweredY.sign * abs(poweredY).root(curvature)
        path.lineTo(x.toFloat(), y.toFloat())
    }
    for (x in radius downTo -radius) {
        val poweredY = poweredRadius - abs(x.toDouble()).pow(curvature)
        val y = -poweredY.sign * abs(poweredY).root(curvature)
        path.lineTo(x.toFloat(), y.toFloat())
    }
    path.close()

    // Since the shape is drawn centered at [0, 0], we use this offset
    // to adjust the top-left corner of the shape to [0, 0]
    path.offset(radius.toFloat(), radius.toFloat())

    return path
}

/**
 * Builds the full squircle path with [radius] and [curvature] params. The built path will be set to [dstPath].
 * NOTE: this is the preferred way to build the path as it first looks for the same path in the cache.
 */
fun buildFullSquirclePath(dstPath: Path, radius: Int, curvature: Double) {
    val key = FullSquircleParams(radius, curvature)
    val cachedPath = fullSquirclePathLruCache.get(key)
            ?: createFullSquirclePath(radius, curvature)
    dstPath.set(cachedPath)
}