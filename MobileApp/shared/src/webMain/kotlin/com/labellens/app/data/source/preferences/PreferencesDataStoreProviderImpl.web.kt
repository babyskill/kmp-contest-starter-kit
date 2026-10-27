package com.labellens.app.data.source.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import com.labellens.app.util.Constants

class PreferencesDataStoreProviderImpl : PreferencesDataStoreProvider {
    override fun providePreferencesDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        storage = WebLocalStorage(
            serializer = PreferencesSerializer,
            name = Constants.PREFERENCES_STORAGE_NAME,
        ),
    )
}
