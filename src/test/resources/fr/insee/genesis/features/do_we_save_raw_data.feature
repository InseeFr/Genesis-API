Feature: Do we save raw data in genesis
  Scenario Outline: JSON raw data import
    Given We have raw data file in "<JsonFile>"
    When We call save raw data endpoint for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    Then We should have <ExpectedStatusCode> status code
    And We should have <ExpectedDocumentCount> raw data document
    Examples:
      | JsonFile                                  | CampaignId   | QuestionnaireId | InterrogationId | ExpectedStatusCode | ExpectedDocumentCount |
      | raw_data/rawdatasample.json               | TESTCAMPAIGN | TESTQUEST       | TESTUE00001     | 201                | 1                     |
      | raw_data/rawdatasample_syntax_error.json  | TESTCAMPAIGN | TESTQUEST       | TESTUE00001     | 400                | 0                     |
      | raw_data/invalidData_but_correctJson.json | TESTCAMPAIGN | TESTQUEST       | TESTUE00001     | 201                | 1                     |
