package org.acme.twitter;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.token.propagation.AccessToken;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey="twitter-client")
@AccessToken
@Path("/")
public interface TwitterClient {
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("users/{userid}/tweets")
	Uni<String> userTweets(@PathParam("userid") String userid, 
			               @QueryParam("tweet.fields") String fields);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tweets")
	Uni<String> createTweet(String json);
	
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("tweets/search/recent")
	Uni<String> searchTweets(@QueryParam("query") String query, 
			                @QueryParam("tweet.fields") String tweetFields,
			                @QueryParam("user.fields") String userFields,
			                @QueryParam("expansions") String expansions);
}
