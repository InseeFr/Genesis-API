Feature: Raw data processing
 Scenario Outline: Raw data processing
  Given We have raw data file in "<JsonFile>"
  When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
  And We process raw data for campaign "<CampaignId>", questionnaire "<QuestionnaireId>" and interrogation "<InterrogationId>"
  Then For collected variable "<CollectedVariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedCollectedValue>" for iteration 1
  And For external variable "<ExternalVariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedExternalValue>" for iteration 1
  Examples:
   | JsonFile                                  | CampaignId            | QuestionnaireId | InterrogationId | CollectedVariableName  | ExpectedCollectedValue | ExternalVariableName | ExpectedExternalValue |
   | raw_data/rawdatasample.json               | RAWDATATESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | PRENOM_PAR1            | Farid                  | RPPRENOM             | Robert                |
   | raw_data/rawdatasample_filieremodel.json  | RAWDATATESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | PRENOM_PAR1            | Farid                  | RPPRENOM             | Robert                |

  Scenario Outline: Raw data processing with optional variables
    Given We have raw data file in "<JsonFile>"
    When We save that raw data with validation
    And We process raw data for campaign "<CampaignId>", questionnaire "<QuestionnaireId>" and interrogation "<InterrogationId>"
    Then For collected variable "<CollectedVariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedCollectedValue>" for iteration 1
    And For external variable "<ExternalVariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedExternalValue>" for iteration 1
    And In surveyUnit "<InterrogationId>" of the campaign "<CampaignId>" we must have "TESTCONTEXT" as contextualId, isCapturedIndirectly to "true" and validationDate null
    Examples:
      | JsonFile                                            | CampaignId            | QuestionnaireId | InterrogationId | CollectedVariableName  | ExpectedCollectedValue | ExternalVariableName | ExpectedExternalValue |
      | raw_data/rawdatasample_filieremodel_optionals.json  | RAWDATATESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | PRENOM_PAR1            | Farid                  | RPPRENOM             | Robert                |


  @NeedsLogPrepare
 Scenario Outline: Raw data processing with correct Json but invalid data
   Given We have raw data file in "<JsonFile>"
   When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
   And We process raw data for campaign "<CampaignId>", questionnaire "<QuestionnaireId>" and interrogation "<InterrogationId>"
   Then We shouldn't have any response for campaign "<CampaignId>"
   And We should have "No collected data for interrogation" in the logs
   And We should have "No collected or external variable for interrogation" in the logs
   Examples:
   | JsonFile                                  | CampaignId            | QuestionnaireId | InterrogationId |
   | raw_data/invalidData_but_correctJson.json | RAWDATATESTCAMPAIGN   | TESTQUEST       | TESTUE00001     |

  @NeedsLogPrepare
  Scenario Outline: Raw data processing with correct Json but no collected
    Given We have raw data file in "<JsonFile>"
    When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    And We process raw data for campaign "<CampaignId>", questionnaire "<QuestionnaireId>" and interrogation "<InterrogationId>"
    Then We shouldn't have any response for campaign "<CampaignId>"
    And We should have "No collected data for interrogation" in the logs
    And For external variable "<ExternalVariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedExternalValue>" for iteration 1
    Examples:
      | JsonFile                                  | CampaignId            | QuestionnaireId | InterrogationId | ExternalVariableName | ExpectedExternalValue |
      | raw_data/rawdatasample_no_collected.json  | RAWDATATESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | RPPRENOM             | Robert                |
