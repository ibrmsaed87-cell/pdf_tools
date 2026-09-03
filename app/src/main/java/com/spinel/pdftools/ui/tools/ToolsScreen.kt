package com.spinel.pdftools.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spinel.pdftools.R
import com.spinel.pdftools.ui.components.PremiumGridToolCard
import com.spinel.pdftools.ui.components.SectionHeader
import com.spinel.pdftools.ui.home.ToolItem
import com.spinel.pdftools.ui.theme.AccentBlue
import com.spinel.pdftools.ui.theme.AccentOrange
import com.spinel.pdftools.ui.theme.AccentPurple
import com.spinel.pdftools.ui.theme.AccentTeal

data class ToolCategory(
    val titleResId: Int,
    val tools: List<ToolItem>
)

@Composable
fun ToolsScreen(onNavigateToImageToPdf: () -> Unit = {}) {
    val categories = listOf(
        ToolCategory(
            titleResId = R.string.category_create,
            tools = listOf(
                ToolItem(R.string.action_scan_document, R.string.desc_scan_document, Icons.Filled.DocumentScanner, AccentPurple),
                ToolItem(R.string.action_image_to_pdf, R.string.desc_image_to_pdf, Icons.Filled.Image, AccentBlue)
            )
        ),
        ToolCategory(
            titleResId = R.string.category_optimize,
            tools = listOf(
                ToolItem(R.string.action_compress_pdf, R.string.desc_compress_pdf, Icons.Filled.Compress, AccentTeal)
            )
        ),
        ToolCategory(
            titleResId = R.string.category_edit_organize,
            tools = listOf(
                ToolItem(R.string.action_merge_pdf, R.string.desc_merge_pdf, Icons.AutoMirrored.Filled.MergeType, AccentPurple),
                ToolItem(R.string.action_split_pdf, R.string.desc_split_pdf, Icons.Filled.Splitscreen, AccentOrange),
                ToolItem(R.string.action_organize_pdf, R.string.desc_organize_pdf, Icons.Filled.GridView, AccentTeal)
            )
        ),
        ToolCategory(
            titleResId = R.string.category_convert,
            tools = listOf(
                ToolItem(R.string.action_pdf_to_jpg, R.string.desc_pdf_to_jpg, Icons.Filled.PictureAsPdf, AccentBlue)
            )
        )
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = stringResource(id = R.string.nav_tools),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(id = R.string.tools_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        categories.forEach { category ->
            item(span = { GridItemSpan(2) }) {
                SectionHeader(
                    title = stringResource(id = category.titleResId),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(category.tools) { tool ->
                PremiumGridToolCard(
                    title = stringResource(id = tool.titleResId),
                    description = stringResource(id = tool.descResId),
                    icon = tool.icon,
                    iconContainerColor = tool.iconContainerColor,
                    onClick = { 
                        if (tool.titleResId == R.string.action_image_to_pdf) {
                            onNavigateToImageToPdf()
                        } else {
                            /* Coming soon */
                        }
                    }
                )
            }
        }
        
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
