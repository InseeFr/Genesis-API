Feature: Get lunatic models and their questionnaireIds from genesis
  Scenario Outline: Get lunatic model
    Given We have a lunatic model in database with questionnaire id "<QuestionnaireId>" from the json of "<CampaignId>"
    When We get lunatic model for questionnaire "<QuestionnaireId>"
    Then We should have that lunatic model as response
    Examples:
      | CampaignId    | QuestionnaireId    |
      | LUNATIC-TEST  | LUNATIC-TEST_QUEST |

  Scenario Outline: Get questionnaireId (No response in base)
    Given We have a lunatic model in database with questionnaire id "<QuestionnaireId>" from the json of "<CampaignId>"
    When We get questionnaire id for interrogation "<InterrogationId>"
    Then We should have a 404 error code
    Examples:
      | CampaignId    | QuestionnaireId    |  InterrogationId |
      | LUNATIC-TEST  | LUNATIC-TEST_QUEST |  UE1             |

  Scenario Outline: Get questionnaireId
    Given We have a lunatic model in database with questionnaire id "<QuestionnaireId>" from the json of "<CampaignId>"
    And We have a response in database with campaign id "<CampaignId>", questionnaire id "<QuestionnaireId>" and interrogation id "<InterrogationId>"
    When We get questionnaire id for interrogation "<InterrogationId>"
    Then We should have "<QuestionnaireId>" as response
    Examples:
      | CampaignId    | QuestionnaireId    |  InterrogationId |
      | LUNATIC-TEST  | LUNATIC-TEST_QUEST |  UE1             |

  Scenario Outline: Get questionnaireId (multiple questionnaires for same interrogation)
    Given We have a response in database with campaign id "<CampaignId1>", questionnaire id "<QuestionnaireId1>" and interrogation id "<InterrogationId>"
    And We have a response in database with campaign id "<CampaignId2>", questionnaire id "<QuestionnaireId2>" and interrogation id "<InterrogationId>"
    When We get questionnaire id for interrogation "<InterrogationId>"
    Then We should have a 207 error code
    Examples:
      | CampaignId1   | CampaignId2          | QuestionnaireId1    | QuestionnaireId2         |  InterrogationId |
      | LUNATIC-TEST  | RAWDATATESTCAMPAIGN  | LUNATIC-TEST_QUEST | RAWDATATESTCAMPAIGN_QUEST |  UE1             |