package killua.dev.base.ui.filters

sealed class DurationFilter {
    object All : DurationFilter()
    object UnderOneMinute : DurationFilter()
    object OneToThreeMinutes : DurationFilter()
    object ThreeToTenMinutes : DurationFilter()
    object MoreThanTemMinutes : DurationFilter()
}

sealed class TypeFilter{
    object All: TypeFilter()
    object Videos: TypeFilter()
    object Images: TypeFilter()
}

sealed class PlatformFilter{
    object All: PlatformFilter()
    object Twitter: PlatformFilter()
    object Lofter: PlatformFilter()
}

data class FilterOptions(
    val selectedAuthors: Set<String> = emptySet(),
    val durationFilter: DurationFilter = DurationFilter.All,
    val platformFilter: PlatformFilter = PlatformFilter.All,
    val typeFilter: TypeFilter = TypeFilter.All,
)