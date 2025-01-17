package db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Userinfo::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(UriTypeConverter::class)
abstract class UserinfoDatabase : RoomDatabase() {
    abstract fun userinfoDao(): UserinfoDAO
    companion object {
        @Volatile
        private var INSTANCE: UserinfoDatabase? = null
        fun getInstance(context: Context): UserinfoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UserinfoDatabase::class.java,
                    "user_info.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}