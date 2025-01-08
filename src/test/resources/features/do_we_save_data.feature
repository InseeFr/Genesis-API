Feature: Do we save data ?
  Everybody wants to know if we save data correctly

  Scenario Outline: Save all data from a folder
    Given We have data in directory "SAMPLETEST"
    When We copy data file "data_backup/data.complete.validated.STPDv1.2.xml" to that directory
    When We copy data file "data_backup/data.complete.partial.STPDv1.20231122164209.xml" to that directory
    When We copy data file "data_backup/data.complete.validated.STPDv1.20231122164209.xml" to that directory
    When We save data from that directory
    When We delete that directory

    Then There should be <ExpectedCount> "<ExpectedStatus>" SurveyUnit in database
    Examples:
      | ExpectedCount | ExpectedStatus |
      | 2             | COLLECTED      |