Feature: OpenNMS OpenAPI Specification export via


  Background:
    Given DB url in system property "database.url"
    Given DB username "postgres" and password "ignored"
    Given keycloak server URL in system property "keycloak.url"
    Given keycloak test user "test-user" with password "passw0rd"
    Given keycloak test realm "opennms"
    Given application base url in system property "application.base-url"

  Scenario: Verify the Top-Level details in the OpenAPI Specification
    Given JSON accept encoding
    Then login test user with keycloak
    Then send GET request at path "/openapi.json" with retry timeout 20000
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      | openapi == 3.0.1                    |
      | info.title == OpenNMS Rest Services |

  Scenario: Verify the Alarms Rest Service entry in the OpenAPI Specification
    Given JSON accept encoding
    Then login test user with keycloak
    Then send GET request at path "/openapi.json" with retry timeout 20000
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      | paths["/alarms/list"]["get"]["operationId"] == getAlarms                                                       |
      | paths["/alarms/list"]["get"]["responses"]["default"]["description"] == Retrieve the list of alarms             |
      | paths["/alarms/{id}/memo"]["put"]["operationId"] == updateMemo                                                 |
      | paths["/alarms/{id}/memo"]["put"]["responses"]["default"]["description"] == Update the memo for an Alarm       |
      | paths["/alarms/{id}/memo"]["delete"]["operationId"] == removeMemo                                              |
      | paths["/alarms/{id}/memo"]["delete"]["responses"]["default"]["description"] == Remove the memo for an Alarm    |
      | paths["/alarms/{id}/journal"]["put"]["operationId"] == updateJournal                                           |
      | paths["/alarms/{id}/journal"]["put"]["responses"]["default"]["description"] == Update the journal for an Alarm |

  Scenario: Verify the Events Rest Service entry in the OpenAPI Specification
    Given JSON accept encoding
    Then login test user with keycloak
    Then send GET request at path "/openapi.json" with retry timeout 20000
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      | paths["/events/count"]["get"]["operationId"] == getCount                                                                                 |
      | paths["/events/count"]["get"]["responses"]["default"]["description"] == Retrieve the count of events                                     |
      | paths["/events/{eventId}"]["get"]["operationId"] == getEvent                                                                             |
      | paths["/events/{eventId}"]["get"]["responses"]["default"]["description"] == Retrieve an event by ID                                      |
      | paths["/events/{eventId}"]["put"]["operationId"] == updateEvent                                                                          |
      | paths["/events/{eventId}"]["put"]["responses"]["default"]["description"] ==  Update the ACK state of an event                            |
      | paths["/events"]["get"]["operationId"] == getEvents                                                                                      |
      | paths["/events"]["get"]["responses"]["default"]["description"] ==  Retrieve a list of events                                             |
      | paths["/events"]["put"]["operationId"] == updateEvents                                                                                   |
      | paths["/events"]["put"]["responses"]["default"]["description"] ==  Update the ACK state of events that match filter/query criteria given |
      | paths["/events"]["post"]["operationId"] == publishEvent                                                                                  |
      | paths["/events"]["post"]["responses"]["default"]["description"] ==   Publish a new event                                                 |
      | paths["/events/between"]["get"]["operationId"] == getEventsBetween                                                                       |
      | paths["/events/between"]["get"]["responses"]["default"]["description"] == Retrieve a list of events in the specified date range          |

