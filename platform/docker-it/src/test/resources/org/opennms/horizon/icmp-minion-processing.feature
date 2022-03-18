Feature: ICMP minion processing

  Scenario: Test ping command in the container
    Given SSH username "admin" and password "admin"
    Given host "localhost" and port in system property "horizon.ssh.port"
    Then ping the mock minion ICMP server via ssh connection
    Then verify the response matches "^(PING: \/%s) \d+.\d+ ms$"