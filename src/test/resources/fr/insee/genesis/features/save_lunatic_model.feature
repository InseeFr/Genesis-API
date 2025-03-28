Feature: Save lunatic model in genesis
  Scenario Outline: Lunatic model saving
    Given We have a lunatic model json file in spec folder "<CampaignId>"
    When We save that lunatic model json file with questionnaire id "<QuestionnaireId>"
    Then We should have a document with id "<QuestionnaireId>" and the contents from the body
    Examples:
    | CampaignId           | QuestionnaireId           |
    | LUNATIC-TEST         | LUNATIC-TEST_QUEST        |
    | RAWDATATESTCAMPAIGN  | RAWDATATESTCAMPAIGN_QUEST |

  Scenario Outline: Lunatic model update
    Given We have a lunatic model in database with questionnaire id "<QuestionnaireId>" from the json of "<CampaignId1>"
    And We have a lunatic model json file in spec folder "<CampaignId2>"
    When We save that lunatic model json file with questionnaire id "<QuestionnaireId>"
    Then We should have a document with id "<QuestionnaireId>" and the contents from the body
    Examples:
      | CampaignId1           | CampaignId2         | QuestionnaireId           |
      | LUNATIC-TEST          | RAWDATATESTCAMPAIGN | LUNATIC-TEST_QUEST        |
      | RAWDATATESTCAMPAIGN   | LUNATIC-TEST        | RAWDATATESTCAMPAIGN_QUEST |