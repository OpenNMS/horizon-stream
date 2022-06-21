Feature: Node REST API endpoints integration tests

  Background:
    Given REST server url in system property "rest-server-url"
    Given Keycloak auth server url in system property "keycloak.url", realm "opennms" and client "admin-cli"

  Scenario: Admin user have full access to all endpoints
    Given User "admin-user" with password "password123"
    Then User can loging and create access token
    Then Use can create new node
      | label | foreignId | sysLocation |
      | node-label1 | test-foreign-id1 | node-test-location1 |
      | node-label2 | test-foreign-id2 | node-test-location2 |
    Then User can list nodes
      | label | foreignId | sysLocation |
      | node-label1 | test-foreign-id1 | node-test-location1 |
      | node-label2 | test-foreign-id2 | node-test-location2 |
    Then User can get node by ID
    Then User can update node
    Then User can delete node

  Scenario: Normal user only can view node
    Given User "test-user" with password "password123"
    Then User can loging and create access token
    Then User can list nodes
      | label | foreignId | sysLocation |
      | node-label1 | test-foreign-id1 | node-test-location1 |
    Then User can get node by ID
    Then User is not allowed to create new node
    Then User is not allowed to update a node
    Then User is not allowed to delete a node

  Scenario: Not authorized user can't access the node endpoint
    Then Without correct token user can't access node endpoint

