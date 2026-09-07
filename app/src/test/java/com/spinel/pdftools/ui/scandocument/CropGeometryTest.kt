package com.spinel.pdftools.ui.scandocument

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class CropGeometryTest {

    @Test
    fun getOutputDimensions_calculatesCorrectly() {
        val quad = Quadrilateral(
            topLeft = PointF(0f, 0f),
            topRight = PointF(100f, 0f),
            bottomRight = PointF(100f, 200f),
            bottomLeft = PointF(0f, 200f)
        )
        val (width, height) = CropGeometry.getOutputDimensions(quad)
        assertEquals(100f, width, 0.01f)
        assertEquals(200f, height, 0.01f)
    }

    @Test
    fun getOutputDimensions_takesMaxEdges() {
        val quad = Quadrilateral(
            topLeft = PointF(0f, 0f),
            topRight = PointF(100f, 0f),
            bottomRight = PointF(120f, 200f),
            bottomLeft = PointF(0f, 200f)
        )
        val (width, height) = CropGeometry.getOutputDimensions(quad)
        assertEquals(120f, width, 0.01f)
        assertTrue(height > 200f)
    }

    @Test
    fun isValidQuadrilateral_validQuadReturnsTrue() {
        val quad = Quadrilateral(
            topLeft = PointF(10f, 10f),
            topRight = PointF(90f, 10f),
            bottomRight = PointF(90f, 90f),
            bottomLeft = PointF(10f, 90f)
        )
        assertTrue(CropGeometry.isValidQuadrilateral(quad))
    }

    @Test
    fun isValidQuadrilateral_intersectingEdgesReturnsFalse() {
        val quad = Quadrilateral(
            topLeft = PointF(10f, 10f),
            topRight = PointF(90f, 90f),
            bottomRight = PointF(90f, 10f),
            bottomLeft = PointF(10f, 90f)
        )
        assertFalse(CropGeometry.isValidQuadrilateral(quad))
    }

    @Test
    fun mapPointToSource_and_mapSourceToView() {
        val viewWidth = 1000f
        val viewHeight = 2000f
        val sourceWidth = 500f
        val sourceHeight = 500f
        
        val sourcePoint = PointF(250f, 250f)
        val viewPoint = CropGeometry.mapSourceToView(sourcePoint, viewWidth, viewHeight, sourceWidth, sourceHeight)
        
        assertEquals(500f, viewPoint.x, 0.01f)
        assertEquals(1000f, viewPoint.y, 0.01f)
        
        val mappedBack = CropGeometry.mapPointToSource(viewPoint, viewWidth, viewHeight, sourceWidth, sourceHeight)
        assertEquals(250f, mappedBack.x, 0.01f)
        assertEquals(250f, mappedBack.y, 0.01f)
    }
}
