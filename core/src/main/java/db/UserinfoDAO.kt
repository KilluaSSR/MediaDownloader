package db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlin.uuid.ExperimentalUuidApi

@Dao
interface UserinfoDAO{
    @Query("SELECT * FROM Userinfo")
    suspend fun getUser(): List<Userinfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(Userinfo: Userinfo)

    @Delete
    suspend fun delete(Userinfo: Userinfo)
}