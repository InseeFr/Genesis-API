Feature: Do we extract data ?
  Everybody wants to know if we extract data correctly

  Scenario Outline: Collected data extraction (COLLECTED only)
    Given We have data in directory "SAMPLETEST-PARADATA-v1"
    When We create survey unit models from file "reponse-platine/data.complete.validated.STPDv1.20231122164209.xml" with DDI "ddi-SAMPLETEST-PARADATA-v1.xml"
    Then For SurveyUnit "<InterrogationId>" there should be at least one "<ExpectedStatus>" SurveyUnit Model
    Examples:
      | InterrogationId | ExpectedStatus |
      | 0000007      | COLLECTED      |

  Scenario Outline: Collected data extraction (COLLECTED only)
    Given We have data in directory "SAMPLETEST-PARADATA-v1"
    When We create survey unit models from file "reponse-platine/data.complete.validated.STPDv1.20231122164209.xml" with DDI "ddi-SAMPLETEST-PARADATA-v1.xml"
    Then For SurveyUnit "<InterrogationId>" there shouldn't be a "<UnexpectedStatus>" SurveyUnit Model
    Examples:
      | InterrogationId | UnexpectedStatus |
      | 0000007      | EDITED           |
      | 0000007      | FORCED           |
      | 0000007      | INPUTED          |
      | 0000007      | PREVIOUS         |



    Scenario Outline: Collected data extraction (all states)
      Given We have data in directory "SAMPLETEST-PARADATA-v2"
      When We create survey unit models from file "reponse-platine/data.complete.validated.STPDv2.20231122164209.xml" with DDI "ddi-SAMPLETEST-PARADATA-v2.xml"
      Then For SurveyUnit "<InterrogationId>" there should be at least one "<ExpectedStatus>" SurveyUnit Model
      Examples:
        | InterrogationId | ExpectedStatus |
        | 0000007      | COLLECTED      |
        | 0000007      | EDITED         |
        | 0000007      | FORCED         |
        | 0000007      | INPUTED        |
        | 0000007      | PREVIOUS       |

  Scenario Outline: Collected data extraction content
    Given We have data in directory "<Directory>"
    When We create survey unit models from file "<FileName>" with DDI "<DDIFileName>"
    Then We should have a "<ExpectedDataState>" Survey Unit model with id "<InterrogationId>" with "<VariableName>" filled with "<ExpectedValue>" at index <ExpectedIndex>
    Examples:
      | Directory              | FileName                                                          | DDIFileName                   |  ExpectedDataState | InterrogationId | VariableName   | ExpectedValue  | ExpectedIndex |
      | SAMPLETEST-PARADATA-v1 | reponse-platine/data.complete.validated.STPDv1.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v1.xml | COLLECTED          | 0000007      | PRENOM_C       | TESTPRENOM7    | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | COLLECTED          | 0000007      | PRENOM_C       | TESTPRENOM7    | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | EDITED             | 0000007      | SANTE_ENFLOG71 | TESTSANTE7E    | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | INPUTED            | 0000007      | PETIT_ENF      | TESTPETITENF7I | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | FORCED             | 0000007      | LANGUE2_ENTOU  | FR             | 0             |
      | SAMPLETEST-PARADATA-v2 | reponse-platine/data.complete.validated.STPDv2.20231122164209.xml |ddi-SAMPLETEST-PARADATA-v2.xml | PREVIOUS           | 0000007      | AVIS_FILTRE    | 1              | 0             |


  Scenario Outline: External data extraction
    Given We have data in directory "<Directory>"
    Given We copy data file "data_backup/data.complete.validated.STPDv1.20231122164209.xml" to that directory
    When We save data from that directory
    Then We should have 1 values for external variable "<ExternalVariableName>" for survey unit "<SurveyUnitId>"
    And For external variable "<ExternalVariableName>" in survey unit "<SurveyUnitId>" we should have "<ExpectedValue>" and loopId "<LoopId>"
    Examples:
      | Directory              | ExternalVariableName | LoopId        | SurveyUnitId | ExpectedValue    |
      | SAMPLETEST             | RPANAISCONJ          | B_PRENOMREP_1 | 0000007      | 1986             |
      | SAMPLETEST             | RPPRENOM             | B_PRENOMREP_1 | 0000007      | TESTRPRENOM7_2   |
      | SAMPLETEST             | RPPRENOM             | B_PRENOMREP_2 | 0000007      | TESTRPRENOM7PAR1 |
