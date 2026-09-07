package com.spinel.pdftools.ui.scandocument

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

data class PointF(val x: Float, val y: Float)

data class Quadrilateral(
    val topLeft: PointF,
    val topRight: PointF,
    val bottomRight: PointF,
    val bottomLeft: PointF
) {
    fun toList(): List<PointF> = listOf(topLeft, topRight, bottomRight, bottomLeft)
}

object CropGeometry {

    fun getOutputDimensions(quad: Quadrilateral): Pair<Float, Float> {
        val widthTop = distance(quad.topLeft, quad.topRight)
        val widthBottom = distance(quad.bottomLeft, quad.bottomRight)
        val heightLeft = distance(quad.topLeft, quad.bottomLeft)
        val heightRight = distance(quad.topRight, quad.bottomRight)

        val outputWidth = max(widthTop, widthBottom)
        val outputHeight = max(heightLeft, heightRight)

        return Pair(outputWidth, outputHeight)
    }

    private fun distance(p1: PointF, p2: PointF): Float {
        return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
    }

    fun isValidQuadrilateral(quad: Quadrilateral): Boolean {
        // Simple check for self-intersection (crossing edges)
        if (doLineSegmentsIntersect(quad.topLeft, quad.bottomRight, quad.topRight, quad.bottomLeft)) {
            // Diagonals MUST intersect in a valid convex quad. 
            // If they DO NOT intersect, it's a concave or self-intersecting polygon.
            // Wait, standard intersection of diagonals is a property of a convex quad.
        }

        val intersectDiagonals = doLineSegmentsIntersect(quad.topLeft, quad.bottomRight, quad.topRight, quad.bottomLeft)
        if (!intersectDiagonals) return false

        // Check for zero area / overlapping points
        val minDistance = 10f
        if (distance(quad.topLeft, quad.topRight) < minDistance) return false
        if (distance(quad.topRight, quad.bottomRight) < minDistance) return false
        if (distance(quad.bottomRight, quad.bottomLeft) < minDistance) return false
        if (distance(quad.bottomLeft, quad.topLeft) < minDistance) return false

        return true
    }

    private fun doLineSegmentsIntersect(p1: PointF, p2: PointF, p3: PointF, p4: PointF): Boolean {
        val d1 = direction(p3, p4, p1)
        val d2 = direction(p3, p4, p2)
        val d3 = direction(p1, p2, p3)
        val d4 = direction(p1, p2, p4)

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true
        }

        if (d1 == 0f && onSegment(p3, p4, p1)) return true
        if (d2 == 0f && onSegment(p3, p4, p2)) return true
        if (d3 == 0f && onSegment(p1, p2, p3)) return true
        if (d4 == 0f && onSegment(p1, p2, p4)) return true

        return false
    }

    private fun direction(a: PointF, b: PointF, c: PointF): Float {
        return (c.x - a.x) * (b.y - a.y) - (b.x - a.x) * (c.y - a.y)
    }

    private fun onSegment(a: PointF, b: PointF, c: PointF): Boolean {
        return c.x >= minOf(a.x, b.x) && c.x <= maxOf(a.x, b.x) &&
               c.y >= minOf(a.y, b.y) && c.y <= maxOf(a.y, b.y)
    }

    fun mapPointToSource(
        viewPoint: PointF,
        viewWidth: Float,
        viewHeight: Float,
        sourceWidth: Float,
        sourceHeight: Float
    ): PointF {
        val viewAspect = viewWidth / viewHeight
        val sourceAspect = sourceWidth / sourceHeight

        var drawWidth = viewWidth
        var drawHeight = viewHeight
        var left = 0f
        var top = 0f

        if (sourceAspect > viewAspect) {
            drawHeight = viewWidth / sourceAspect
            top = (viewHeight - drawHeight) / 2f
        } else {
            drawWidth = viewHeight * sourceAspect
            left = (viewWidth - drawWidth) / 2f
        }

        val sx = ((viewPoint.x - left) / drawWidth) * sourceWidth
        val sy = ((viewPoint.y - top) / drawHeight) * sourceHeight

        // Constrain to source bounds
        val constrainedX = sx.coerceIn(0f, sourceWidth)
        val constrainedY = sy.coerceIn(0f, sourceHeight)

        return PointF(constrainedX, constrainedY)
    }

    fun mapSourceToView(
        sourcePoint: PointF,
        viewWidth: Float,
        viewHeight: Float,
        sourceWidth: Float,
        sourceHeight: Float
    ): PointF {
        val viewAspect = viewWidth / viewHeight
        val sourceAspect = sourceWidth / sourceHeight

        var drawWidth = viewWidth
        var drawHeight = viewHeight
        var left = 0f
        var top = 0f

        if (sourceAspect > viewAspect) {
            drawHeight = viewWidth / sourceAspect
            top = (viewHeight - drawHeight) / 2f
        } else {
            drawWidth = viewHeight * sourceAspect
            left = (viewWidth - drawWidth) / 2f
        }

        val vx = left + (sourcePoint.x / sourceWidth) * drawWidth
        val vy = top + (sourcePoint.y / sourceHeight) * drawHeight

        return PointF(vx, vy)
    }
}
