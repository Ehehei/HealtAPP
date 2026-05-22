package com.example.data.storage

import android.content.Context
import android.net.Uri
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.File
import java.util.UUID

class PhotoStorage(private val context: Context) {

    init {
        AeadConfig.register()
    }

    private val aead: Aead by lazy {
        AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREF)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    private val rootDir: File
        get() = File(context.filesDir, "user_photos").apply { if (!exists()) mkdirs() }

    fun savePhoto(sourceUri: Uri, subDir: String = "body"): String {
        val dir = File(rootDir, subDir).apply { if (!exists()) mkdirs() }
        val target = File(dir, "${UUID.randomUUID()}.enc")
        val plain = context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Cannot open input stream for $sourceUri" }
            input.readBytes()
        }
        val cipher = aead.encrypt(plain, ASSOCIATED_DATA)
        target.writeBytes(cipher)
        return target.absolutePath
    }

    fun readBytes(path: String): ByteArray? {
        val file = File(path)
        if (!file.exists()) return null
        val raw = file.readBytes()
        return if (file.extension == "enc") aead.decrypt(raw, ASSOCIATED_DATA) else raw
    }

    fun deletePhoto(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.delete()
    }

    private companion object {
        const val KEYSET_NAME = "photo_keyset"
        const val KEYSET_PREF = "photo_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://photo_master_key"
        val ASSOCIATED_DATA: ByteArray = "healtapp.photo".toByteArray()
    }
}
