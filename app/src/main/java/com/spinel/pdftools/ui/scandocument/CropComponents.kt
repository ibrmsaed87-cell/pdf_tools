package com.spinel.pdftools.ui.scandocument

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spinel.pdftools.R

@Composable
fun CroppingView(
    uri: Uri,
    initialQuad: Quadrilateral?,
    onRetake: () -> Unit,
    onApplyCrop: (Quadrilateral) -> Unit
) {
    val context = LocalContext.current
    var intrinsicWidth by remember { mutableStateOf(1f) }
    var intrinsicHeight by remember { mutableStateOf(1f) }
    var quad by remember { mutableStateOf<Quadrilateral?>(null) }
    var imageLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val stream = context.contentResolver.openInputStream(uri)
        if (stream != null) {
            BitmapFactory.decodeStream(stream, null, options)
            stream.close()
            
            var rotation = 0
            val exifStream = context.contentResolver.openInputStream(uri)
            if (exifStream != null) {
                val exif = ExifInterface(exifStream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
                exifStream.close()
            }
            
            if (rotation == 90 || rotation == 270) {
                intrinsicWidth = options.outHeight.toFloat()
                intrinsicHeight = options.outWidth.toFloat()
            } else {
                intrinsicWidth = options.outWidth.toFloat()
                intrinsicHeight = options.outHeight.toFloat()
            }
            imageLoaded = true

            if (initialQuad != null) {
                quad = initialQuad
            } else {
                // Initialize default quad 10% inset
                val insetX = intrinsicWidth * 0.1f
                val insetY = intrinsicHeight * 0.1f
                quad = Quadrilateral(
                    topLeft = PointF(insetX, insetY),
                    topRight = PointF(intrinsicWidth - insetX, insetY),
                    bottomRight = PointF(intrinsicWidth - insetX, intrinsicHeight - insetY),
                    bottomLeft = PointF(insetX, intrinsicHeight - insetY)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            if (imageLoaded && quad != null) {
                val viewWidth = constraints.maxWidth.toFloat()
                val viewHeight = constraints.maxHeight.toFloat()
                
                AsyncImage(
                    model = uri,
                    contentDescription = "Crop Target",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                
                var activeCornerIndex by remember { mutableStateOf(-1) }

                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val currentQuad = quad ?: return@detectDragGestures
                                    // Map quad to view space
                                    val viewTL = CropGeometry.mapSourceToView(currentQuad.topLeft, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                                    val viewTR = CropGeometry.mapSourceToView(currentQuad.topRight, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                                    val viewBR = CropGeometry.mapSourceToView(currentQuad.bottomRight, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                                    val viewBL = CropGeometry.mapSourceToView(currentQuad.bottomLeft, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                                    
                                    val corners = listOf(viewTL, viewTR, viewBR, viewBL)
                                    val clickPoint = PointF(offset.x, offset.y)
                                    
                                    // Find closest corner within a reasonable touch target (e.g. 100 pixels)
                                    val touchTarget = 150f
                                    var closestDist = Float.MAX_VALUE
                                    var closestIdx = -1
                                    
                                    for (i in corners.indices) {
                                        val dist = Math.hypot((corners[i].x - clickPoint.x).toDouble(), (corners[i].y - clickPoint.y).toDouble()).toFloat()
                                        if (dist < touchTarget && dist < closestDist) {
                                            closestDist = dist
                                            closestIdx = i
                                        }
                                    }
                                    activeCornerIndex = closestIdx
                                },
                                onDragEnd = {
                                    activeCornerIndex = -1
                                },
                                onDragCancel = {
                                    activeCornerIndex = -1
                                },
                                onDrag = { change, dragAmount ->
                                    if (activeCornerIndex != -1) {
                                        change.consume()
                                        val currentQuad = quad ?: return@detectDragGestures
                                        
                                        // Get the current source point of the active corner, map to view, add drag, map back
                                        val sourcePoint = when (activeCornerIndex) {
                                            0 -> currentQuad.topLeft
                                            1 -> currentQuad.topRight
                                            2 -> currentQuad.bottomRight
                                            else -> currentQuad.bottomLeft
                                        }
                                        
                                        val viewPoint = CropGeometry.mapSourceToView(sourcePoint, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                                        val newViewPoint = PointF(viewPoint.x + dragAmount.x, viewPoint.y + dragAmount.y)
                                        
                                        val newSourcePoint = CropGeometry.mapPointToSource(newViewPoint, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                                        
                                        quad = when (activeCornerIndex) {
                                            0 -> currentQuad.copy(topLeft = newSourcePoint)
                                            1 -> currentQuad.copy(topRight = newSourcePoint)
                                            2 -> currentQuad.copy(bottomRight = newSourcePoint)
                                            else -> currentQuad.copy(bottomLeft = newSourcePoint)
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    val currentQuad = quad
                    if (currentQuad != null) {
                        val tl = CropGeometry.mapSourceToView(currentQuad.topLeft, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                        val tr = CropGeometry.mapSourceToView(currentQuad.topRight, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                        val br = CropGeometry.mapSourceToView(currentQuad.bottomRight, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                        val bl = CropGeometry.mapSourceToView(currentQuad.bottomLeft, viewWidth, viewHeight, intrinsicWidth, intrinsicHeight)
                        
                        val pathStroke = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        
                        // Draw boundaries
                        drawLine(Color.White, Offset(tl.x, tl.y), Offset(tr.x, tr.y), strokeWidth = 5f)
                        drawLine(Color.White, Offset(tr.x, tr.y), Offset(br.x, br.y), strokeWidth = 5f)
                        drawLine(Color.White, Offset(br.x, br.y), Offset(bl.x, bl.y), strokeWidth = 5f)
                        drawLine(Color.White, Offset(bl.x, bl.y), Offset(tl.x, tl.y), strokeWidth = 5f)
                        
                        val radius = 30f
                        // Draw handles
                        drawCircle(Color.White, radius, Offset(tl.x, tl.y))
                        drawCircle(Color.White, radius, Offset(tr.x, tr.y))
                        drawCircle(Color.White, radius, Offset(br.x, br.y))
                        drawCircle(Color.White, radius, Offset(bl.x, bl.y))
                        
                        // Draw inner dots for better target visualization
                        drawCircle(primaryColor, radius * 0.4f, Offset(tl.x, tl.y))
                        drawCircle(primaryColor, radius * 0.4f, Offset(tr.x, tr.y))
                        drawCircle(primaryColor, radius * 0.4f, Offset(br.x, br.y))
                        drawCircle(primaryColor, radius * 0.4f, Offset(bl.x, bl.y))
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onRetake) {
                Text(
                    text = stringResource(R.string.action_retake),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Button(
                onClick = { quad?.let { onApplyCrop(it) } },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.action_apply_crop),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
