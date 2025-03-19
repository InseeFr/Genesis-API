Feature: Do we extract data ?
  Everybody wants to know if we extract data correctly

  Scenario Outline: Survey Unit data extraction volumetry
    Given We have data in directory "<CampaignId>"
    When We save data from that directory
    And We extract survey unit data with questionnaireId "<CampaignId>" and interrogationId "<InterrogationId>"
    Then The extracted survey unit data response should have a survey unit model with interrogationId "<InterrogationId>"
    And The extracted survey unit data response should have a survey unit model for interrogationId "<InterrogationId>" with <ExpectedCollectedVariablesCount> collected variables
    And The extracted survey unit data response should have a survey unit model for interrogationId "<InterrogationId>" with <ExpectedExternalVariablesCount> external variables
    Examples:
      | CampaignId       | InterrogationId  | ExpectedCollectedVariablesCount   | ExpectedExternalVariablesCount    |
      | TEST-TABLEAUX    | AUTO11000        | 49                                | 4                                 |


  Scenario Outline: Survey Unit latest states extraction
    Given We have data in directory "<CampaignId>"
    When We save data from that directory
    And We extract survey unit latest states with questionnaireId "<CampaignId>" and interrogationId "<InterrogationId>"
    And The extracted survey unit latest states response should have a survey unit DTO has interrogationId "<InterrogationId>" with <ExpectedCollectedVariablesCount> collected variables
    And The extracted survey unit latest states response should have a survey unit DTO has interrogationId "<InterrogationId>" with <ExpectedExternalVariablesCount> external variables
    Examples:
      | CampaignId       | InterrogationId  | ExpectedCollectedVariablesCount   | ExpectedExternalVariablesCount    |
      | TEST-TABLEAUX    | AUTO11000        | 49                                | 4                                 |