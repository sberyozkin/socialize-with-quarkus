package org.acme.spotify;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.oidc.UserInfo;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

/*
 * 1. Get private playlists - single playlist
 * - external_urls object -> `spotify` property
 * - checks `items[0]`
 *   - images array [0]
 *   - id -> playlists/{id}/tracks 
 *    
 *    -> each items - is a track, images[0] -> track image
 *       `preview_url` is an mp3 preview 
 */

@Path("/spotify")
@Authenticated
public class SpotifyService {
	
	@Inject
    UserInfo userInfo;
	
	@Inject
	@RestClient
	SpotifyClient spotifyClient;
	
	@Inject
    Template playlists;
	
	@Inject
    Template tracks;
	
	@GET
	@Produces("text/html")
    public Uni<TemplateInstance> getPlaylists() {
        return spotifyClient.getPlaylists().onItem()
    		  .transform(c -> playlists.data("name", userInfo.getDisplayName())
    				                   .data("playlists", playlists(c)));
    }
	
	@GET
	@Path("/playlist/{id}/tracks")
    @Produces("text/html")
    public Uni<TemplateInstance> getPlaylistTracks(@PathParam("id") String id) {
        return spotifyClient.getPlaylistTracks(id).onItem()
    		  .transform(c -> tracks.data("name", userInfo.getDisplayName())
	                   .data("tracks", tracks(c)));
    }
	

	
	private List<Playlist> playlists(String jsonStr) {
		JsonObject json = new JsonObject(jsonStr);
		List<Playlist> playlists = new LinkedList<>();
		JsonArray playlistsArray = json.getJsonArray("items");
		for (int i = 0; i < playlistsArray.size(); i++) {
			JsonObject item = playlistsArray.getJsonObject(i);
			Playlist playlist = new Playlist();
			playlist.name = item.getString("name");
			playlist.id = item.getString("id");
			JsonArray imagesArray = item.getJsonArray("images");
			playlist.imageUrl = imagesArray.getJsonObject(0).getString("url");
			JsonObject externalUrls = item.getJsonObject("external_urls");
			playlist.spotifyUrl = externalUrls.getString("spotify");
			playlists.add(playlist);
		}
		return playlists;
	}
	
	private List<Track> tracks(String jsonStr) {
		JsonObject json = new JsonObject(jsonStr);
		List<Track> tracks = new LinkedList<>();
		JsonArray tracksArray = json.getJsonArray("items");
		for (int i = 0; i < tracksArray.size(); i++) {
			JsonObject item = tracksArray.getJsonObject(i);
			Track track = new Track();
			JsonObject trackObject = item.getJsonObject("track");
			track.name = trackObject.getString("name");
			track.previewUrl = trackObject.getString("preview_url");
			JsonObject externalUrls = trackObject.getJsonObject("external_urls");
			track.spotifyUrl = externalUrls.getString("spotify");
			
			JsonObject album = trackObject.getJsonObject("album");
			JsonArray imagesArray = album.getJsonArray("images");
			track.imageUrl = imagesArray.getJsonObject(0).getString("url");
			
			tracks.add(track);
		}
		return tracks;
	}
	
	public static class Playlist {
		public String id;
		public String name;
		public String imageUrl;
		public String spotifyUrl;
	}
	
	public static class Track {
		public String name;
		public String previewUrl;
		public String imageUrl;
		public String spotifyUrl;
	}
	
}

