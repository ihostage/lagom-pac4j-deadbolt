package org.taymyr.hello.impl;

import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.server.ServerServiceCall;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.lagom.LagomWebContext;

import java.util.function.Function;

import static com.lightbend.lagom.javadsl.server.HeaderServiceCall.compose;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

public interface SecuredService {

    Config getSecurityConfig();

    @SuppressWarnings("unchecked")
    default <Request, Response> ServerServiceCall<Request, Response> authenticate(
            Function<CommonProfile, ServerServiceCall<Request, Response>> serviceCall) {
        return compose(requestHeader -> {
            CommonProfile profile = null;
            try {
                Clients clients = getSecurityConfig().getClients();
                Client defaultClient = clients.findClient(clients.getDefaultSecurityClients());
                LagomWebContext context = new LagomWebContext(requestHeader);
                profile = defaultClient.getUserProfile(defaultClient.getCredentials(context), context);
            } catch (TechnicalException ex) {
                // do nothing
            }
            return serviceCall.apply(ofNullable(profile).orElse(new AnonymousProfile()));
        });
    }

    default <Request, Response> ServerServiceCall<Request, Response> authorize(
            Authorizer<CommonProfile> authorizer,
            Function<CommonProfile, ServerServiceCall<Request, Response>> serviceCall) {
        return authenticate(profile -> compose(requestHeader -> {
            if (authorizer == null || !authorizer.isAuthorized(new LagomWebContext(requestHeader), singletonList(profile))) {
                throw new Forbidden("Authorization failed");
            }
            return serviceCall.apply(profile);
        }));
    }

    @SuppressWarnings("unchecked")
    default <Request, Response> ServerServiceCall<Request, Response> authorize(
            String authorizerName,
            Function<CommonProfile, ServerServiceCall<Request, Response>> serviceCall) {
        return authorize(getSecurityConfig().getAuthorizers().get(authorizerName), serviceCall);
    }

}
