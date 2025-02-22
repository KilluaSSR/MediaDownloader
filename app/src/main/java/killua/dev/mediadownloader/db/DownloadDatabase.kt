package killua.dev.mediadownloader.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import db.Download
import db.UriTypeConverter

@Database(
    entities = [Download::class, TagEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(UriTypeConverter::class, TypesConverter::class, TagsConverter::class)
abstract class MediaDownloadDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: MediaDownloadDatabase? = null
        fun getInstance(context: Context): MediaDownloadDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MediaDownloadDatabase::class.java,
                    "media_downloads.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}