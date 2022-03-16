Feature: ICP Grpc client server communication

  #TODO: this is the initial test and will move to docker container
  Scenario: Test Minion client can communicate to server
    Given identity id "12345" location "cloud" type "new_type"
    Given server host "localhost" port 8990
    Given client retry times 5
    Then initial MinionGrpc client
    Then check minion grpc client is connected

