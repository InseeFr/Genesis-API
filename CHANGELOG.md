# Changelog

## 1.0.6 - [2024-02-12]

### Fixed
- Refactor of code in order to upgrade RAM management when retrieving survey units from database

## 1.0.5 - [2024-02-08]

### Fixed
- Change batch size for extraction endpoint : writing 100 survey units at a time

## 1.0.4 - [2024-02-08]

### Fixed
- Missing logging info for termination of extraction endpoint

## 1.0.3 - [2024-02-08]

### Added
- End point to get all the data for a specific questionnaire written in a json file in the output directory

## 1.0.2 - [2024-01-29]

### Fixed
- Fix spam in logs

## 1.0.1 - [2024-01-26]

### Fixed
- Fix WEB mode forced when saving a single file in database
- Fix idQuestionnaire not persisted in database
- Fix issue that causes 500 error when no documents are found in database
- Fix issue with variables that are collected but not present in DDI specifications 

## 1.0.0 - [2024-01-17]
- First release