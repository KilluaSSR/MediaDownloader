package killua.dev.mediadownloader.ui.ViewModels

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import javax.inject.Inject

data class UserInfoPageUIState(
    val favouriteUserName: String = "",
    val favouriteUserScreenName: String = "",
    val downloadedTimes: Int = 0,
    val lofterTagsCount: Int = 0,
    val totalDownloadsCount: Int = 0
): UIState
@HiltViewModel
class UserInfoPageViewModel @Inject constructor(

): BaseViewModel<UIIntent,UserInfoPageUIState, SnackbarUIEffect>(UserInfoPageUIState())