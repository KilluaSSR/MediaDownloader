package killua.dev.base.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

val UserPageTwitterButtons = listOf(
    ButtonsEssentials(
        "Subscribe",
        Icons.Rounded.Subscriptions,
        UserpageRoutes.TwitterSubscribe.route
    ),
    ButtonsEssentials(
        "Get all media",
        Icons.Rounded.VideoLibrary,
        UserpageRoutes.TwitterGetAll.route
    )
)

val UserPageLofterButtons = listOf(
    ButtonsEssentials(
        "Authors",
        Icons.Rounded.AccountCircle,
        UserpageRoutes.LofterAuthors.route
    ),
    ButtonsEssentials(
        "Get by Tags",
        Icons.Rounded.Bookmarks,
        UserpageRoutes.LofterGetAuthorImagesByTags.route,
    ),
)