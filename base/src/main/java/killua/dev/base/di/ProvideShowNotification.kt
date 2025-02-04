package killua.dev.base.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.base.utils.ShowNotification
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideShowNotification {
    @Provides
    @Singleton
    fun provideShowNotification(
        @ApplicationContext context: Context
    ) = ShowNotification(context)
}