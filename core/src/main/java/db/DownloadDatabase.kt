package db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Download::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(UriTypeConverter::class)
abstract class TwitterDownloadDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: TwitterDownloadDatabase? = null

        fun getInstance(context: Context): TwitterDownloadDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TwitterDownloadDatabase::class.java,
                    "twitter_downloads.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}