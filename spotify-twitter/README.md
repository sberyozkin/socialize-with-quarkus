# socialize-with-quarkus

Important: To have this demo working with your own Spotify and Twitter social accounts you may need to change your developer subscription plans. You can also try one of the other social providers documented at https://quarkus.io/guides/security-openid-connect-providers instead.


1. Register Spotify (https://quarkus.io/guides/security-openid-connect-providers#spotify) and Twitter (https://quarkus.io/guides/security-openid-connect-providers#twitter) or X (https://quarkus.io/guides/security-openid-connect-providers#x) applications

2. Update the configuration source: https://github.com/sberyozkin/socialize-with-quarkus/blob/main/spotify-twitter/src/main/java/org/acme/ProvidersConfigSource.java#L25

3. Run `mvn quarkus:dev`

4. Go to `http://localhost:8080`: login to Twitter in a new tab, check your posts. Login to Spotify in another new tab, check your playlists and individual playlist tracks
