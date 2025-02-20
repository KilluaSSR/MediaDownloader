package killua.dev.mediadownloader.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.tokens.SizeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterByType(
    currentFilter: FilterOptions,
    onFilterChange: (FilterOptions) -> Unit
){
    Brush.horizontalGradient(listOf(Color.Red, Color.Blue))

    Text(
        text = stringResource(R.string.filter_by_platforms),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = SizeTokens.Level8)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
    ) {
        FilterChip(
            selected = currentFilter.typeFilter == TypeFilter.All,
            onClick = {
                onFilterChange(currentFilter.copy(typeFilter = TypeFilter.All))
            },
            label = { Text(stringResource(R.string.all)) }
        )
        FilterChip(
            selected = currentFilter.typeFilter== TypeFilter.Videos,
            onClick = {
                onFilterChange(currentFilter.copy(typeFilter = TypeFilter.Videos))
            },
            label = { Text(stringResource(R.string.videos)) }
        )
        FilterChip(
            selected = currentFilter.typeFilter== TypeFilter.Images,
            onClick = {
                onFilterChange(currentFilter.copy(typeFilter = TypeFilter.Images))
            },
            label = { Text(stringResource(R.string.images)) }
        )
        FilterChip(
            selected = currentFilter.typeFilter== TypeFilter.PDF,
            onClick = {
                onFilterChange(currentFilter.copy(typeFilter = TypeFilter.PDF))
            },
            label = { Text(stringResource(R.string.pdf)) }
        )
    }
}