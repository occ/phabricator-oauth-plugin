# Phabricator Authorization Plugin for Jenkins

## Setup

### Setup Phabricator

* Enable OAuth Server application
* Go to OAuth Server and create an application for Jenkins
  * Redirect URI should be: `[JENKINS_ROOT]/securityRealm/finishLogin`.
* Note Client PHID and Application Secret

### Setup Jenkins

* Install phabricator-oauth-plugin
  * If building from source use `mvn hpi:hpi` to build the hpi file.
* Configure the plugin at `Manage Jenkins > Configure Global Securityr`
  * Enable security and choose "Phabricator Authentication Plugin" security realm.
  * Enter Phabricator URI. This is `phabricator.base-uri`.
  * Enter Client PHID and Application Secret values from the Phabricator OAuth Server.
