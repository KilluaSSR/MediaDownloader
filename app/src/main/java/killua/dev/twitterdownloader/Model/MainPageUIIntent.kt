package killua.dev.twitterdownloader.Model

import android.content.Context
import killua.dev.base.ui.UIIntent

sealed class MainPageUIIntent : UIIntent {
    data class ExecuteDownload(val tweetID: String) : MainPageUIIntent()
    data class NavigateToFavouriteUser(val context: Context, val userID: String, val screenName: String) : MainPageUIIntent()
}