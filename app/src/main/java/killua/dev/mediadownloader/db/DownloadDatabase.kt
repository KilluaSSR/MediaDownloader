package db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import killua.dev.mediadownloader.db.TagDao
import killua.dev.mediadownloader.db.TagEntry
import killua.dev.mediadownloader.db.TagsConverter
import killua.dev.mediadownloader.db.TypesConverter

@Database(
    entities = [Download::class, TagEntry::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(UriTypeConverter::class, TypesConverter::class, TagsConverter::class)
abstract class TwitterDownloadDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: TwitterDownloadDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加type列并设置默认值
                database.execSQL(
                    "ALTER TABLE Download ADD COLUMN type TEXT NOT NULL DEFAULT 'Twitter'"
                )

                // 重命名列并创建新表
                database.execSQL("""
                    CREATE TABLE Download_new (
                        uuid TEXT NOT NULL PRIMARY KEY,
                        user_id TEXT,
                        screen_name TEXT,
                        name TEXT,
                        type TEXT NOT NULL DEFAULT 'Twitter',
                        file_uri TEXT,
                        link TEXT,
                        tweetID TEXT,
                        file_name TEXT NOT NULL,
                        file_type TEXT NOT NULL,
                        file_size INTEGER NOT NULL,
                        range_header TEXT,
                        status TEXT NOT NULL,
                        progress INTEGER NOT NULL,
                        error_message TEXT,
                        created_at INTEGER NOT NULL,
                        completed_at INTEGER,
                        mime_type TEXT
                    )
                """)

                // 迁移数据
                database.execSQL("""
                    INSERT INTO Download_new (
                        uuid, user_id, screen_name, name, type, file_uri, link,
                        tweetID, file_name, file_type, file_size, range_header,
                        status, progress, error_message, created_at, completed_at,
                        mime_type
                    )
                    SELECT 
                        uuid, twitter_user_id, twitter_screen_name, twitter_name,
                        'Twitter', file_uri, link, tweetID, file_name, file_type,
                        file_size, range_header, status, progress, error_message,
                        created_at, completed_at, mime_type
                    FROM Download
                """)

                // 删除旧表并重命名新表
                database.execSQL("DROP TABLE Download")
                database.execSQL("ALTER TABLE Download_new RENAME TO Download")

                // 创建tags表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER NOT NULL PRIMARY KEY,
                        tags TEXT NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getInstance(context: Context): TwitterDownloadDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TwitterDownloadDatabase::class.java,
                    "twitter_downloads.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}