package killua.dev.base.ui.filters

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
import killua.dev.base.ui.tokens.SizeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterByType(
    currentFilter: FilterOptions,
    onFilterChange: (FilterOptions) -> Unit
){
    Brush.horizontalGradient(listOf(Color.Red, Color.Blue))

    Text(
        text = "Filter by type",
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
            label = { Text("All") }
        )
        FilterChip(
            selected = currentFilter.typeFilter== TypeFilter.Videos,
            onClick = {
                onFilterChange(currentFilter.copy(typeFilter = TypeFilter.Videos))
            },
            label = { Text("Videos") }
        )
        FilterChip(
            selected = currentFilter.typeFilter== TypeFilter.Images,
            onClick = {
                onFilterChange(currentFilter.copy(typeFilter = TypeFilter.Images))
            },
            label = { Text("Images") }
        )
        FilterChip(
            selected = currentFilter.typeFilter== TypeFilter.PDF,
            onClick = {
                onFilterChange(currentFilter.copy(typeFilter = TypeFilter.PDF))
            },
            label = { Text("PDF") }
        )
    }
}