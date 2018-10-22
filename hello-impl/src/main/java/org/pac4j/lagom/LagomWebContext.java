package org.pac4j.lagom;

import com.lightbend.lagom.javadsl.api.transport.RequestHeader;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.TechnicalException;

import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;

public class LagomWebContext implements WebContext {

    private RequestHeader requestHeader;

    public LagomWebContext(RequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    @Override
    public SessionStore getSessionStore() {
        return null;
    }

    @Override
    public String getRequestParameter(String name) {
        return null;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return null;
    }

    @Override
    public Object getRequestAttribute(String name) {
        return null;
    }

    @Override
    public void setRequestAttribute(String name, Object value) {

    }

    @Override
    public String getRequestHeader(String name) {
        return requestHeader.getHeader(name).orElseThrow(() -> new TechnicalException(format("Header %s not found", name)));
    }

    @Override
    public String getRequestMethod() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public void writeResponseContent(String content) {

    }

    @Override
    public void setResponseStatus(int code) {

    }

    @Override
    public void setResponseHeader(String name, String value) {

    }

    @Override
    public void setResponseContentType(String content) {

    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getFullRequestURL() {
        return null;
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        return null;
    }

    @Override
    public void addResponseCookie(Cookie cookie) {

    }

    @Override
    public String getPath() {
        return null;
    }
}
