package org.acme.basic;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/basic")
@Authenticated
public class BasicService {
	
	@Inject
    SecurityIdentity identity;
	
	@GET
	@Produces("text/plain")
    public String getName() {
        return "Hello, " + identity.getPrincipal().getName();
    }
}

