Feature: Do we save loops ?
  Everybody wants to know if we can store loop ids into database

  Scenario Outline: Loops in external variables
    Given We have data in directory "TEST-TABLEAUX"
    Given We copy data file "data_backup/data.complete.validated.TEST-TABLEAUX.xml" to that directory
    When We save data from that directory
    When We delete that directory

    Then For external variable "<VariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedValue>" and loopId "<ExpectedIdLoop>" for iteration <Iteration>
    Examples:
      | VariableName       | InterrogationId | ExpectedIdLoop | ExpectedValue | Iteration |
      | CODESA             | AUTO11000       | TABESA         | AAA           | 1         |
      | CODESA             | AUTO11000       | TABESA         | BBB           | 2         |
      | CODESA             | AUTO11000       | TABESA         | CCC           | 3         |
      | CODESA             | AUTO11000       | TABESA         | DDD           | 4         |