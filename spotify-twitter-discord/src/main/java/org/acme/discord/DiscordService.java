package org.acme.discord;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.oidc.UserInfo;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;


@Path("/discord")
@Authenticated
public class DiscordService {
	
    @Inject
    UserInfo userInfo;

    @Inject
	@RestClient
	DiscordClient discordClient;
    
    @Inject
    Template discord;
    
    @Inject
    Template quarkusclub;
	
    @GET
    @Produces("text/html")
    public TemplateInstance userinfo() {
	    return discord.data("name", userInfo.getString("global_name"));
	}
	
    @GET
    @Produces("text/html")
    @Path("quarkusclub")
    public TemplateInstance quarkusclub() {
    	return quarkusclub.data("name", userInfo.getString("global_name"));	
	   // return discordClient.getChannel("1134208020277100544");
	}
}

