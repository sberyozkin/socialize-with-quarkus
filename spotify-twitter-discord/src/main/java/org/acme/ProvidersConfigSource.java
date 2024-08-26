package org.acme;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class ProvidersConfigSource implements ConfigSource {

	@Override
	public Set<String> getPropertyNames() {
		return providers.keySet();
	}

	@Override
	public String getValue(String propertyName) {
		return providers.get(propertyName);
	}

	@Override
	public String getName() {
		return "providers-config-source";
	}

	private static final Map<String, String> providers = 
			Map.of("spotify-client-id", "your-spotify-client-id",
				   "spotify-client-secret", "your-spotify-client-secret",
				   "twitter-client-id", "your-twitter-client-id",
				   "twitter-client-secret", "your-spotify-client-secret",
				   "discord-client-id", "your-discord-client-id",
				   "discord-client-secret", "your-discord-client-secret",
				   "basic-auth-secret", "basic-auth-secret");
	
}
