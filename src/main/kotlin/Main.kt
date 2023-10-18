import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

fun main() {
    val client = SpotifyClient()
    val trackEntities = ConcurrentHashMap<String, Track>()
    try {
        collect(client, trackEntities)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    writeCSV(trackEntities)
}

fun collect(client: SpotifyClient, trackEntities: MutableMap<String, Track>) {
    val trackMapper = TrackMapper()
    val maxAmount = 500

    client.getCategories().forEach { category ->
        println("Category ${category.name} with id: ${category.id}")

        client.getPlaylists(category.id).forEach { playlist ->
            if (playlist.name != null) {
                println("\tPlaylist ${playlist.name}")
                client.getPlaylistTracks(playlist.id).forEach { playlistTrack ->
                    if (playlistTrack.track != null) {
                        val trackId = playlistTrack.track.id
                        val trackName = playlistTrack.track.name

                        val (artists, year) = client.getArtistsAndReleaseYear(trackId)
                        val count = client.getPlayCount(trackId)
                        if (year == 2020 && count != 0L && !trackEntities.containsKey(trackId)) {
                            val features = client.getTrackFeatures(trackId)
                            val trackEntity = trackMapper.asEntity(features, trackName, artists, count, year)
                            trackEntities[trackId] = trackEntity
                            println("\t\tAdded track with id $trackId and name $trackName")
                            if (trackEntities.size >= maxAmount) {
                                return
                            }
                        } else {
                            println("\t\tSkipped track with id $trackId and name $trackName")
                        }
                        println("Current count is ${trackEntities.size}")
                    }
                }
            }
        }
    }
}

fun writeCSV(trackEntities: Map<String, Track>) {
    println("Writing to csv...")

    val trackMapper = TrackMapper()

    val filename = "${LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-")}.csv"
    val file = File(filename)
    val writer = CSVWriter(FileWriter(file))
    writer.use {
        writer.writeNext(
            arrayOf(
                "Id",
                "Song name",
                "Artists names",
                "Acousticness",
                "Danceability",
                "Energy",
                "Instrumentalness",
                "Liveness",
                "Loudness",
                "Speechiness",
                "Valence",
                "Play count",
                "Year",
            ),
        )
        trackEntities.entries.forEach {
            writer.writeNext(trackMapper.asStringArray(it.value))
        }
    }
    println("Finished writing to csv")
}
