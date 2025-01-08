Feature: Do we save loops ?
  Everybody wants to know if we can store loop ids into database

  Scenario Outline: Loops in external variables
    Given We have data in directory "TEST-TABLEAUX"
    When We copy data file "data_backup/data.complete.validated.TEST-TABLEAUX.xml" to that directory
    When We save data from that directory
    When We delete that directory

    Then For external variable "<VariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedIdLoop>" as idLoop and "<ExpectedValue>" as first value
    Examples:
      | VariableName       | InterrogationId | ExpectedIdLoop | ExpectedValue |
      | CODESA             | AUTO11000       | TABESA_1       | AAA |
      | CODESA             | AUTO11000       | TABESA_2       | BBB |
      | CODESA             | AUTO11000       | TABESA_3       | CCC |
      | CODESA             | AUTO11000       | TABESA_4       | DDD |