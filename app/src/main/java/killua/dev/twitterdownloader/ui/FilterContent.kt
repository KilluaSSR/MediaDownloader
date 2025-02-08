package killua.dev.twitterdownloader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import killua.dev.base.ui.filters.FilterByAuthors
import killua.dev.base.ui.filters.FilterByDuration
import killua.dev.base.ui.filters.FilterByPlatforms
import killua.dev.base.ui.filters.FilterByType
import killua.dev.base.ui.filters.FilterOptions
import killua.dev.base.ui.tokens.SizeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterContent(
    availableAuthors: Set<String> = emptySet<String>(),
    availableTags: Set<String> = emptySet<String>(),
    currentFilter: FilterOptions,
    onFilterChange: (FilterOptions) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SizeTokens.Level16)
    ) {
        FilterByType(currentFilter, onFilterChange)
        FilterByDuration(currentFilter, onFilterChange)
        FilterByPlatforms(currentFilter, onFilterChange)
        FilterByAuthors(availableAuthors, currentFilter, onFilterChange)
    }
}