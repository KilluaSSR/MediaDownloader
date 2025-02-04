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
fun FilterByAuthors(
    availableAuthors: Set<String>,
    currentFilter: FilterOptions,
    onFilterChange: (FilterOptions) -> Unit
){
    Text(
        text = "Filter by author",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = SizeTokens.Level8)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
    ) {
        availableAuthors.forEach { author ->
            FilterChip(
                selected = currentFilter.selectedAuthors.contains(author),
                onClick = {
                    val newAuthors = if (currentFilter.selectedAuthors.contains(author)) {
                        currentFilter.selectedAuthors - author
                    } else {
                        currentFilter.selectedAuthors + author
                    }
                    onFilterChange(currentFilter.copy(selectedAuthors = newAuthors))
                },
                label = { Text(author) }
            )
        }
    }
}