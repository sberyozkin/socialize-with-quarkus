package org.acme.spotify;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.token.propagation.AccessToken;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey="spotify-client")
@AccessToken
@Path("/")
public interface SpotifyClient {

	@GET
	@Path("me/playlists")
	@Consumes(MediaType.APPLICATION_JSON)
	Uni<String> getPlaylists();
	
	@GET
	@Path("playlists/{id}/tracks")
	@Consumes(MediaType.APPLICATION_JSON)
	Uni<String> getPlaylistTracks(@PathParam("id") String id);
}
