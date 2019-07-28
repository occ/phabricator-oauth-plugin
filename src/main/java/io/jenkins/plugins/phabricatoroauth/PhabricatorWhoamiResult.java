package io.jenkins.plugins.phabricatoroauth;

@SuppressWarnings("unused")
public class PhabricatorWhoamiResult {
  private String userName;
  private String primaryEmail;
  private String realName;
  private String image;

  public String getUserName() {
    return userName;
  }

  public String getRealName() {
    return realName;
  }

  public String getPrimaryEmail() {
    return primaryEmail;
  }

  public String getImage() {
    return image;
  }
}
