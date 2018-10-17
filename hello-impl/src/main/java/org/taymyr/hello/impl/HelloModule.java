package org.taymyr.hello.impl;

import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.filters.AuthorizedRoutes;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.play.deadbolt2.Pac4jRoleHandler;
import org.pac4j.play.http.DefaultHttpActionAdapter;
import org.pac4j.play.store.PlayCookieSessionStore;
import org.pac4j.play.store.PlaySessionStore;
import org.taymyr.hello.api.HelloService;
import org.taymyr.hello.auth.HelloAuthorizedRoutes;
import org.taymyr.hello.auth.HelloCustomRoleHandler;
import org.taymyr.hello.auth.HelloPac4jHandlerCache;

import javax.inject.Singleton;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(HelloService.class, HelloServiceImpl.class);

    bind(PlaySessionStore.class).to(PlayCookieSessionStore.class);

    bind(AuthorizedRoutes.class).to(HelloAuthorizedRoutes.class).in(Singleton.class);

    bind(Pac4jRoleHandler.class).to(HelloCustomRoleHandler.class);
    bind(HandlerCache.class).to(HelloPac4jHandlerCache.class);
  }

  @Provides
  protected HeaderClient helloHeaderClient() {
    HeaderClient headerClient = new HeaderClient(HttpConstants.AUTHORIZATION_HEADER, (credentials, webContext) -> {
      final CommonProfile profile = new CommonProfile();
      profile.setId(((TokenCredentials)credentials).getToken());
      credentials.setUserProfile(profile);
    });
    return headerClient;
  }

  @Provides
  protected Config provideConfig(HeaderClient headerClient) {
    final Config config = new Config(headerClient);
    config.setHttpActionAdapter(new DefaultHttpActionAdapter());
    return config;
  }
}
