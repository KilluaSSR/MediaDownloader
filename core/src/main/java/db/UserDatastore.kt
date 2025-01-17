package db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import di.ApplicationScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Userdata(
    val screenname: String,
    val name: String,
    val userct0: String,
    val userauth: String,
)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_data"
)
object UserPreuserferences {
    private val SCREENNAME_KEY = stringPreferencesKey("screenname")
    private val NAME_KEY = stringPreferencesKey("name")
    private val USERCT0_KEY = stringPreferencesKey("userct0")
    private val USERAUTH_KEY = stringPreferencesKey("userauth")

    suspend fun saveUserdata(context: Context, userdata: Userdata) {
        context.dataStore.edit { preferences ->
            preferences[SCREENNAME_KEY] = userdata.screenname
            preferences[NAME_KEY] = userdata.name
            preferences[USERCT0_KEY] = userdata.userct0
            preferences[USERAUTH_KEY] = userdata.userauth
        }
    }

    fun getUserdata(context: Context): Flow<Userdata?> {
        return context.dataStore.data.map { preferences ->
            val screenname = preferences[SCREENNAME_KEY]
            val name = preferences[NAME_KEY]
            val userct0 = preferences[USERCT0_KEY]
            val userauth = preferences[USERAUTH_KEY]
            if (screenname != null && name != null && userct0 != null && userauth != null) {
                Userdata(screenname, name, userct0, userauth)
            } else {
                null
            }
        }
    }
}