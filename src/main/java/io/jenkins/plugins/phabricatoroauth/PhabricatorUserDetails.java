package io.jenkins.plugins.phabricatoroauth;

import hudson.security.SecurityRealm;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

public class PhabricatorUserDetails implements UserDetails {
  private final String username;
  private final String realName;

  public PhabricatorUserDetails(String username, String realName) {
    this.username = username;
    this.realName = realName;
  }

  @Override
  public GrantedAuthority[] getAuthorities() {
    return new GrantedAuthority[] {
        SecurityRealm.AUTHENTICATED_AUTHORITY
    };
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  // FIXME
  public static PhabricatorUserDetails fromWhoamiResult(PhabricatorWhoamiResult whoamiResult) {
    return new PhabricatorUserDetails(whoamiResult.getUserName(), whoamiResult.getRealName());
  }

  public String getRealName() {
    return realName;
  }
}
