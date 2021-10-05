package com.frolo.customdrawable.squircle

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.util.LruCache
import kotlin.concurrent.getOrSet
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

const val MIN_SQUIRCLE_CURVATURE = 1.0
const val DEFAULT_SQUIRCLE_CURVATURE = 4.0

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
 * Calculates the step for moving along the X-axis when creating a squircle path.
 */
private fun calculateXStep(radius: Int): Double {
    // If the radius is small, then we need to use a smaller step for better smoothing
    val smallRadiusThreshold = 80
    return (radius.toDouble() / smallRadiusThreshold).coerceIn(0.2, 1.0)
}

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

    // Constructing the bottom part, moving on the X-axis from -radius to +radius
    var x: Double = -radius.toDouble()
    var isLastPoint: Boolean = false
    while (true) {
        val poweredY = poweredRadius - abs(x).pow(curvature)
        val y = poweredY.sign * abs(poweredY).root(curvature)
        path.lineTo(x.toFloat(), y.toFloat())
        if (isLastPoint) {
            break
        }
        x += calculateXStep(radius)
        if (x >= radius) {
            isLastPoint = true
            x = radius.toDouble()
        }
    }

    // Constructing the top part, moving on the X-axis from +radius to -radius
    x = radius.toDouble()
    isLastPoint = false
    while (true) {
        val poweredY = poweredRadius - abs(x).pow(curvature)
        val y = -poweredY.sign * abs(poweredY).root(curvature)
        path.lineTo(x.toFloat(), y.toFloat())
        if (isLastPoint) {
            break
        }
        x -= calculateXStep(radius)
        if (x <= -radius) {
            isLastPoint = true
            x = -radius.toDouble()
        }
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
internal fun buildFullSquirclePath(dstPath: Path, radius: Int, curvature: Double) {
    val key = FullSquircleParams(radius, curvature)
    val cachedPath = fullSquirclePathLruCache.get(key)
            ?: createFullSquirclePath(radius, curvature)
    dstPath.set(cachedPath)
}


private val tmpMatrix = ThreadLocal<Matrix>()

/**
 * Returns a matrix for temporary computations. This instance must not be stored anywhere.
 */
private fun getTmpMatrix(): Matrix {
    return tmpMatrix.getOrSet { Matrix() }
}

/**
 * Builds a squircle path on [path] and centers it according to the given [bounds].
 * If the bounds are empty, then the resulting path will be empty.
 * [curvature] defines the curvature of the squircle shape.
 * NOTE: must be called on the main thread.
 */
internal fun buildPathCentered(path: Path, curvature: Double, bounds: Rect?) {
    if (bounds == null || bounds.isEmpty) {
        path.reset()
        return
    }

    val radius = min(bounds.width(), bounds.height()) / 2
    buildFullSquirclePath(path, radius, curvature)

    // Placing the shape in the center of the bounds
    val leftOffset = bounds.width() / 2f - radius
    val topOffset = bounds.height() / 2f - radius
    val matrix = getTmpMatrix()
    matrix.setTranslate(bounds.left.toFloat() + leftOffset, bounds.top.toFloat() + topOffset)
    path.transform(matrix)
}