package com.spinel.pdftools.ui.files

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spinel.pdftools.R
import com.spinel.pdftools.ui.components.PremiumEmptyState

@Composable
fun FilesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.nav_files),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        PremiumEmptyState(
            title = stringResource(id = R.string.empty_recent_title),
            description = stringResource(id = R.string.empty_recent_desc),
            icon = Icons.Filled.FolderOpen
        )
        
        Spacer(modifier = Modifier.weight(1.5f))
    }
}
