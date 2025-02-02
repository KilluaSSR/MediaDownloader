package killua.dev.base.ui.filters

sealed class DurationFilter {
    object All : DurationFilter()
    object UnderOneMinute : DurationFilter()
    object OneToThreeMinutes : DurationFilter()
    object ThreeToTenMinutes : DurationFilter()
    object MoreThanTemMinutes : DurationFilter()
}

data class FilterOptions(
    val selectedAuthors: Set<String> = emptySet(),
    val durationFilter: DurationFilter = DurationFilter.All
)