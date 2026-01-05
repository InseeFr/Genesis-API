Feature: Schedule Kraftwerk executions for Bangles
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

  Scenario Outline: Delete schedule (non existant context)
    When We delete the schedules of "<PartitionId>"
    Then The context controller response should have a 404 status code

    Examples:
    | PartitionId |
    | NONEXISTANT |

  Scenario Outline: Delete schedule (existant context)
    Given We have a context in database with partition "<PartitionId>" and 1 valid schedule(s)
    When We delete the schedules of "<PartitionId>"
    Then The context of "<PartitionId>" should have 0 schedules

  Examples:
    | PartitionId |
    | PARTITION1  |

  Scenario Outline: Delete multiple schedules
  Given We have a context in database with partition "<PartitionId>" and 3 valid schedule(s)
  When We delete the schedules of "<PartitionId>"
  Then The context of "<PartitionId>" should have 0 schedules

  Examples:
    | PartitionId |
    | PARTITION1  |

  Scenario Outline: Get all schedules
    Given We have a context in database with partition "PARTITION1" and 1 valid schedule(s)
    And We have a context in database with partition "PARTITION2" and 1 valid schedule(s)
    And We have a context in database with partition "PARTITION3" and 1 valid schedule(s)
    When We get all the schedules
    Then The get all schedules should have 3 schedules, 1 per partition
    Examples:

  Scenario Outline: Delete expired schedules
    Given We have a context in database with partition "PARTITION1" and 1 valid schedule(s)
    And We have a context in database with partition "PARTITION2" and 1 expired schedule(s)
    And We have a context in database with partition "PARTITION3" and 1 valid schedule(s)
    When We delete the expired schedules
    Then The get all schedules should have 2 schedules and no schedule for "PARTITION2"
    Examples:
