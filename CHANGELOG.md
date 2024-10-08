# Changelog


## 1.2.14 - [2024-10-08]

### Added
- Delete expired schedules

### Changed
- Change to hexagonal architecture

### Fixed
- Bug when no data in xml files

### Updated
- Update dependencies : mapstruct, pitest, cucumber, jackson
- Update spring-boot
- Update BPM


## 1.2.13 - [2024-09-09]
### Fixed
- Fix conflict : xslt transformation is now in BPM library

## 1.2.12 - [2024-09-06]
### Fixed
- Change to BPM version 1.0.1 (hot fix DDI Reader)

## 1.2.11 - [2024-09-05]
### Fixed
- Fixed get simplified responses POST endpoint

## 1.2.10 - [2024-08-30]
### Updated
- Update dependencies (pitest, spring-boot, mapstruct)

### Added
- Improve encryption management
- Accept data without DDI
- Read metadata throw BPM

## 1.2.9 - [2024-08-01]
### Fixed
- Wrong loop reading in data file

## 1.2.8 - [2024-07-31]
### Added
- Alternative INPUTED syntaxes (INPUTTED & IMPUTED)

### Changed
- All campaigns integration endpoint now reads one specific folder (differential/data)

### Fixed
- File in error moved to done even if there is no responses added to database
- No error log when file in error

## 1.2.7 - [2024-07-30]
### Added
- First version to manage encryption
- Export files for volumetry
- Add GET all campaigns and questionnaires endpoints

## 1.2.6 - [2024-07-11]
### Added
- Add endpoint to delete schedule

### Fixed
- Spring profile management

### Updated
- Update dependencies

## 1.2.5 - [2024-05-17]
### Added
- More explicit documentation on schedule frequency
### Fixed
- Fix last execution date not in 24h UTC format

## 1.2.4 - [2024-05-16]
### Added
- Service to set last execution date
- Service to get modes by campaign
- Remove input data file if already present in done folder to avoid duplications
- Stop treatment when no data file in folder

### Removed
- Update last execution date service

## 1.2.3 - [2024-05-03]
### Added
- Get idQuestionnaires by IdCampaign endpoint

## 1.2.2 - [2024-05-03]
### Added
- Treat all campaigns endpoint
### Fixed
- Fixed health check always returning version n.a

## 1.2.1 - [2024-04-24]
First production version

### Fixed
- Https management

## 1.2.0 - [2024-04-10]

### Added
- Bangles schedule support

## 1.1.0 - [2024-02-23]

### Fixed
- Upgrade to Java 21

### Added
- End point to delete all responses of one questionnaire

## 1.0.7 - [2024-02-13]

### Fixed
- Fix batch size when querying database in order to save RAM

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