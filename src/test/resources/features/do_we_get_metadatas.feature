Feature: Do we get variable types ?

  Scenario Outline: Get variable types from database
    Given We import metadata in database from "<Directory>" spec folder
    When We get metadata from database
    Then There should be a variable "<VariableName>" with type "<VariableType>"
    Examples:
      |Directory  | VariableName | VariableType   |
      |SAMPLETEST | PRENOMREP    | STRING         |
      |SAMPLETEST | AGE          | NUMBER         |