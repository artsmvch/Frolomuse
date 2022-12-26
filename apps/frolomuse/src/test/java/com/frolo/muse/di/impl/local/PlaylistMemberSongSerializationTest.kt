package com.frolo.muse.di.impl.local

import com.frolo.music.model.test.mockSong
import com.frolo.test.mockKT
import junit.framework.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class PlaylistMemberSongSerializationTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private inline fun <reified T> testSerialization(model: T) {
        // Create a test file
        val file = tmpFolder.newFile("model_serialization")
        val outputStream = ObjectOutputStream(FileOutputStream(file))
        outputStream.writeObject(model)
        outputStream.flush()
        outputStream.close()
        val inputStream = ObjectInputStream(FileInputStream(file))
        val deserializedModel = inputStream.readObject()
        Assert.assertTrue("Deserialized model has a different type", deserializedModel is T)
        Assert.assertEquals("Deserialized model is not the same", deserializedModel, model)
        // Clean up the test file
        if (!file.delete()) {
            // throw IllegalStateException("Failed to delete file $file")
        }
    }

    @Test
    fun test_PlaylistMemberSongSerialization() {
        val model = PlaylistDatabaseManager.PlaylistMemberSong(
            song = mockSong(),
            playlistId = mockKT(),
            entity = mockKT()
        )
        testSerialization<PlaylistDatabaseManager.PlaylistMemberSong>(model)
    }
}