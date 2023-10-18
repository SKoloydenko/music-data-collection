import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.neovisionaries.i18n.CountryCode
import okhttp3.OkHttpClient
import okhttp3.Request
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures
import se.michaelthelin.spotify.model_objects.specification.Category
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack

class SpotifyClient {
    private val api = authenticate()
    private val client = OkHttpClient.Builder().build()

    private fun authenticate(): SpotifyApi {
        val api = SpotifyApi.Builder().setClientId(System.getenv("CLIENT_ID"))
            .setClientSecret(System.getenv("CLIENT_SECRET"))
            .build()
        val accessToken = api.clientCredentials().build().execute().accessToken
        api.accessToken = accessToken
        return api
    }

    fun getCategories(): List<Category> {
        val request = api.listOfCategories.country(CountryCode.US).limit(50).build()
        return request.execute().items.filterNotNull()
    }

    fun getPlaylists(categoryId: String): List<PlaylistSimplified> {
        val request = api.getCategorysPlaylists(categoryId).country(CountryCode.US).limit(50).build()
        return request.execute().items.filterNotNull()
    }

    fun getPlaylistTracks(playlistId: String): List<PlaylistTrack> {
        val request = api.getPlaylistsItems(playlistId).limit(50).build()
        return request.execute().items.filterNotNull()
    }

    fun getArtistsAndReleaseYear(trackId: String): Pair<List<String>, Int> {
        val request = api.getTrack(trackId).build()
        val track = request.execute()
        val artists = track.artists.map { it.name }
        val year = track.album.releaseDate.subSequence(0, 4).toString().toInt()
        return Pair(artists, year)
    }

    fun getTrackFeatures(trackId: String): AudioFeatures {
        val request = api.getAudioFeaturesForTrack(trackId).build()
        return request.execute()
    }

    fun getPlayCount(trackId: String): Long {
        val variables = "%7B%22uri%22%3A%22spotify%3Atrack%3A$trackId%22%7D"
        val extensions =
            "%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%22e101aead6d78faa11d75bec5e36385a07b2f1c4a0420932d374d89ee17c70dd6%22%7D%7D"
        val token =
            System.getenv("ACCESS_TOKEN")
        val request = Request.Builder()
            .header(
                "Authorization",
                "Bearer $token",
            )
            .url("https://api-partner.spotify.com/pathfinder/v1/query?operationName=getTrack&variables=$variables&extensions=$extensions")
            .build()
        val response = client.newCall(request).execute()
        val mapper = ObjectMapper().registerKotlinModule()
        val json = mapper.readTree(response.body?.string())
        return json["data"]["trackUnion"]["playcount"].asLong()
    }
}
