Feature: Do we extract data ?
  Everybody wants to know if we extract data correctly

  Scenario Outline: Collected data extraction (COLLECTED only)
    Given We have data in directory "SAMPLETEST-PARADATA-v1"
    When We create DTOs from file "reponse-platine/data.complete.validated.STPDv1.20231122164209.xml" with DDI "ddi-SAMPLETEST-PARADATA-v1.xml"
    Then For SurveyUnit "<SurveyUnitId>" there should be at least one "<ExpectedStatus>" SurveyUnit DTO
    Examples:
      | SurveyUnitId | ExpectedStatus |
      | 0000007      | COLLECTED      |

  Scenario Outline: Collected data extraction (COLLECTED only)
    Given We have data in directory "SAMPLETEST-PARADATA-v1"
    When We create DTOs from file "reponse-platine/data.complete.validated.STPDv1.20231122164209.xml" with DDI "ddi-SAMPLETEST-PARADATA-v1.xml"
    Then For SurveyUnit "<SurveyUnitId>" there shouldn't be a "<UnexpectedStatus>" SurveyUnit DTO
    Examples:
      | SurveyUnitId | UnexpectedStatus |
      | 0000007      | EDITED           |
      | 0000007      | FORCED           |
      | 0000007      | INPUTED          |
      | 0000007      | PREVIOUS         |



    Scenario Outline: Collected data extraction (all states)
      Given We have data in directory "SAMPLETEST-PARADATA-v2"
      When We create DTOs from file "reponse-platine/data.complete.validated.STPDv2.20231122164209.xml" with DDI "ddi-SAMPLETEST-PARADATA-v2.xml"
      Then For SurveyUnit "<SurveyUnitId>" there should be at least one "<ExpectedStatus>" SurveyUnit DTO
      Examples:
        | SurveyUnitId | ExpectedStatus |
        | 0000007      | COLLECTED      |
        | 0000007      | EDITED         |
        | 0000007      | FORCED         |
        | 0000007      | INPUTED        |
        | 0000007      | PREVIOUS       |

  Scenario Outline: Collected data extraction content
    Given We have data in directory "<Directory>"
    When We create DTOs from file "<FileName>" with DDI "<DDIFileName>"
    Then We should have a "<ExpectedDataState>" DTO for survey unit "<SurveyUnitId>" with "<VariableName>" filled with "<ExpectedValue>" at index <ExpectedIndex>
    Examples:
      | Directory              | FileName                                                          | DDIFileName                   |  ExpectedDataState | SurveyUnitId | VariableName   | ExpectedValue  | ExpectedIndex |
      | SAMPLETEST-PARADATA-v1 | reponse-platine/data.complete.validated.STPDv1.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v1.xml | COLLECTED          | 0000007      | PRENOM_C       | TESTPRENOM7    | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | COLLECTED          | 0000007      | PRENOM_C       | TESTPRENOM7    | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | EDITED             | 0000007      | SANTE_ENFLOG71 | TESTSANTE7E    | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | INPUTED            | 0000007      | PETIT_ENF      | TESTPETITENF7I | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | FORCED             | 0000007      | LANGUE2_ENTOU  | FR             | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | PREVIOUS           | 0000007      | AVIS_FILTRE    | 2              | 1             |


  Scenario Outline: External data extraction
    Given We have data in directory "<Directory>"
    When We create DTOs from file "<FileName>" with DDI "<DDIFileName>"
    Then We should have <ExpectedNumberOfValues> values for external variable "<ExternalVariableName>" for survey unit "<SurveyUnitId>"
    And For external variable "<ExternalVariableName>" in survey unit "<SurveyUnitId>" we should have "<ExpectedValue>" as value number <ExpectedValueIndex>
    Examples:
      | Directory              | FileName                                                          | DDIFileName                   | ExpectedNumberOfValues | ExternalVariableName | SurveyUnitId | ExpectedValue    | ExpectedValueIndex |
      | SAMPLETEST-PARADATA-v1 | reponse-platine/data.complete.validated.STPDv1.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v1.xml | 1                      | RPANAISCONJ          | 0000007      | 1986             | 1                  |
      | SAMPLETEST-PARADATA-v1 | reponse-platine/data.complete.validated.STPDv1.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v1.xml | 2                      | RPPRENOM             | 0000007      | TESTRPRENOM7_2   | 1                  |
      | SAMPLETEST-PARADATA-v1 | reponse-platine/data.complete.validated.STPDv1.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v1.xml | 2                      | RPPRENOM             | 0000007      | TESTRPRENOM7PAR1 | 2                  |
