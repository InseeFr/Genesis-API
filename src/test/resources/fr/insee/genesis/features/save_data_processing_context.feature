Feature: Create data processing context
  Scenario Outline: Save data processing context (non existant)
    When We save data processing context for partition "<PartitionId>"
    Then We should have a context document for partition "<PartitionId>"
    And Review indicator should be "false"

    Examples:
      | PartitionId |
      | PARTITION1  |
      | PARTITION2  |


  Scenario Outline: Save data processing context (existant)
    Given We have a context in database with partition "<PartitionId>"
    When We save data processing context for partition "<PartitionId>"
    Then Save data processing response should have a 208 status code

    Examples:
      | PartitionId |
      | PARTITION1  |
      | PARTITION2  |

  Scenario Outline: Set withReview
    Given We have a context in database with partition "<PartitionId>" and review indicator to "<WithReview>"
    When We set withReview to "<NewWithReview>"
    Then Review indicator should be "<NewWithReview>"

    Examples:
      | PartitionId | WithReview | NewWithReview |
      | PARTITION1  | false      | true          |
      | PARTITION2  | true       | false         |
      | PARTITION3  | true       | true          |

  Scenario Outline: Save schedule (non existant context)
    When We save a new kraftwerk schedule for partition "<PartitionId>", frequency "<Cron>" and service to call "<ServiceToCall>", with beginning date at "2024-01-01T12:00:00" and ending at "2999-01-01T12:00:00"
    Then We should have a context document for partition "<PartitionId>" containing a kraftwerk schedule with frequency "<Cron>" and service to call "<ServiceToCall>", with beginning date at "2024-01-01T12:00:00" and ending at "2999-01-01T12:00:00"

    Examples:
      | PartitionId | Cron        | ServiceToCall |
      | PARTITION1  | 0 0 1 * * * | GENESIS       |
      | PARTITION2  | 0 0 2 * * * | MAIN          |

  Scenario Outline: Save schedule (existant context)
    Given We have a context in database with partition "<PartitionId>"
    When We save a new kraftwerk schedule for partition "<PartitionId>", frequency "<Cron>" and service to call "<ServiceToCall>", with beginning date at "2024-01-01T12:00:00" and ending at "2999-01-01T12:00:00"
    Then We should have a context document for partition "<PartitionId>" containing a kraftwerk schedule with frequency "<Cron>" and service to call "<ServiceToCall>", with beginning date at "2024-01-01T12:00:00" and ending at "2999-01-01T12:00:00"

    Examples:
      | PartitionId | Cron        | ServiceToCall |
      | PARTITION1  | 0 0 1 * * * | GENESIS       |
      | PARTITION2  | 0 0 2 * * * | MAIN          |

