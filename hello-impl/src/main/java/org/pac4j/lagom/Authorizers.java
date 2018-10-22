package org.pac4j.lagom;

import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.CheckProfileTypeAuthorizer;
import org.pac4j.core.authorization.authorizer.IsAnonymousAuthorizer;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class Authorizers {

    private static final Authorizer AUTHENTICATED = new IsAuthenticatedAuthorizer<>();
    private static final Authorizer ANONYMOUS = new IsAnonymousAuthorizer<>();

    @SuppressWarnings("unchecked")
    public static <U extends CommonProfile> Authorizer<U> authenticated() {
        return AUTHENTICATED;
    }

    @SuppressWarnings("unchecked")
    public static <U extends CommonProfile> Authorizer<U> anonymous() {
        return ANONYMOUS;
    }

    @SuppressWarnings("unchecked")
    public static <U extends CommonProfile> Authorizer<U> anyRole(String ... roles) {
        return new RequireAnyRoleAuthorizer(roles);
    }

    @SuppressWarnings("unchecked")
    public static <U extends CommonProfile> Authorizer<U> anyRole(List<String> roles) {
        return new RequireAnyRoleAuthorizer(roles);
    }

    @SuppressWarnings("unchecked")
    public static <U extends CommonProfile> Authorizer<U> anyRole(Set<String> roles) {
        return new RequireAnyRoleAuthorizer(roles);
    }

    @SuppressWarnings("unchecked")
    public static Authorizer<CommonProfile> typed(Class<? extends CommonProfile> clazz) {
        return new CheckProfileTypeAuthorizer(clazz);
    }

    public static <U extends CommonProfile> Authorizer<U> and(Authorizer<U>... authorizers) {
        return new AndAuthorizer<>(Arrays.asList(authorizers));
    }

    public static <U extends CommonProfile> OrAuthorizer<U> or(Authorizer<U>... authorizers) {
        return new OrAuthorizer<>(Arrays.asList(authorizers));
    }

    private static class AndAuthorizer<U extends CommonProfile> implements Authorizer<U> {

        private final List<Authorizer<U>> authorizers;

        AndAuthorizer(List<Authorizer<U>> authorizers) {
            this.authorizers = authorizers;
        }

        @Override
        public boolean isAuthorized(WebContext context, List<U> profiles) {
            for (Authorizer<U> authorizer : authorizers) {
                if (!authorizer.isAuthorized(context, profiles)) return false;
            }
            return true;
        }

    }

    public static class OrAuthorizer<U extends CommonProfile> implements Authorizer<U> {

        private final List<Authorizer<U>> authorizers;

        OrAuthorizer(List<Authorizer<U>> authorizers) {
            this.authorizers = authorizers;
        }

        @Override
        public boolean isAuthorized(WebContext context, List<U> profiles) {
            for (Authorizer<U> authorizer : authorizers) {
                if (authorizer.isAuthorized(context, profiles)) return true;
            }
            return false;
        }
    }

    private Authorizers() {
    }
}
