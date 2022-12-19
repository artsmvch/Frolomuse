package com.frolo.music.model

import com.frolo.music.model.test.mockSong
import com.frolo.test.mockKT
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.*


@RunWith(JUnit4::class)
class ModelSerializationTest {

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
        assertTrue("Deserialized model has a different type", deserializedModel is T)
        assertEquals("Deserialized model is not the same", deserializedModel, model)
        // Clean up the test file
        if (!file.delete()) {
            // throw IllegalStateException("Failed to delete file $file")
        }
    }

    @Test
    fun test_songSerialization() {
        testSerialization(mockSong())
    }

    @Test
    fun test_albumSerialization() {
        testSerialization(mockKT<Album>())
    }

    @Test
    fun test_artistSerialization() {
        testSerialization(mockKT<Artist>())
    }

    @Test
    fun test_playlistSerialization() {
        testSerialization(mockKT<Playlist>())
    }

    @Test
    fun test_genreSerialization() {
        testSerialization(mockKT<Genre>())
    }

    @Test
    fun test_myFileSerialization() {
        testSerialization(mockKT<MyFile>())
    }

    @Test
    fun test_mediaBucketSerialization() {
        testSerialization(mockKT<MediaBucket>())
    }
}