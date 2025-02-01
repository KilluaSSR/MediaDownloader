package killua.dev.base.datastore

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(
    name = "DataStore"
)
internal fun Context.readStoreString(key: androidx.datastore.preferences.core.Preferences.Key<String>, defValue: String) = dataStore.data.map { preferences -> preferences[key] ?: defValue }
fun Context.readStoreBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, defValue: Boolean) = dataStore.data.map { preferences -> preferences[key] ?: defValue }
internal fun Context.readStoreInt(key: androidx.datastore.preferences.core.Preferences.Key<Int>, defValue: Int) = dataStore.data.map { preferences -> preferences[key] ?: defValue }
internal fun Context.readStoreLong(key: androidx.datastore.preferences.core.Preferences.Key<Long>, defValue: Long) = dataStore.data.map { preferences -> preferences[key] ?: defValue }
internal suspend fun Context.saveStoreString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) = dataStore.edit { settings -> settings[key] = value }
suspend fun Context.saveStoreBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) = dataStore.edit { settings -> settings[key] = value }
internal suspend fun Context.saveStoreInt(key: androidx.datastore.preferences.core.Preferences.Key<Int>, value: Int) = dataStore.edit { settings -> settings[key] = value }
internal suspend fun Context.saveStoreLong(key: androidx.datastore.preferences.core.Preferences.Key<Long>, value: Long) = dataStore.edit { settings -> settings[key] = value }