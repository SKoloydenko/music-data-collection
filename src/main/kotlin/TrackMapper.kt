import se.michaelthelin.spotify.model_objects.specification.AudioFeatures

class TrackMapper {
    fun asEntity(features: AudioFeatures, name: String, artists: List<String>, playCount: Long, year: Int): Track {
        return Track(
            id = features.id,
            name = name,
            artists = artists,
            acousticness = features.acousticness,
            danceability = features.danceability,
            energy = features.energy,
            instrumentalness = features.instrumentalness,
            liveness = features.liveness,
            loudness = features.loudness,
            speechiness = features.speechiness,
            valence = features.valence,
            playCount = playCount,
            year = year,
        )
    }

    fun asStringArray(track: Track): Array<String> {
        return arrayOf(
            track.id,
            track.name,
            track.artists.joinToString(", "),
            track.acousticness.toString(),
            track.danceability.toString(),
            track.energy.toString(),
            track.instrumentalness.toString(),
            track.liveness.toString(),
            track.loudness.toString(),
            track.speechiness.toString(),
            track.valence.toString(),
            track.playCount.toString(),
            track.year.toString(),
        )
    }
}
