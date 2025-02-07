package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Lofter

import android.content.Context
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState

data class LofterPreparePageUIState(
    val isLoggedIn: Boolean = false,
    val dateSelected: Boolean = false,
    val startDate: String = "0",
    val endDate: String = "0",

) : UIState

sealed class LofterPreparePageUIIntent : UIIntent {
    data class OnDateChanged(val context: Context) : LofterPreparePageUIIntent()
    data class OnResume(val context: Context) : LofterPreparePageUIIntent()
    data class OnTagsChanged(val context: Context) : LofterPreparePageUIIntent()
}