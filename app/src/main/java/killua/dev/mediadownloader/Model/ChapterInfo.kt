package killua.dev.mediadownloader.Model

import killua.dev.mediadownloader.api.Kuaikan.Chapter
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanEpisodes
import killua.dev.mediadownloader.api.Pixiv.Model.NovelInfo

sealed class ChapterInfo {
    abstract val id: String
    abstract val title: String
    abstract val seriesName: String?
    data class DownloadableChapter(
        override val id: String,
        override val title: String,
        override val seriesName: String?
    ) : ChapterInfo()

}

fun Chapter.toChapterInfo() =
    ChapterInfo.DownloadableChapter(id = id, title = name, seriesName = null)

fun NovelInfo.toChapterInfo() =
    ChapterInfo.DownloadableChapter(id = id, title = title, seriesName = seriesTitle)

fun MissEvanEpisodes.toChapterInfo(seriesName: String) =
    ChapterInfo.DownloadableChapter(id = sound_id, title = name, seriesName = seriesName)