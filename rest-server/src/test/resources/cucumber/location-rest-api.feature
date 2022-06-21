Feature: Location REST API endpoints integration tests

  Background:
    Given REST server url in system property "rest-server-url"
    Given Keycloak auth server url in system property "keycloak.url", realm "opennms" and client "admin-cli"

  Scenario: admin user can view/add/update/delete location
    Given User "admin-user" with password "password123"
    Then User can loging and create access token
    Then User can create new locations via REST API
      | location | monitoringArea |
      | location-test |  localhost |
      | location-test2 |  office-network |
    Then User can list locations
      | location | monitoringArea |
      | location-test |  localhost |
      | location-test2 |  office-network |
    Then User can get location by ID
    Then User can update the location
    Then User can delete the location by ID

  Scenario: Normal user only can view location
    Given User "test-user" with password "password123"
    Then User can loging and create access token
    Then User can list locations
      | location | monitoringArea |
      | location-test |  localhost |
    Then User can get location by ID
    Then User am not allowed to create new location
    Then User am not allowed to update the location by ID
    Then User am not allowed to delete the location

  Scenario: Not authorized user can't access the REST API
    Then Without correct token user can't access rest api


