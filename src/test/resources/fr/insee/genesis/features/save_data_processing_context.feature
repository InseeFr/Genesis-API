Feature: Create data processing context
  Scenario Outline: Save data processing context (non existant)
    When We save data processing context for partition "<PartitionId>" and review indicator to "<withReview>"
    Then We should have one context document for partition "<PartitionId>"
    And Review indicator should be "<withReview>"

    Examples:
      | PartitionId | withReview |
      | PARTITION1  | false      |
      | PARTITION2  | true       |

  Scenario Outline: Save data processing context (non existant, no withReview)
    When We save data processing context for partition "<PartitionId>"
    Then We should have one context document for partition "<PartitionId>"
    And Review indicator should be "false"

    Examples:
      | PartitionId |
      | PARTITION1  |

  Scenario Outline: Save data processing context (existant)
    Given We have a context in database with partition "<PartitionId>"
    When We save data processing context for partition "<PartitionId>" and review indicator to "<withReview>"
    Then We should have one context document for partition "<PartitionId>"
    And Review indicator should be "<withReview>"

    Examples:
      | PartitionId | withReview |
      | PARTITION1  | false      |
      | PARTITION2  | true       |