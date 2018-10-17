package org.taymyr.hello.auth;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import be.objectify.deadbolt.java.filters.AuthorizedRoute;
import be.objectify.deadbolt.java.filters.AuthorizedRoutes;
import be.objectify.deadbolt.java.filters.FilterConstraints;
import com.lightbend.lagom.javadsl.api.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taymyr.hello.api.HelloService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

public class HelloAuthorizedRoutes extends AuthorizedRoutes {

    private static final Logger logger = LoggerFactory.getLogger(HelloAuthorizedRoutes.class);

    private List<AuthorizedRoute> routes;

    @Inject
    public HelloAuthorizedRoutes(final Provider<FilterConstraints> filterConstraintsProvider, HelloService service) {
        super(filterConstraintsProvider);
        this.routes = new ArrayList<>();
        for (Descriptor.Call call : service.descriptor().calls()) {
            try {
                Method method = call.serviceCallHolder().getClass().getDeclaredMethod("method");
                Method serviceMethod = (Method) method.invoke(call.serviceCallHolder());
                SubjectPresent subjectPresent = serviceMethod.getAnnotation(SubjectPresent.class);
                if (subjectPresent != null) {
                    if (call.callId() instanceof Descriptor.PathCallId) {
                        routes.add(new AuthorizedRoute(Optional.empty(), ((Descriptor.PathCallId)call.callId()).pathPattern(),
                                filterConstraints.subjectPresent()));
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public List<AuthorizedRoute> routes() {
        return routes;
    }
}
