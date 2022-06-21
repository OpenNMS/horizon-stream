Feature: User management integration tests

  Background:
    Given REST server url in system property "rest-server-url"
    Given Keycloak auth server url in system property "keycloak.url", realm "opennms" and client "admin-cli"

  Scenario: Regular user only have can access own account with limit operations
    Given User "admin-user" with password "password123"
    Then User can loging and create access token
    Then User can create a new user
    # new user values
    """json
      {
        "username": "it-test-user2",
        "firstName": "test",
        "lastName": "user",
        "email": "ittestuser2@opennms.com",
        "roles": ["user"]
      }
     """
    Given User "test-user" with password "password123"
    Then User can loging and create access token
    Then User can get own account by id
      | username | test-user |
      | roles.get(0) | user |
    Then User can update his own account
    """json
      {
        "username": "test-user",
        "firstName": "test",
        "lastName": "user",
        "email": "testuser-updated@opennms.com",
        "roles": ["user"]
     }
     """
    Then User can reset password to "newPassword123" for his own account
    Then User can't change roles for his account
    """json
      {
        "username": "test-user",
        "firstName": "test",
        "lastName": "user",
        "email": "testuser-updated@opennms.com",
        "roles": ["user", "admin"]
     }
     """
    Then User can not search users
    Then User can not create user
    Then User can not delete a user
    Then User can not update other user account
    Then User can not reset password for other user account

  Scenario: Admin user has full access to user management endpoints
    Given User "admin-user" with password "password123"
    Then User can loging and create access token
    Then User can get own account by id
      | username | admin-user |
      | roles.get(0) | admin |
    Then User can list users
    # json path and values
      | size() | 3 |
      | [0].username | admin-user |
      | [1].username | it-test-user2 |
      | [2].username | test-user     |
      | [0].roles.get(0) | admin |
      | [1].roles.get(0) | user  |
      | [2].roles.get(0) | user  |
    Then User can search users with partial username "it-test"
      | size() | 1 |
      | [0].username | it-test-user2 |
      | [0].firstName    | test          |
      | [0].lastName     | user          |
      | [0].email        | ittestuser2@opennms.com |
      | [0].roles[0] | user |
    Then User can get other user account by ID
    | username | it-test-user2 |
    | firstName | test        |
    | lastName  | user        |
    | email     | ittestuser2@opennms.com|
    | roles.size() | 1                |
    | roles[0] | user             |
    Then User can update a user account
    """json
    {   "username": "it-test-user2",
        "firstName": "test-updated",
        "lastName": "user-updated",
        "email": "ittestuser-updated@opennms.com",
        "roles": ["user", "admin"]
    }
    """
    Then User can remove roles from user account
    Then User can assign role "user" to a user
    Then User can reset password for another user with new password "newPassword123"
    Then User can delete a user
    Then User can update his own account
     """json
      {
        "username": "admin-user",
        "firstName": "Admin",
        "lastName": "User",
        "email": "adminuser-updated@opennms.com",
        "roles": ["admin"]
     }
     """
    Then User can reset password to "newPassword123" for his own account
    Then User can't change roles for his account
    """json
      {
        "username": "admin-user",
        "firstName": "Admin",
        "lastName": "User",
        "email": "admintuser-updated@opennms.com",
        "roles": ["admin, user"]
     }
     """
    Then User can't delete his own account



