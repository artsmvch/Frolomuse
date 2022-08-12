package com.frolo.muse.database.entity

/**
 * Debug method for detecting anomalies in playlist member entities.
 */
@Throws(PlaylistAnomaly::class)
fun detectPlaylistAnomalies(entities: List<PlaylistMemberEntity>) {
    if (entities.isEmpty()) {
        // Empty list is just OK
        return
    }

    val arrayList = ArrayList(entities)

    // The first item in play order should be there
    val firstInPlayOrderIndex = arrayList.indexOfFirst { it.prevId == null }
    if (firstInPlayOrderIndex < 0) {
        throw PlaylistAnomaly("Could not find the first item in play order")
    }

    var currInPlayOrder: PlaylistMemberEntity? = arrayList.removeAt(firstInPlayOrderIndex)

    var counter = 1

    while (currInPlayOrder != null) {
        if (currInPlayOrder.nextId != null) {

            // The next item in play order should be there
            val nextInPlayOrderIndex = arrayList.indexOfFirst { it.id == currInPlayOrder!!.nextId }
            if (nextInPlayOrderIndex < 0) {
                throw PlaylistAnomaly("Could not find the next item in play order for position $counter")
            }

            val nextInPlayOrder = arrayList.removeAt(nextInPlayOrderIndex)
            if (nextInPlayOrder.prevId != currInPlayOrder.id) {
                throw PlaylistAnomaly("Inconsistency detected: " +
                        "prevId of the next item in play order != id of the current item in play order")
            }

            currInPlayOrder = nextInPlayOrder
            counter++
        } else {
            // Reached the end
            currInPlayOrder = null
        }
    }

    if (arrayList.isNotEmpty()) {
        throw PlaylistAnomaly("Extra elements left, not tied to the order. Count=${arrayList.count()}")
    }
}