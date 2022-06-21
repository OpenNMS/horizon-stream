Feature: Location GraphQL Integration tests

  Background:
    Given REST server url in system property "rest-server-url"
    Given Keycloak auth server url in system property "keycloak.url", realm "opennms" and client "admin-cli"

  Scenario: Admin user has full access of locations via graphql endpoint
    Given User "admin-user" with password "password123"
    Then User can loging and create access token
    Then User can create new locations
       | location | monitoringArea |
       | graphql-test |  localhost |
       | graphql-test2 |  office-network |
    Then User can query locations
      | location | monitoringArea |
      | graphql-test |  localhost |
      | graphql-test2 |  office-network |
    Then User can query a location by ID
    Then User can update a location
    Then User can delete a location

  Scenario: Normal user only can view location view locations via graphql endpoint
    Given User "test-user" with password "password123"
    Then User can loging and create access token
    Then User can query locations
      | location | monitoringArea |
      | graphql-test |  localhost |
    Then User can query a location by ID
    Then User not allowed to create a location
    Then User not allowed to update a location
    Then User not allowed to delete a location

  Scenario: Not authorized user can't access the graphql API
    Then Without correct token user can't access graphql api

