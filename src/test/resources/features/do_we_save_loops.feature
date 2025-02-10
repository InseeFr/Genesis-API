Feature: Do we save loops ?
  Everybody wants to know if we can store loop info into database

  Scenario Outline: Loops in collected variables
    Given We have data in directory "TEST-TABLEAUX"
    Given We copy data file "data_backup/data.complete.validated.TEST-TABLEAUX.xml" to that directory
    When We save data from that directory
    When We delete that directory

    Then For collected variable "<VariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedValue>" and scope "<ExpectedScope>" for iteration <Iteration>
    Then If we get latest states for "TEST-TABLEAUX" in collected variable "<VariableName>", survey unit "<InterrogationId>" we should have "<ExpectedValue>" for iteration <Iteration>
    Examples:
      | VariableName           | InterrogationId | ExpectedScope     | ExpectedValue        | Iteration |
      | TABESTANP1             | AUTO11000       | TABESTANP         | 38312300H0           | 1         |
      | TABESTANP1             | AUTO11000       | TABESTANP         | 39001100H0           | 2         |
      | TABESTANP1             | AUTO11000       | TABESTANP         | 3832300030           | 3         |

  Scenario Outline: Loops in external variables
    Given We have data in directory "TEST-TABLEAUX"
    Given We copy data file "data_backup/data.complete.validated.TEST-TABLEAUX.xml" to that directory
    When We save data from that directory
    When We delete that directory

    Then For external variable "<VariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedValue>" and scope "<ExpectedScope>" for iteration <Iteration>
    Then If we get latest states for "TEST-TABLEAUX" in external variable "<VariableName>", survey unit "<InterrogationId>" we should have "<ExpectedValue>" for iteration <Iteration>
    Examples:
      | VariableName       | InterrogationId | ExpectedScope  | ExpectedValue | Iteration |
      | CODESA             | AUTO11000       | TABESA         | AAA           | 1         |
      | CODESA             | AUTO11000       | TABESA         | BBB           | 2         |
      | CODESA             | AUTO11000       | TABESA         | CCC           | 3         |
      | CODESA             | AUTO11000       | TABESA         | DDD           | 4         |