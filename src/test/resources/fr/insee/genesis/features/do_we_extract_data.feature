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

  Scenario Outline: Survey Unit latest states extraction (withReview false)
    Given We have data in directory "<CampaignId>"
    When We save data from that directory
    And We extract survey unit latest states with questionnaireId "<CampaignId>" and interrogationId "<InterrogationId>"
    Then The response of get latest states should have 403 status code

    Examples:
      | CampaignId       | InterrogationId  |
      | TEST-TABLEAUX    | AUTO11000        |


  Scenario Outline: Survey Unit latest states extraction (withReview true)
    Given We have data in directory "<CampaignId>"
    And We have a context in database for that data with review "true"
    When We save data from that directory
    And We extract survey unit latest states with questionnaireId "<CampaignId>" and interrogationId "<InterrogationId>"
    Then The extracted survey unit latest states response should have a survey unit DTO has interrogationId "<InterrogationId>" with <ExpectedCollectedVariablesCount> collected variables
    And The extracted survey unit latest states response should have a survey unit DTO has interrogationId "<InterrogationId>" with <ExpectedExternalVariablesCount> external variables
    Examples:
      | CampaignId       | InterrogationId  | ExpectedCollectedVariablesCount   | ExpectedExternalVariablesCount    |
      | TEST-TABLEAUX    | AUTO11000        | 49                                | 4                                 |

  Scenario Outline: Multiple different Contexts for one partitionId
    Given We have data in directory "<CampaignId>"
    And We have a context in database for that data with review "true"
    And We have a context in database for that data with review "false"
    When We save data from that directory
    And We extract survey unit latest states with questionnaireId "<CampaignId>" and interrogationId "<InterrogationId>"
    Then The response of get latest states should have 403 status code

    Examples:
      | CampaignId       | InterrogationId  |
      | TEST-TABLEAUX    | AUTO11000        |

  Scenario Outline: Multiple different Contexts for one interrogationId
    Given We have data in directory "<CampaignId1>"
    And We have a context in database for that data with review "true"
    And We have a survey unit with campaignId "<CampaignId2>" and interrogationId "<InterrogationId>"
    And We have a context in database for partitionId "<CampaignId2>" with review "true"
    When We save data from that directory
    And We extract survey unit latest states with questionnaireId "<CampaignId>" and interrogationId "<InterrogationId>"
    Then The response of get latest states should have 500 status code

    Examples:
      | CampaignId1   | CampaignId2    | InterrogationId  |
      | TEST-TABLEAUX | TEST-TABLEAUX2 | AUTO11000        |