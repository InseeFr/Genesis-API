Feature: Do we validate input data ?
  Everybody wants to know if we validate them beforehand

  Background:
    Given We have data in directory "WEB/TestValidation1"
    When We save responses from the file "TestValidation1.xml" with DDI "TestValidation1DDI.xml"

  Scenario Outline: Correct input data
    Then For SurveyUnit "<SurveyUnitId>" there shouldn't be a FORCED copy of the SurveyUnit
    Examples:
      | SurveyUnitId |
      | TestUE1      |

  Scenario Outline: Incorrect update variable with 1 value
    Then There is a FORCED copy of SurveyUnit "<SurveyUnitId>" without update variable "<IncorrectVariableName>"
    Examples:
      | SurveyUnitId | IncorrectVariableName |
      | TestUE2      | TestInteger           |


  Scenario Outline: Incorrect external variable with 1 value
    Then There is a FORCED copy of SurveyUnit "<SurveyUnitId>" without external variable "<IncorrectVariableName>"
    Examples:
      | SurveyUnitId | IncorrectVariableName |
      | TestUE4      | TestInteger           |

  Scenario Outline: Incorrect update variable with multiple values
    Then There is a FORCED copy of SurveyUnit "<SurveyUnitId>" with update variable "<IncorrectVariableName>" containing <EmptyValuesCount> empty values
    Examples:
      | SurveyUnitId | IncorrectVariableName | EmptyValuesCount |
      | TestUE3      | TestInteger           |1                 |


  Scenario Outline: Incorrect external variable with multiple values
    Then There is a FORCED copy of SurveyUnit "<SurveyUnitId>" with update variable "<IncorrectVariableName>" containing <EmptyValuesCount> empty values
    Examples:
      | SurveyUnitId | IncorrectVariableName | EmptyValuesCount |
      | TestUE3      | TestInteger           |1                 |