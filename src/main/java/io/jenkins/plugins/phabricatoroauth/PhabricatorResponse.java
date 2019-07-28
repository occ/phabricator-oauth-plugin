package io.jenkins.plugins.phabricatoroauth;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class PhabricatorResponse {
  private PhabricatorWhoamiResult result;

  @SerializedName("error_code")
  private int errorCode;

  @SerializedName("error_info")
  private String errorInfo;

  @SuppressWarnings("unchecked")
  static PhabricatorResponse fromJson(String json) {
    final Gson gson = new Gson();
    return gson.fromJson(json, PhabricatorResponse.class);
  }

  public PhabricatorWhoamiResult getResult() {
    return result;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getErrorInfo() {
    return errorInfo;
  }
}
