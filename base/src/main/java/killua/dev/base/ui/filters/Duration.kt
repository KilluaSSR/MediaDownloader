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
import killua.dev.base.ui.tokens.SizeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterByDuration(
    currentFilter: FilterOptions,
    onFilterChange: (FilterOptions) -> Unit
){
    Text(
        text = "Filter by duration",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = SizeTokens.Level8)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
    ) {
        FilterChip(
            selected = currentFilter.durationFilter == DurationFilter.UnderOneMinute,
            onClick = {
                onFilterChange(currentFilter.copy(durationFilter = DurationFilter.UnderOneMinute))
            },
            label = { Text("Less than 1 min") }
        )
        FilterChip(
            selected = currentFilter.durationFilter == DurationFilter.OneToThreeMinutes,
            onClick = {
                onFilterChange(currentFilter.copy(durationFilter = DurationFilter.OneToThreeMinutes))
            },
            label = { Text("1-3 min") }
        )
        FilterChip(
            selected = currentFilter.durationFilter == DurationFilter.ThreeToTenMinutes,
            onClick = {
                onFilterChange(currentFilter.copy(durationFilter = DurationFilter.ThreeToTenMinutes))
            },
            label = { Text("3-10 min") }
        )
        FilterChip(
            selected = currentFilter.durationFilter == DurationFilter.MoreThanTemMinutes,
            onClick = {
                onFilterChange(currentFilter.copy(durationFilter = DurationFilter.MoreThanTemMinutes))
            },
            label = { Text("More than 10 min") }
        )
    }
}