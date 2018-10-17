package org.taymyr.hello.auth;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import org.pac4j.core.config.Config;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.deadbolt2.Pac4jRoleHandler;
import org.pac4j.play.store.PlaySessionStore;
import play.libs.concurrent.HttpExecutionContext;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HelloPac4jHandlerCache implements HandlerCache {

    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    private Config config;

    private HttpExecutionContext httpExecutionContext;

    private PlaySessionStore playSessionStore;

    private DeadboltHandler defaultHandler;

    private final Pac4jRoleHandler roleHandler;

    @Inject
    public HelloPac4jHandlerCache(final Config config, final HttpExecutionContext httpExecutionContext,
                                  final PlaySessionStore playSessionStore, Pac4jRoleHandler roleHandler) {
        this.config = config;
        this.httpExecutionContext = httpExecutionContext;
        this.playSessionStore = playSessionStore;
        this.roleHandler = roleHandler;
        defaultHandler = new HelloPac4jHandler(config, httpExecutionContext, null, playSessionStore, roleHandler);
        handlers.put(ConfigKeys.DEFAULT_HANDLER_KEY, defaultHandler);
    }

    @Override
    public DeadboltHandler apply(final String clients) {
        DeadboltHandler handler = handlers.get(clients);
        if (handler == null) {
            handler = getAndBuildHandler(clients);
        }
        return handler;
    }

    protected synchronized DeadboltHandler getAndBuildHandler(final String clients) {
        DeadboltHandler handler = handlers.get(clients);
        if (handler == null) {
            handler = new HelloPac4jHandler(config, httpExecutionContext, clients, playSessionStore, roleHandler);
        }
        return handler;
    }

    @Override
    public DeadboltHandler get() {
        return defaultHandler;
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "handlers", handlers, "config", config,
                "httpExecutionContext", httpExecutionContext, "playSessionStore", playSessionStore);
    }
}
