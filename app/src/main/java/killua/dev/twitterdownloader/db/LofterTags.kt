package killua.dev.twitterdownloader.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import killua.dev.base.Model.AvailablePlatforms
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tags")
data class TagEntry(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "tags") val tags: Set<String> = emptySet(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE id = 1")
    fun observeTags(): Flow<TagEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTags(tagEntry: TagEntry)
}

class TypesConverter {
    @TypeConverter
    fun fromType(type: AvailablePlatforms): String = type.name

    @TypeConverter
    fun toType(value: String): AvailablePlatforms = AvailablePlatforms.valueOf(value)
}

class TagsConverter {
    @TypeConverter
    fun fromTags(tags: Set<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toTags(value: String): Set<String> =
        if (value.isEmpty()) emptySet() else value.split(",").toSet()
}