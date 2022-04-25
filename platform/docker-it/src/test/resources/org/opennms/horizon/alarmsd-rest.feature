Feature: OpenNMS Alarm Daemon Rest

  Background:
    Given DB url in system property "database.url"
    Given DB username "postgres" and password "ignored"
    Given keycloak server URL in system property "keycloak.url"
    Given keycloak admin user "keycloak-admin" with password "admin"
    Given keycloak test user "test-user" with password "passw0rd"
    Given keycloak test realm "opennms"
    Given application base url in system property "application.base-url"

  Scenario: Add SENTINEL monitoring system to the database
    Then execute SQL statement "insert into monitoringsystems (id,location,type) VALUES ('00000000-0000-0000-0000-000000ddba11', 'SENTINEL', 'System')"

  Scenario: Setup KEYCLOAK for testing
    Then login admin user with keycloak
    Then add keycloak realm "opennms"
    Then add keycloak user "test-user" with password "passw0rd" in realm "opennms"
    Then add keycloak user "noperm-user" with password "passw0rd" in realm "opennms"
    Then create role "admin" in realm "opennms"
    Then assign role "admin" to keycloak user "test-user" in realm "opennms"

#  Scenario: Login the test user

# TODO: enable once events API is available
  Scenario: Ensure Events endpoints are reachable
    Then send GET request at path "/events/count" with retry timeout 20000

  Scenario: Admin user request /alarms/list endpoint with JSON
    Given JSON accept encoding
    Then login test user with keycloak
    Then send GET request at path "/alarms/list" with retry timeout 20000
    Then verify the response code 200 was returned
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      | totalCount == 0 |

  Scenario: Admin user request /events endpoint with JSON
    Given JSON accept encoding
    Given XML content type
    Given POST request body in resource "test-data/event001.xml"
    Then login test user with keycloak
    Then send POST request at path "/events"
    Then verify the response code 202 was returned
    Then request non-empty alarm list with retry timeout 20000
    Then verify the response code 200 was returned
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      | totalCount == 1                                                       |
      | alarm[0].uei == uei.opennms.org/alarms/trigger                        |
      | alarm[0].logMessage == A problem has been triggered on //x-service-x. |
      | alarm[0].lastEvent.source == x-source-x                               |

  Scenario: MISSING CREDENTIALS on the /events endpoint
    Then send GET request at path "/events/count" with retry timeout 20000
    Then verify the response code 401 was returned

  Scenario: User lacks permissions on the /events endpoint
    Given keycloak test user "noperm-user" with password "passw0rd"
    Given keycloak test realm "opennms"
    Then login test user with keycloak
    Then send GET request at path "/events" with retry timeout 20000
    Then verify the response code 403 was returned

