package killua.dev.twitterdownloader.ui.ViewModels

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
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