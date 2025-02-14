package killua.dev.twitterdownloader.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.VideoLibrary
import killua.dev.base.ui.AdvancedpageRoutes

val AdvancedPageTwitterButtons = listOf(
    ButtonsEssentials(
        "Get my Bookmarks",
        Icons.Rounded.Bookmarks,
        AdvancedpageRoutes.TwitterGetMyBookmarks.route
    ),
    ButtonsEssentials(
        "Get my Likes",
        Icons.Rounded.ThumbUp,
        AdvancedpageRoutes.TwitterGetMyBookmarks.route
    ),
//    ButtonsEssentials(
//        "Subscribe",
//        Icons.Rounded.Subscriptions,
//        AdvancedpageRoutes.TwitterSubscribe.route
//    ),
    ButtonsEssentials(
        "Get someone's all media",
        Icons.Rounded.VideoLibrary,
        AdvancedpageRoutes.TwitterGetAll.route
    )
)

val AdvancedPageLofterButtons = listOf(
//    ButtonsEssentials(
//        "Authors",
//        Icons.Rounded.AccountCircle,
//        AdvancedpageRoutes.LofterAuthors.route
//    ),
    ButtonsEssentials(
        "Get pics by Tags",
        Icons.Rounded.Bookmarks,
        AdvancedpageRoutes.LofterGetAuthorImagesByTags.route,
    ),
)

val AdvancedPageKuaikanButtons = listOf(
    ButtonsEssentials(
        "Entire comic",
        Icons.Rounded.Book,
        AdvancedpageRoutes.KuaikanEntireComic.route
    ),
)