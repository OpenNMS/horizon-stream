Feature: ICMP minion processing

  Scenario: Test ping command in the container
    #todo should be changed to admin/admin when move the container
    Given SSH username "karaf" and password "karaf"
    Given host "localhost" and port number 8101
    #Then start the mock minion ICMP server
    Then ping the mock minion ICMP server via ssh connection
    Then verify the response matches "^(PING: \/%s) \d+.\d+ ms$"