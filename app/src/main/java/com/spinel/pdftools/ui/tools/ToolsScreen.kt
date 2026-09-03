package com.spinel.pdftools.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spinel.pdftools.R
import com.spinel.pdftools.ui.home.ActionCard
import com.spinel.pdftools.ui.home.PrimaryActionCard
import com.spinel.pdftools.ui.home.ToolItem

@Composable
fun ToolsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.nav_tools),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val quickTools = listOf(
            ToolItem(R.string.action_image_to_pdf, Icons.Filled.Image, isImportant = true),
            ToolItem(R.string.action_compress_pdf, Icons.Filled.Compress, isImportant = true),
            ToolItem(R.string.action_merge_pdf, Icons.AutoMirrored.Filled.MergeType),
            ToolItem(R.string.action_split_pdf, Icons.Filled.Splitscreen),
            ToolItem(R.string.action_pdf_to_jpg, Icons.Filled.PictureAsPdf),
            ToolItem(R.string.action_organize_pdf, Icons.Filled.GridView)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item(span = { GridItemSpan(2) }) {
                PrimaryActionCard()
            }
            items(quickTools) { tool ->
                ActionCard(
                    title = stringResource(id = tool.titleResId), 
                    icon = tool.icon,
                    isImportant = tool.isImportant
                )
            }
        }
    }
}

