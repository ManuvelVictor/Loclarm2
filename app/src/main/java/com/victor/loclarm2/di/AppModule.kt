package com.victor.loclarm2.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.loclarm2.data.local.DataStoreManager
import com.victor.loclarm2.data.local.SettingsDataStore
import com.victor.loclarm2.data.repository.AlarmRepositoryImpl
import com.victor.loclarm2.data.repository.AuthRepositoryImpl
import com.victor.loclarm2.domain.repository.AlarmRepository
import com.victor.loclarm2.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        credentialManager: CredentialManager
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore, credentialManager)
    }

    @Provides
    @Singleton
    fun provideAlarmRepository(firestore: FirebaseFirestore): AlarmRepository {
        return AlarmRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object AppModule {
        @Provides
        @Singleton
        fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
            return SettingsDataStore(context)
        }
    }

}