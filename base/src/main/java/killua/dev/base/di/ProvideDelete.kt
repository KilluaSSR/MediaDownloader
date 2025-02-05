package killua.dev.base.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.base.utils.FileDelete
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDelete {
    @Provides
    @Singleton
    fun provideDelete(@ApplicationContext context: Context) = FileDelete(context)
}