package io.jenkins.plugins.phabricatoroauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import java.net.URI;

/**
 * Phabricator OAuth Server Configuration.
 *
 * <p>Reference: https://secure.phabricator.com/book/phabcontrib/article/using_oauthserver/
 */
public class PhabricatorOAuthApi extends DefaultApi20 {
  private final URI baseUri;

  public PhabricatorOAuthApi(URI baseUri) {
    this.baseUri = baseUri;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return baseUri.resolve("/oauthserver/token/").toString();
  }

  @Override
  protected String getAuthorizationBaseUrl() {
    return baseUri.resolve("/oauthserver/auth/").toString();
  }
}
