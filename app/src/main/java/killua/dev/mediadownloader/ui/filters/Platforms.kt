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
fun FilterByPlatforms(
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
            selected = currentFilter.platformFilter == PlatformFilter.All,
            onClick = {
                onFilterChange(currentFilter.copy(platformFilter = PlatformFilter.All))
            },
            label = { Text(stringResource(R.string.all)) }
        )
        FilterChip(
            selected = currentFilter.platformFilter == PlatformFilter.Twitter,
            onClick = {
                onFilterChange(currentFilter.copy(platformFilter = PlatformFilter.Twitter))
            },
            label = { Text(stringResource(R.string.twitter)) }
        )
        FilterChip(
            selected = currentFilter.platformFilter == PlatformFilter.Lofter,
            onClick = {
                onFilterChange(currentFilter.copy(platformFilter = PlatformFilter.Lofter))
            },
            label = { Text(stringResource(R.string.lofter)) }
        )
        FilterChip(
            selected = currentFilter.platformFilter == PlatformFilter.Pixiv,
            onClick = {
                onFilterChange(currentFilter.copy(platformFilter = PlatformFilter.Pixiv))
            },
            label = { Text(stringResource(R.string.pixiv)) }
        )
        FilterChip(
            selected = currentFilter.platformFilter == PlatformFilter.Kuaikan,
            onClick = {
                onFilterChange(currentFilter.copy(platformFilter = PlatformFilter.Kuaikan))
            },
            label = { Text(stringResource(R.string.kuaikan)) }
        )
        FilterChip(
            selected = currentFilter.platformFilter == PlatformFilter.MissEvan,
            onClick = {
                onFilterChange(currentFilter.copy(platformFilter = PlatformFilter.MissEvan))
            },
            label = { Text(stringResource(R.string.missevan)) }
        )
    }
}