Feature: ICP Grpc client server communication

  Scenario: Test Minion client can communicate to server
    Given identity id "12345" location "cloud" type "new_type"
    Given server host "localhost" port in system property "grpc.server.port"
    Given client retry times 5
    Then initial MinionGrpc client
    Then check minion grpc client is connected

