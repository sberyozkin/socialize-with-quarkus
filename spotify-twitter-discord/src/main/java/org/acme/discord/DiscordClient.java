package org.acme.discord;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.token.propagation.AccessToken;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey="discord-client")
@AccessToken
@Path("/")
public interface DiscordClient {

  
    @GET
	@Path("/channels/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Uni<String> getChannel(@PathParam("id") String id);

	@GET
	@Path("/channels/{channel.id}/messages")
	@Consumes(MediaType.APPLICATION_JSON)
	Uni<String> getChannelMessages(@PathParam("id") String id);
	
}
