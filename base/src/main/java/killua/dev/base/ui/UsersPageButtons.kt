package killua.dev.base.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.VideoLibrary

val AdvancedPageTwitterButtons = listOf(
    ButtonsEssentials(
        "Get my Bookmarks",
        Icons.Rounded.Bookmarks,
        AdvancedpageRoutes.TwitterGetMyBookmarks.route
    ),
    ButtonsEssentials(
        "Subscribe",
        Icons.Rounded.Subscriptions,
        AdvancedpageRoutes.TwitterSubscribe.route
    ),
    ButtonsEssentials(
        "Get all media",
        Icons.Rounded.VideoLibrary,
        AdvancedpageRoutes.TwitterGetAll.route
    )
)

val AdvancedPageLofterButtons = listOf(
    ButtonsEssentials(
        "Authors",
        Icons.Rounded.AccountCircle,
        AdvancedpageRoutes.LofterAuthors.route
    ),
    ButtonsEssentials(
        "Get by Tags",
        Icons.Rounded.Bookmarks,
        AdvancedpageRoutes.LofterGetAuthorImagesByTags.route,
    ),
)