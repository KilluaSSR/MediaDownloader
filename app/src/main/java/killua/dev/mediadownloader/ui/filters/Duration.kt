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
import androidx.compose.ui.res.stringResource
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.tokens.SizeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterByDuration(
    currentFilter: FilterOptions,
    onFilterChange: (FilterOptions) -> Unit
){
    Text(
        text = stringResource(R.string.filter_by_duration),
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
            label = { Text(stringResource(R.string.less_than_one_min))}
        )
        FilterChip(
            selected = currentFilter.durationFilter == DurationFilter.OneToThreeMinutes,
            onClick = {
                onFilterChange(currentFilter.copy(durationFilter = DurationFilter.OneToThreeMinutes))
            },
            label = { Text(stringResource(R.string.one_to_three_min)) }
        )
        FilterChip(
            selected = currentFilter.durationFilter == DurationFilter.ThreeToTenMinutes,
            onClick = {
                onFilterChange(currentFilter.copy(durationFilter = DurationFilter.ThreeToTenMinutes))
            },
            label = { Text(stringResource(R.string.three_to_ten_min)) }
        )
        FilterChip(
            selected = currentFilter.durationFilter == DurationFilter.MoreThanTemMinutes,
            onClick = {
                onFilterChange(currentFilter.copy(durationFilter = DurationFilter.MoreThanTemMinutes))
            },
            label = { Text(stringResource(R.string.more_than_ten_min)) }
        )
    }
}