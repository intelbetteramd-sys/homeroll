package app.pixroost.android.spike.data

import android.provider.MediaStore

object MediaStoreConstants {
    const val MEDIA_SELECTION = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
    const val MEDIA_SORT = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC, ${MediaStore.Files.FileColumns._ID} DESC"
    const val FIRST_PAGE_SIZE = 200
    const val PREFS_NAME = "s02"
    const val PREF_STORE_VERSION = "version"
    const val PREF_GENERATION = "generation"
    const val NO_GENERATION = -1L
}
