Feature: Do we save raw data in genesis
  Scenario Outline: Correct JSON raw data import volumetry
    Given We have raw data file in "<JsonFile>"
    When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    Then We should have <ExpectedDocumentCount> raw data document
    And We should have <ExpectedCollectedVariablesCount> collected variables and <ExpectedExternalVariablesCount> external variables for campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    Examples:
    | JsonFile                    | CampaignId     | QuestionnaireId | InterrogationId | ExpectedDocumentCount | ExpectedCollectedVariablesCount | ExpectedExternalVariablesCount |
    | raw_data/rawdatasample.json | TESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | 1                     | 694                             | 21                             |

  Scenario Outline: Correct JSON raw data import content (value array)
    Given We have raw data file in "<JsonFile>"
    When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    Then For "<CollectedVariableName>" collected variable, state "<DataState>", campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>" we should have "<ExpectedCollectedValue>" as value number <ExpectedValueIndex> in array
    And For "<ExternalVariableName>" external variable, campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>" we should have "<ExpectedExternalValue>" as value number <ExpectedValueIndex> in array
    Examples:
      | JsonFile                    | CampaignId     | QuestionnaireId | InterrogationId | CollectedVariableName | DataState | ExpectedCollectedValue | ExpectedValueIndex | ExternalVariableName | ExpectedExternalValue |
      | raw_data/rawdatasample.json | TESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | PRENOM_PAR1           | COLLECTED | Farid                  | 0                  | RPPRENOM             | Robert                |

  Scenario Outline: Correct JSON raw data import content (value)
    Given We have raw data file in "<JsonFile>"
    When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    Then For "<ExternalVariableName>" external variable, campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>" we should have "<ExpectedExternalValue>" as value
    Examples:
      | JsonFile                    | CampaignId     | QuestionnaireId | InterrogationId | ExternalVariableName | ExpectedExternalValue |
      | raw_data/rawdatasample.json | TESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | TYPE_QUEST           | 2                     |

  Scenario Outline: Wrong JSON raw data import
    Given We have raw data file in "<JsonFile>"
    When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    Then We should have <ExpectedStatusCode> status code
    And We should have <ExpectedDocumentCount> raw data document
    Examples:
      | JsonFile                                 | CampaignId     | QuestionnaireId | InterrogationId | ExpectedStatusCode | ExpectedDocumentCount |
      | raw_data/rawdatasample_syntax_error.json | TESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | 400                | 0                     |

