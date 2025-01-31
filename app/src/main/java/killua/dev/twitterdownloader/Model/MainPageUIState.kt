package killua.dev.twitterdownloader.Model

import killua.dev.base.ui.UIState

data class MainPageUIState(
    val youHaveDownloadedSth: Boolean = false,
    val favouriteUserName: String = "",
    val favouriteUserScreenName: String = "",
    val favouriteUserID: String = "",
    val downloadedTimes: Int = 0
) : UIState