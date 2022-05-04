Feature: Hello World

  Scenario: Say Hello
    Given horizon stream server base url in environment variable "HORIZON_STREAM_BASE_URL"
    Then send GET request to horizon-stream at path "/events/count"
    Then verify HTTP response code = 200