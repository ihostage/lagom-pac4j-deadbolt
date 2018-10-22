package org.taymyr.hello.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jwt.config.signature.RSASignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oidc.profile.keycloak.KeycloakOidcProfile;
import org.taymyr.hello.api.HelloService;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.pac4j.core.context.HttpConstants.AUTHORIZATION_HEADER;
import static org.pac4j.core.context.HttpConstants.BEARER_HEADER_PREFIX;
import static org.pac4j.lagom.Authorizers.anonymous;
import static org.pac4j.lagom.Authorizers.authenticated;

import static java.util.Collections.singletonList;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(HelloService.class, HelloServiceImpl.class);
  }

  @Provides
  protected JwtAuthenticator provideJwtAuthenticator(com.typesafe.config.Config configuration) {
    // TODO smart initialize algorithm by application.conf
    // return new JwtAuthenticator(singletonList(new SecretSignatureConfiguration(configuration.getString("pac4j.lagom.jwt.secret"))));
    RSASignatureConfiguration rsaSignatureConfiguration = new RSASignatureConfiguration();
    rsaSignatureConfiguration.setPublicKey(createPublicKey(configuration.getString("pac4j.lagom.jwt.rsa.public")));
    return new JwtAuthenticator(singletonList(rsaSignatureConfiguration));
  }

  private RSAPublicKey createPublicKey(String key) {
    try {
      byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
      X509EncodedKeySpec x509publicKey = new X509EncodedKeySpec(byteKey);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) kf.generatePublic(x509publicKey);
    } catch (Exception e) {
      String msg = "Failed to create RSA public key";
      throw new IllegalArgumentException(msg, e);
    }
  }

  @Provides
  protected HeaderClient provideHeaderClient(JwtAuthenticator jwtAuthenticator) {
    HeaderClient headerClient = new HeaderClient();
    headerClient.setHeaderName(AUTHORIZATION_HEADER);
    headerClient.setPrefixHeader(BEARER_HEADER_PREFIX);
    headerClient.setAuthenticator(jwtAuthenticator);
    headerClient.setName("jwt");
    headerClient.setProfileCreator((credentials, context) -> {
      KeycloakOidcProfile profile = new KeycloakOidcProfile();
      profile.setId(credentials.getUserProfile().getId());
      profile.addAttributes(credentials.getUserProfile().getAttributes());
      return profile;
    });
    headerClient.setAuthorizationGenerator((context, profile) -> {
      JSONObject realm_access = (JSONObject) profile.getAttribute("realm_access");
      JSONArray roles = (JSONArray) realm_access.get("roles");
      roles.forEach(role -> profile.addRole((String)role));
      return profile;
    });
    return headerClient;
  }

  @Provides
  protected Config provideConfig(HeaderClient headerJwtClient) {
    final Config config = new Config(headerJwtClient);
    config.getClients().setDefaultSecurityClients(headerJwtClient.getName());
    config.addAuthorizer("_anonymous_", anonymous());
    config.addAuthorizer("_authenticated_", authenticated());
    return config;
  }
}
