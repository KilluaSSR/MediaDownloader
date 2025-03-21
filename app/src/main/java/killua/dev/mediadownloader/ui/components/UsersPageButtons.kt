package killua.dev.mediadownloader.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.VideoLibrary
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.AdvancedpageRoutes

val AdvancedPageTwitterButtons = listOf(
    ButtonsEssentials(
        R.string.get_bookmarks,
        Icons.Rounded.Bookmarks,
        AdvancedpageRoutes.TwitterGetMyBookmarks.route
    ),
    ButtonsEssentials(
        R.string.get_likes,
        Icons.Rounded.ThumbUp,
        AdvancedpageRoutes.TwitterGetMyBookmarks.route
    ),
//    ButtonsEssentials(
//        "Subscribe",
//        Icons.Rounded.Subscriptions,
//        AdvancedpageRoutes.TwitterSubscribe.route
//    ),
    ButtonsEssentials(
        R.string.get_user_media,
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
        R.string.get_by_tags,
        Icons.Rounded.Bookmarks,
        AdvancedpageRoutes.LofterGetAuthorImagesByTags.route,
    ),
)

val AdvancedPageKuaikanButtons = listOf(
    ButtonsEssentials(
        R.string.get_entire_comic,
        Icons.Rounded.Book,
        AdvancedpageRoutes.KuaikanEntireComic.route
    ),
)
val AdvancedPixivButtons = listOf(
    ButtonsEssentials(
        R.string.get_entire_novel,
        Icons.Rounded.Book,
        AdvancedpageRoutes.KuaikanEntireComic.route
    ),
)
val AdvancedMissEvanButtons = listOf(
    ButtonsEssentials(
        R.string.get_entire_drama,
        Icons.Rounded.Radio,
        AdvancedpageRoutes.KuaikanEntireComic.route
    ),
)