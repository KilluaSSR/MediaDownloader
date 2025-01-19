package killua.dev.twitterdownloader.ui

import Model.SnackbarUIEffect
import ui.BaseViewModel
import ui.UIEffect
import ui.UIIntent
import ui.UIState
import javax.inject.Inject
sealed class TwitterUserPageIntent : UIIntent {
    data class Search(val query: String) : TwitterUserPageIntent()
    data class Sort(val sortType: SortType) : TwitterUserPageIntent()
    object Refresh : TwitterUserPageIntent()
}
enum class SortType {
    NAME_ASC,    // 按名字升序
    NAME_DESC,   // 按名字降序
    DATE_ASC,    // 按日期升序
    DATE_DESC,   // 按日期降序
    SIZE_ASC,    // 按大小升序
    SIZE_DESC    // 按大小降序
}

//data class TwitterUserPageState(
//    val users: List<TwitterUserItem> = emptyList(),
//    val sortType: SortType = SortType.NAME_ASC,
//) : UIState
//
//
//class TwitterUserPageViewmodel @Inject constructor(): BaseViewModel<TwitterUserPageIntent, TwitterUserPageState, SnackbarUIEffect>(
//    TwitterUserPageState()
//){
//
//}