package org.acme.twitter;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.oidc.UserInfo;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/twitter")
@Authenticated
public class TwitterService {
	
	@Inject
    UserInfo userInfo;
	
	@Inject
	@RestClient
	TwitterClient twitterClient;
	
	@Inject
    Template tweets;
	
	@Inject
    Template tweet;
	
	@Inject
    Template createTweet;
	
	@GET
	@Produces("text/html")
    public Uni<TemplateInstance> getTweets() {
        final String userName = userInfo.getObject("data").getString("name");
        final String twitterHandle = userInfo.getObject("data").getString("username");
		final String userId = userInfo.getObject("data").getString("id");
		return twitterClient.userTweets(userId, "created_at,conversation_id").onItem()
	      		  .transform(c -> tweets.data("name", userName)
	      				   .data("handle", twitterHandle)
		                   .data("tweets", tweets(c)));
    }
	
	
	@GET
	@Path("/conversation/{conversationId}")
    @Produces("text/html")
    public Uni<TemplateInstance> tweetConversation(@PathParam("conversationId") String conversationId,
    		@QueryParam("tweetId") String tweetId) {
		return twitterClient.searchTweets(
				"conversation_id:" + conversationId,
				"author_id,created_at,conversation_id,in_reply_to_user_id",
				"username",
				"author_id,referenced_tweets.id")
				.onItem().transform(c -> tweet.data("conversationId", conversationId)
						.data("replies", replies(c, tweetId)));
    }
	
	@GET
	@Path("/create/{inRepyToTweetId}")
    @Produces("text/html")
    public Uni<TemplateInstance> getCreateTweetForm(@PathParam("inRepyToTweetId") String inRepyToTweetId) {
		return Uni.createFrom().item(createTweet.data("inRepyToTweetId", inRepyToTweetId));
    }
	
	@POST
	@Produces("text/html")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Response> replyToTweet(@FormParam("inRepyToTweetId") String inRepyToTweetId,
    		@FormParam("text") String text) {
		
		JsonObject tweet = new JsonObject();
		tweet.put("text", text);
		JsonObject reply = new JsonObject();
		reply.put("in_reply_to_tweet_id", inRepyToTweetId);
		tweet.put("reply", reply);
		
		return twitterClient.createTweet(tweet.toString())
				.replaceWith(Response.created(URI.create("/twitter")).build());
    }
	
	@GET
	@Produces("text/html")
	@Path("poll")
	public Uni<Response> startQuarkusPlaylistPoll() {
		
		JsonObject tweet = new JsonObject();
		tweet.put("text", "Support Quarkus Insights session now: which Quarkus playlist track at https://open.spotify.com/playlist/2nSA0oAfkOrLEl62nshuRj do you like most ?");
		JsonObject poll = new JsonObject();
		JsonArray options = new JsonArray();
		options.add("Don't You Worry (Black Eyed Peas, Shakira)");
		options.add("Dancing In the Dark (Bruce Springsteen)");
		options.add("Thunderstruck (AC/DC)");
		poll.put("options", options);
		poll.put("duration_minutes", 20);
		tweet.put("poll", poll);
		
		return twitterClient.createTweet(tweet.toString())
				.replaceWith(Response.created(URI.create("/twitter")).build());
    }
	
	private List<Tweet> tweets(String jsonStr) {
		JsonObject json = new JsonObject(jsonStr);
		List<Tweet> tweetlists = new LinkedList<>();
		JsonArray tweetlistsArray = json.getJsonArray("data");
		for (int i = 0; i < tweetlistsArray.size(); i++) {
			JsonObject item = tweetlistsArray.getJsonObject(i);
			Tweet tweet = new Tweet();
			tweet.createdAt = item.getString("created_at");
			tweet.text = item.getString("text");
			tweet.id = item.getString("id");
			tweet.conversationId = item.getString("conversation_id");
			tweetlists.add(tweet);
		}
		return tweetlists;
	}
	
	private List<Reply> replies(String jsonStr, String tweetId) {
		JsonObject json = new JsonObject(jsonStr);
		
		Map<String, ReplyAuthor> authors = new HashMap<>();
		
		JsonObject includes = json.getJsonObject("includes");
		
		if (includes != null) {
			JsonArray usersArray = includes.getJsonArray("users");
			if (usersArray != null) {
				for (int i = 0; i < usersArray.size(); i++) {
					JsonObject item = usersArray.getJsonObject(i);
					ReplyAuthor replyAuthor = new ReplyAuthor();
					replyAuthor.id = item.getString("id");
					replyAuthor.name = item.getString("name");
					replyAuthor.username = item.getString("username");
					authors.put(replyAuthor.id, replyAuthor);
				}
			}
		}
		
		List<Reply> replyList = new LinkedList<>();
		JsonArray replyListArray = json.getJsonArray("data");
		if (replyListArray != null) {
			for (int i = 0; i < replyListArray.size(); i++) {
				JsonObject item = replyListArray.getJsonObject(i);
				
				if (!tweetIdMatched(tweetId, item)) {
					continue;
				}
				
				Reply reply = new Reply();
				reply.createdAt = item.getString("created_at");
				reply.text = item.getString("text");
				reply.id = item.getString("id");
				reply.conversationId = item.getString("conversation_id");
				
				String authorId = item.getString("author_id");
				reply.author = authors.get(authorId);
				replyList.add(reply);
			}
		}
		return replyList;
	}
		
	private boolean tweetIdMatched(String tweetId, JsonObject item) {
		if (tweetId == null) {
			return true;
		}
		JsonArray referencedTweets = item.getJsonArray("referenced_tweets");
		if (referencedTweets != null) {
			for (int i = 0; i < referencedTweets.size(); i++) {
				JsonObject refItem = referencedTweets.getJsonObject(i);
				if ("replied_to".equals(refItem.getString("type")) 
						&& tweetId.equals(refItem.getString("id"))) {
					return true;
				}
			}
		}
		return false;
	}

	public static class Tweet {
		public String id;
		public String conversationId;
		public String createdAt;
		public String text;
	}
	
	public static class Reply extends Tweet{
		public ReplyAuthor author;
	}
	
	public static class ReplyAuthor {
		public String id;
		public String username;
		public String name;
	}
}

