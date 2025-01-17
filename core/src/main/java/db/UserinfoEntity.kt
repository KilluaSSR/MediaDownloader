package db

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlin.uuid.ExperimentalUuidApi

@Entity
data class Userinfo @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey
    @ColumnInfo(name = "user_id") val twitterUserId: String? = null,
    @ColumnInfo(name = "user_screen_name") val twitterScreenName: String? = null,
    @ColumnInfo(name = "user_twitter_name") val twitterName: String? = null,
    @ColumnInfo(name = "user_ct0") val userct0: String? = null,
    @ColumnInfo(name = "user_auth") val userauth: String? = null,
)
