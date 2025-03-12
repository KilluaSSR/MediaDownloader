package killua.dev.mediadownloader.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.mediadownloader.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface TwitterSubscriptionRepository {
    fun getAllSubscriptions(): Flow<Map<String, Boolean>>
    suspend fun updateSubscription(authorName: String, isSubscribed: Boolean)
    suspend fun updateBatchSubscriptions(subscriptions: Map<String, Boolean>)
    suspend fun clearAllSubscriptions()
    suspend fun isSubscribed(authorName: String): Boolean
}

class TwitterSubscriptionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TwitterSubscriptionRepository {

    companion object {
        private const val TWITTER_AUTHOR_SUBSCRIPTION_PREFIX = "twitter_author_subscription_"

        private fun getTwitterAuthorSubscriptionKey(authorName: String): Preferences.Key<Boolean> {
            return booleanPreferencesKey("$TWITTER_AUTHOR_SUBSCRIPTION_PREFIX$authorName")
        }
    }

    override fun getAllSubscriptions(): Flow<Map<String, Boolean>> = context.dataStore.data.map { preferences ->
        preferences.asMap().entries
            .filter { it.key.name.startsWith(TWITTER_AUTHOR_SUBSCRIPTION_PREFIX) }
            .associate {
                val authorName = it.key.name.removePrefix(TWITTER_AUTHOR_SUBSCRIPTION_PREFIX)
                authorName to (it.value as Boolean)
            }
    }

    override suspend fun updateSubscription(authorName: String, isSubscribed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[getTwitterAuthorSubscriptionKey(authorName)] = isSubscribed
        }
    }

    override suspend fun updateBatchSubscriptions(subscriptions: Map<String, Boolean>) {
        context.dataStore.edit { preferences ->
            subscriptions.forEach { (authorName, isSubscribed) ->
                preferences[getTwitterAuthorSubscriptionKey(authorName)] = isSubscribed
            }
        }
    }

    override suspend fun clearAllSubscriptions() {
        context.dataStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys
                .filter { it.name.startsWith(TWITTER_AUTHOR_SUBSCRIPTION_PREFIX) }

            keysToRemove.forEach { key ->
                preferences.remove(key)
            }
        }
    }

    override suspend fun isSubscribed(authorName: String): Boolean {
        return context.dataStore.data.first()[getTwitterAuthorSubscriptionKey(authorName)] == true
    }
}