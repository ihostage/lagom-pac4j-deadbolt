package org.taymyr.hello.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oidc.profile.keycloak.KeycloakOidcProfile;
import org.taymyr.hello.api.GreetingMessage;
import org.taymyr.hello.api.HelloService;
import org.taymyr.hello.impl.HelloCommand.Hello;
import org.taymyr.hello.impl.HelloCommand.UseGreetingMessage;

import javax.inject.Inject;

import static org.pac4j.lagom.Authorizers.and;
import static org.pac4j.lagom.Authorizers.anyRole;
import static org.pac4j.lagom.Authorizers.authenticated;
import static org.pac4j.lagom.Authorizers.typed;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the HelloService.
 */
public class HelloServiceImpl implements HelloService, SecuredService {

  private final PersistentEntityRegistry persistentEntityRegistry;

  private final Config securityConfig;

  @Inject
  public HelloServiceImpl(PersistentEntityRegistry persistentEntityRegistry, Config securityConfig) {
    this.persistentEntityRegistry = persistentEntityRegistry;
    this.securityConfig = securityConfig;
    persistentEntityRegistry.register(HelloEntity.class);
  }

  @Override
  public ServiceCall<NotUsed, String> hello(String id) {
    return request -> {
      // Look up the hello world entity for the given ID.
      PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
      // Ask the entity the Hello command.
      return ref.ask(new Hello(id));
    };
  }

  @Override
  public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
    return request -> {
      // Look up the hello world entity for the given ID.
      PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
      // Tell the entity to use the greeting message specified.
      return ref.ask(new UseGreetingMessage(request.message));
    };

  }

  @Override
  public Topic<org.taymyr.hello.api.HelloEvent> helloEvents() {
    // We want to publish all the shards of the hello event
    return TopicProducer.taggedStreamWithOffset(HelloEvent.TAG.allTags(), (tag, offset) ->

      // Load the event stream for the passed in shard tag
      persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {

      // Now we want to convert from the persisted event to the published event.
      // Although these two events are currently identical, in future they may
      // change and need to evolve separately, by separating them now we save
      // a lot of potential trouble in future.
      org.taymyr.hello.api.HelloEvent eventToPublish;

      if (eventAndOffset.first() instanceof HelloEvent.GreetingMessageChanged) {
        HelloEvent.GreetingMessageChanged messageChanged = (HelloEvent.GreetingMessageChanged) eventAndOffset.first();
        eventToPublish = new org.taymyr.hello.api.HelloEvent.GreetingMessageChanged(
          messageChanged.getName(), messageChanged.getMessage()
        );
      } else {
        throw new IllegalArgumentException("Unknown event: " + eventAndOffset.first());
      }

        // We return a pair of the translated event, and its offset, so that
        // Lagom can track which offsets have been published.
        return Pair.create(eventToPublish, eventAndOffset.second());
      })
    );
  }

  @Override
  public ServiceCall<NotUsed, String> securedHello() {
    Authorizer<CommonProfile> authorizer = and(
            authenticated(),
            typed(KeycloakOidcProfile.class),
            anyRole("uma_authorization")
    );
    return authorize(authorizer, profile ->
            request -> completedFuture("Hello, " + profile.getId())
    );
  }

  @Override
  public Config getSecurityConfig() {
    return securityConfig;
  }
}
