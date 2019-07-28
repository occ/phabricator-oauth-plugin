package io.jenkins.plugins.phabricatoroauth;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;

public class PhabricatorAuthenticationToken extends AbstractAuthenticationToken {

  private PhabricatorUserDetails userDetails;

  public PhabricatorAuthenticationToken(PhabricatorUserDetails userDetails) {
    super(new GrantedAuthority[0]);
    this.userDetails = userDetails;
  }

  @Override
  public Object getCredentials() {
    return "";
  }

  @Override
  public Object getPrincipal() {
    return getUserDetails();
  }

  public PhabricatorUserDetails getUserDetails() {
    return userDetails;
  }
}
