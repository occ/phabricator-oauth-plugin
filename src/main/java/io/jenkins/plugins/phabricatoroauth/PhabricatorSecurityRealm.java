package io.jenkins.plugins.phabricatoroauth;

import static com.github.scribejava.core.utils.OAuthEncoder.encode;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.security.SecurityRealm;
import hudson.util.HttpResponses;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.SecurityListener;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.context.SecurityContextHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@SuppressWarnings("unused")
public class PhabricatorSecurityRealm extends SecurityRealm {
  private static final String REFERER_ATTRIBUTE = PhabricatorSecurityRealm.class.getName() + ".referer";
  private static final String TIMESTAMP_ATTRIBUTE = PhabricatorSecurityRealm.class.getName() + ".beginTime";
  private static final String NONCE_ATTRIBUTE = PhabricatorSecurityRealm.class.getName() + ".nonce";
  private static final Logger LOG = Logger.getLogger(PhabricatorSecurityRealm.class.getName());
  private final String clientPhId;
  private final String phabricatorUri;
  private final String applicationSecret;

  @DataBoundConstructor
  public PhabricatorSecurityRealm(String phabricatorUri, String clientPhId, String applicationSecret) {
    this.phabricatorUri = phabricatorUri;
    this.clientPhId = clientPhId;
    this.applicationSecret = applicationSecret;
  }

  @Override
  public SecurityComponents createSecurityComponents() {
    AuthenticationManager am = authentication -> {
      if (authentication instanceof PhabricatorAuthenticationToken) return authentication;
      throw new BadCredentialsException("Invalid Phabricator credentials");
    };

    return new SecurityComponents(am);
  }

  @Override
  public boolean allowsSignup() {
    return false;
  }

  @Override
  public String getLoginUrl() {
    return "securityRealm/commenceLogin";
  }

  @SuppressWarnings("unused")
  public HttpResponse doCommenceLogin(StaplerRequest request, @QueryParameter String from, @Header("Referer") final String referer) {
    LOG.info(String.format("Request: %s, from: %s, referer: %s", request, from, referer));

    OAuth20Service phabricatorLoginService = getPhabricatorOAuth20Service();
    request.getSession().setAttribute(REFERER_ATTRIBUTE, referer);
    return new HttpRedirect(phabricatorLoginService.getAuthorizationUrl());
  }

  private OAuth20Service getPhabricatorOAuth20Service() {
    String callbackUri = getOAuthCallbackUri();

    PhabricatorOAuthApi phabricatorOAuthApi = new PhabricatorOAuthApi(URI.create(getPhabricatorUri()));
    return new ServiceBuilder(clientPhId)
        .apiSecret(applicationSecret)
        .callback(callbackUri)
        .build(phabricatorOAuthApi);
  }

  private String getOAuthCallbackUri() {
    String rootUrl = Jenkins.get().getRootUrl();
    if (rootUrl == null) {
      throw new NullPointerException("Jenkins root URL wasn't configured.");
    }

    URI jenkinsRootUri = URI.create(rootUrl);
    return jenkinsRootUri.resolve("securityRealm/finishLogin").toString();
  }

  private PhabricatorWhoamiResult getUserDetails(final OAuth20Service oAuthService, final OAuth2AccessToken accessToken)
      throws InterruptedException, ExecutionException, IOException {
    final OAuthRequest whoamiRequest = new OAuthRequest(Verb.GET,
        URI.create(getPhabricatorUri())
            .resolve("/api/user.whoami?access_token=" + encode(accessToken.getAccessToken())).toString());
    final Response whoamiResponse = oAuthService.execute(whoamiRequest);

    if (!whoamiResponse.isSuccessful()) {
      throw new IllegalStateException("Couldn't fetch use details from Phabricator. Invalid access token?");
    }

    PhabricatorResponse response = PhabricatorResponse.fromJson(whoamiResponse.getBody());
    if (response.getErrorCode() != 0) {
      throw new IllegalStateException("Error occurred while fetching user details. Error: " + response.getErrorInfo());
    }

    return response.getResult();
  }

  @SuppressWarnings("unused")
  public HttpResponse doFinishLogin(StaplerRequest request, @QueryParameter String code) {
    LOG.info(String.format("Request: %s code: %s", request, code));

    if (code == null) {
      return HttpResponses.error(404, "Code was null");
    }

    try {
      OAuth20Service phabricatorLoginService = getPhabricatorOAuth20Service();
      final OAuth2AccessToken accessToken = phabricatorLoginService.getAccessToken(code);
      final PhabricatorUserDetails userDetails = PhabricatorUserDetails
          .fromWhoamiResult(getUserDetails(phabricatorLoginService, accessToken));

      final PhabricatorAuthenticationToken token = new PhabricatorAuthenticationToken(userDetails);

      SecurityContextHolder.getContext().setAuthentication(token);
      User u = User.current();
      if (u != null) {
        u.setFullName(userDetails.getRealName());
      }
      SecurityListener.fireAuthenticated(userDetails);
    } catch (InterruptedException | IOException | ExecutionException e) {
      return HttpResponses.error(500, "Unable to authenticate. " + e.getMessage());
    }

    String referer = (String) request.getSession().getAttribute(REFERER_ATTRIBUTE);
    if (referer != null) {
      return HttpResponses.redirectTo(referer);
    } else {
      return HttpResponses.redirectToContextRoot();
    }
  }

  public String getPhabricatorUri() {
    return phabricatorUri;
  }

  public String getClientPhId() {
    return clientPhId;
  }

  public String getApplicationSecret() {
    return applicationSecret;
  }

  @Extension
  public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
    @Override
    public String getDisplayName() {
      return "Phabricator Authentication Plugin";
    }
  }
}
