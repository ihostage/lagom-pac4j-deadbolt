package org.taymyr.hello.auth;

import be.objectify.deadbolt.java.filters.DeadboltRoutePathFilter;
import play.http.DefaultHttpFilters;

import javax.inject.Inject;

public class Filters extends DefaultHttpFilters {

    @Inject
    public Filters(final DeadboltRoutePathFilter deadbolt) {
        super(deadbolt);
    }

}
