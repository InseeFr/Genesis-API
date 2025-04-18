#Feature: Raw data processing
# Scenario Outline: Raw data processing
#  Given We have raw data file in "<JsonFile>"
#  When We save that raw data for web campaign "<CampaignId>", questionnaire "<QuestionnaireId>", interrogation "<InterrogationId>"
#  When We process raw data for campaign "<CampaignId>", questionnaire "<QuestionnaireId>" and interrogation "<InterrogationId>"
#  Then For collected variable "<CollectedVariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedCollectedValue>" for iteration 1
#  And  For external variable "<ExternalVariableName>" in survey unit "<InterrogationId>" we should have "<ExpectedExternalValue>" for iteration 1
#  Examples:
#   | JsonFile                    | CampaignId            | QuestionnaireId | InterrogationId | CollectedVariableName | ExpectedCollectedValue | ExternalVariableName | ExpectedExternalValue |
#   | raw_data/rawdatasample.json | RAWDATATESTCAMPAIGN   | TESTQUEST       | TESTUE00001     | PRENOM_PAR1           | Farid                  | RPPRENOM             | Robert                |