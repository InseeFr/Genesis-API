# Changelog

## 1.11.0 [2025-10-20]
### Added
- Storage of the date of the last json extraction for a survey and a mode

## 1.10.0 [2025-10-16]
### Added
- Get review indicator endpoint
- Survey unit id export
### Fixed
- Cache eviction not working when delete questionnaire metadata

## 1.9.0 [2025-10-09]
### Added
- Authorizations for scheduled Perret calls
- Get questionnaires by review activated or not endpoint
- Save all contextual previous/external variables files for one questionnaire endpoint
### Changed
- No more 400 if the previous/external file has no corresponding part (warn in log instead)
- Changed edited to contextual

## 1.8.6 [2025-09-18]
### Added
- print role authorities on endpoint for batch generic for debug purpose

## 1.8.4 [2025-09-11]
### Updated
- Sonar 5.2.0.4988
- Springdoc openapi webmvc 2.8.13
- Cucumber 7.28.2


## 1.8.3 [2025-08-28]
### Updated
- Springboot 3.5.5
- Springdoc openapi webmvc 2.8.11

### Added
- CI pipeline

## 1.8.2 [2025-08-19]
### Updated
- BPM 1.0.13
- Cucumber 7.27.2
- Pitest 1.20.2


## 1.8.1 [2025-08-18]
### Fixed
- Get unprocessed Id service gets too much data
### Updated
- Cucumber 7.27.1

## 1.8.0 [2025-08-07]
### Added
- Metadata endpoints
- Save and cache metadata after loading

## 1.7.2 [2025-08-07]
### Updated
- BPM 1.0.12

## 1.7.1 [2025-07-31]
### Updated
- BPM 1.0.9

## 1.7.0 [2025-07-31]
### Added
- Back office user role
### Changed
- QuestionnaireIds are forced on capital case
### Fixed
- Invalid variable type in latest states call exception
### Updated
- Cucumber 7.27
- Pitest 1.20.1

## 1.6.2 [2025-07-21]
### Added
- Edited contextual variables storage

## 1.6.1 [2025-07-15]
### Added
- Extract Raw responses for Generic endpoint
- Get processed ids of last 24 hours
### Updated
- BPM 1.0.8

## 1.6.0 [2025-07-08]
### Added
- Performance optimizations
- Allow null variables for non-COLLECTED variables
- Send ids to data quality tool (Perret) during processing

## 1.5.6 [2025-06-18]
### Fixed
- fix rights on context controller

## 1.5.5 [2025-06-18]
### Added
- review indicator added in context
- change format output for quality tools (latest-states endpoint)
- new endpoint to take into account new format input ( /responses/raw/lunatic-json endpoint)

### Updated
- pitest-maven 1.19.6
- json-schema-validator 1.5.7
- springdoc 2.8.9
- springboot 3.5.0

## 1.5.4 [2025-04-24]
### Fixed
- kraftwerk users can create and delete schedules

## 1.5.3 [2025-04-24]
### Added
- Add transformation from raw data to Genesis model
- Save lunatic models in Genesis
- Retrieve lunatic models from Genesis

### Fixed
- Allow scheduler to get all schedules and raw data dtos

### Updated
- jacoco-maven-plugin 0.8.13
- pitest-maven 1.19.1
- eclipse-temurin docker tag to v21.0.7_6-jre-alpine


## 1.5.2 [2025-03-26]
### Added
- Add volumetrics to quantify raw data received

## 1.5.1 [2025-03-25]

### Fixed
- Bug with scheduler : add role for it
### Updated
- springboot 3.4.4
- springdoc-openapi-starter-webmvc-ui 2.8.6


## 1.5.0 [2025-03-20]

### Added
- Role based authorization to endpoints
- Refactor of raw data save endpoint

## 1.4.1 [2025-02-24]

### Fixed
- Bug on missing/filter_result variables in loops/tables incorrectly persisted in database

## 1.4.0 [2025-02-18]

### Changed
- Rename all identifiers and refactor values
### Fixed
- Bug on export of data to Kraftwerk
### Updated
- pitest 1.18.2
- springdoc 2.8.5
- cucumber 7.21.1
  
## 1.3.2 [2025-01-31]

### Added
- External variables in loops/tables are now correctly persisted
### Fixed
- Sequential data parser bug
### Updated
- springdoc 2.8.4

## 1.3.1 [2025-01-24]

### Added
- New endpoint to save raw data from questionnaire directly in MongoDB
- New endpoint to save data send by external quality tools
- New endpoint to save infos on a rundeck execution
- Auto creation of indexes

### Updated
- spring boot 3.4.2
- springdoc 2.8.3
- pitest 1.17.4
- bpm 1.0.5

## 1.3.0 [2024-12-16]

### Added
- Authentication on endpoints (except healthchecks)

### Fixed
- Resolve bug that makes extractions of values from loop and dynamic tables incomplete

### Updated
- spring boot 3.4.0
- springdoc 2.7.0
- jackson 2.18.2
- pitest 1.17.2

## 1.2.16 [2024-11-15]

### Fixed
- Null collected/external variables list in model does not throw exception

### Updated
- Mapstruct, pitest, sonar

## 1.2.15 - [2024-10-29]
### Added
- New endpoint to get latest data for each state (Perret needed)

### Fixed
- Rename endpoints for responses
- Fixed date format
- Improve log and manage error

### Updated
- Cucumber, coverall
- Springboot 3.3.5
- Jackson 2.18.1
- BPM 1.0.3

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
