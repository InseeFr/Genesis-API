Feature: Do we save raw data in genesis
  Scenario Outline: Correct JSON raw data import volumetry
    Given We have raw data file in "<JsonFile>"
    When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
    Then We should have <ExpectedStatusCode> status code
    And We should have <ExpectedDocumentCount> raw data document
    Examples:
      | JsonFile                                  | CampaignId   | QuestionnaireId | InterrogationId | ExpectedStatusCode | ExpectedDocumentCount |
      | raw_data/rawdatasample.json               | TESTCAMPAIGN | TESTQUEST       | TESTUE00001     | 201                | 1                     |
      | raw_data/rawdatasample_syntax_error.json  | TESTCAMPAIGN | TESTQUEST       | TESTUE00001     | 400                | 0                     |
      | raw_data/invalidData_but_correctJson.json | TESTCAMPAIGN | TESTQUEST       | TESTUE00001     | 201                | 1                     |



