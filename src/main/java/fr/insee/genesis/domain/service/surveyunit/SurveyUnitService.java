package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.dto.*;
import fr.insee.genesis.controller.rest.responses.ApiError;
import fr.insee.genesis.controller.rest.responses.ResponseController;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataSequentialParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.controller.utils.DataTransformer;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VarIdScopeTuple;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SurveyUnitService implements SurveyUnitApiPort {

    public static final String PATH_FORMAT = "%s/%s";

    @Qualifier("surveyUnitMongoAdapter")
    private final SurveyUnitPersistencePort surveyUnitPersistencePort;

    private final MetadataService metadataService;
    private final FileUtils fileUtils;
    private final DataProcessingContextApiPort contextService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final ControllerUtils controllerUtils;
    private final AuthUtils authUtils;

    @Autowired
    public SurveyUnitService(SurveyUnitPersistencePort surveyUnitPersistencePort,
                             MetadataService metadataService,
                             FileUtils fileUtils,
                             DataProcessingContextApiPort contextService,
                             SurveyUnitQualityService surveyUnitQualityService,
                             ControllerUtils controllerUtils,
                             AuthUtils authUtils) {
        this.surveyUnitPersistencePort = surveyUnitPersistencePort;
        this.metadataService = metadataService;
        this.fileUtils = fileUtils;
        this.contextService = contextService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.controllerUtils = controllerUtils;
        this.authUtils = authUtils;
    }

    @Override
    public void saveSurveyUnits(List<SurveyUnitModel> surveyUnitModels) {
        surveyUnitPersistencePort.saveAll(surveyUnitModels);
    }

    @Override
    public List<SurveyUnitModel> findByIdsInterrogationAndQuestionnaire(String interrogationId, String questionnaireId) {
        return surveyUnitPersistencePort.findByIds(interrogationId, questionnaireId);
    }

    @Override
    public List<SurveyUnitModel> findByInterrogationId(String interrogationId) {
        return surveyUnitPersistencePort.findByInterrogationId(interrogationId);
    }

    @Override
    public Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId) {
        return surveyUnitPersistencePort.findByQuestionnaireId(questionnaireId);
    }

    /**
     * In this method we want to get the latest update for each variable of a survey unit
     * But we need to separate the updates by mode
     * So we will calculate the latest state for a given collection mode
     * @param interrogationId : Survey unit id
     * @param questionnaireId : Questionnaire id
     * @return the latest update for each variable of a survey unit
     */
    @Override
    public List<SurveyUnitModel> findLatestByIdAndByQuestionnaireId(String interrogationId, String questionnaireId) {
        List<SurveyUnitModel> latestUpdatesbyVariables = new ArrayList<>();
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByIds(interrogationId, questionnaireId);
        List<Mode> modes = getDistinctsModes(surveyUnitModels);
        modes.forEach(mode ->{
            List<SurveyUnitModel> suByMode = surveyUnitModels.stream()
                    .filter(surveyUnitModel -> surveyUnitModel.getMode().equals(mode))
                    .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                    .toList();

            //We had all the variables of the oldest update
            latestUpdatesbyVariables.add(suByMode.getFirst());
            //We keep the name of already added variables to skip them in older updates
            List<VarIdScopeTuple> addedVariables = new ArrayList<>();
            SurveyUnitModel latestUpdate = suByMode.getFirst();

            if(latestUpdate.getCollectedVariables() == null){
                latestUpdate.setCollectedVariables(new ArrayList<>());
            }
            if(latestUpdate.getExternalVariables() == null){
                latestUpdate.setExternalVariables(new ArrayList<>());
            }

            latestUpdate.getCollectedVariables().forEach(colVar -> addedVariables.add(new VarIdScopeTuple(colVar.varId(),
                    colVar.scope(), colVar.iteration())));
            latestUpdate.getExternalVariables().forEach(extVar -> addedVariables.add(new VarIdScopeTuple(extVar.varId(), extVar.scope(), extVar.iteration())));

            suByMode.forEach(surveyUnitModel -> {
                List<VariableModel> collectedVariablesToKeep = new ArrayList<>();
                List<VariableModel> externalVariablesToKeep = new ArrayList<>();
                // We iterate over the variables of the update and add them to the list if they are not already added
                if (surveyUnitModel.getCollectedVariables() != null) {
                    surveyUnitModel.getCollectedVariables().stream()
                            .filter(colVar -> !addedVariables.contains(new VarIdScopeTuple(colVar.varId(), colVar.scope()
                                    , colVar.iteration())))
                            .forEach(colVar -> {
                                collectedVariablesToKeep.add(colVar);
                                addedVariables.add(new VarIdScopeTuple(colVar.varId(), colVar.scope(), colVar.iteration()));
                            });
                }
                if (surveyUnitModel.getExternalVariables() != null){
                    surveyUnitModel.getExternalVariables().stream()
                         .filter(extVar -> !addedVariables.contains(new VarIdScopeTuple(extVar.varId(), extVar.scope(),
                                 extVar.iteration())))
                         .forEach(extVar -> {
                            externalVariablesToKeep.add(extVar);
                            addedVariables.add(new VarIdScopeTuple(extVar.varId(), extVar.scope(), extVar.iteration()));
                         });
                }

                // If there are new variables, we add the update to the list of latest updates
                if (!collectedVariablesToKeep.isEmpty() || !externalVariablesToKeep.isEmpty()){
                    surveyUnitModel.setCollectedVariables(collectedVariablesToKeep);
                    surveyUnitModel.setExternalVariables(externalVariablesToKeep);
                    latestUpdatesbyVariables.add(surveyUnitModel);
                }
            });
        });
        return latestUpdatesbyVariables;
    }


    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    @Override
    public List<List<SurveyUnitModel>> findLatestByIdAndByQuestionnaireIdAndModeOrdered(String questionnaireId, String mode,
                                                                                        List<InterrogationId> interrogationIds) {
        //return object
        List<List<SurveyUnitModel>> listLatestUpdatesbyVariables = new ArrayList<>();

        //1) QUERY
        // => conversion of "List<InterrogationId>" -> "List<String>" for query using lamda
        List<String> queryInParam = interrogationIds.stream().map(InterrogationId::getInterrogationId).toList();

        //Get !!!all versions!!! of a set of "interrogationIds"
        List<SurveyUnitModel> allResponsesVersionsSet = surveyUnitPersistencePort.findBySetOfIdsAndQuestionnaireIdAndMode(questionnaireId, mode, queryInParam);

        //2) FILTER BY interrogationId AND ORDER BY DATE (MOST RECENT FIRST, oldest last)
        interrogationIds.forEach(interrogationId -> {
            List<SurveyUnitModel> allResponsesVersionsForSingleInterrId = allResponsesVersionsSet.stream()
                            .filter(surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId.getInterrogationId()))
                            .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                            .toList();

            List<SurveyUnitModel> latestUpdates = extractLatestUpdates(allResponsesVersionsForSingleInterrId);

            //3) add to result (: keep same existing process)
            listLatestUpdatesbyVariables.add(latestUpdates);
        });

        return listLatestUpdatesbyVariables;
    }


    private List<SurveyUnitModel> extractLatestUpdates(List<SurveyUnitModel> allResponsesVersionsForSingleInterrId) {
        //return
        List<SurveyUnitModel> latestUpdatesbyVariables = new ArrayList<>();

        //We keep the name of already added variables to skip them in older updates
        List<VarIdScopeTuple> addedVariables = new ArrayList<>();

        //No useless process if there is only ONE or NONE element (performances optimisations)
        if(allResponsesVersionsForSingleInterrId.size() <= 1) {
            //We add all the variables of the LATEST update
            latestUpdatesbyVariables.add(allResponsesVersionsForSingleInterrId.getFirst());
            return latestUpdatesbyVariables;
        }

        //ELSE -> CASE WHERE THERE ARE MORE THAN ONE VERSION!
        allResponsesVersionsForSingleInterrId.forEach(surveyUnitModel -> {
            List<VariableModel> collectedVariablesToKeep = new ArrayList<>();
            List<VariableModel> externalVariablesToKeep = new ArrayList<>();
            // We iterate over the variables of the update and add them to the list if they are not already added
            if (surveyUnitModel.getCollectedVariables() != null) {
                surveyUnitModel.getCollectedVariables().stream()
                        .filter(colVar -> !addedVariables.contains(new VarIdScopeTuple(colVar.varId(), colVar.scope(), colVar.iteration())))
                        .forEach(colVar -> {
                            collectedVariablesToKeep.add(colVar);
                            addedVariables.add(new VarIdScopeTuple(colVar.varId(), colVar.scope(), colVar.iteration()));
                        });
            }
            if (surveyUnitModel.getExternalVariables() != null){
                surveyUnitModel.getExternalVariables().stream()
                        .filter(extVar -> !addedVariables.contains(new VarIdScopeTuple(extVar.varId(), extVar.scope(), extVar.iteration())))
                        .forEach(extVar -> {
                            externalVariablesToKeep.add(extVar);
                            addedVariables.add(new VarIdScopeTuple(extVar.varId(), extVar.scope(), extVar.iteration()));
                        });
            }

            // If there are new variables, we add the update to the list of latest updates
            if (!collectedVariablesToKeep.isEmpty() || !externalVariablesToKeep.isEmpty()){
                surveyUnitModel.setCollectedVariables(collectedVariablesToKeep);
                surveyUnitModel.setExternalVariables(externalVariablesToKeep);
                latestUpdatesbyVariables.add(surveyUnitModel);
            }
        });

        return latestUpdatesbyVariables;
    }
    //========= OPTIMISATIONS PERFS (END) ==========


    @Override
    public SurveyUnitDto findLatestValuesByStateByIdAndByQuestionnaireId(String interrogationId,
                                                                         String questionnaireId) {
        SurveyUnitDto surveyUnitDto = SurveyUnitDto.builder()
                .interrogationId(interrogationId)
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();

        //Extract variables
        Map<VarIdScopeTuple, VariableDto> collectedVariableMap = new HashMap<>();
        Map<VarIdScopeTuple, VariableDto> externalVariableMap = new HashMap<>();
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByIds(interrogationId, questionnaireId);
        List<Mode> modes = getDistinctsModes(surveyUnitModels);
        modes.forEach(mode -> {
            List<SurveyUnitModel> suByMode = surveyUnitModels.stream()
                    .filter(surveyUnitModel -> surveyUnitModel.getMode().equals(mode))
                    .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                    .toList();
            VariablesMap variablesMap = null;
            for(SurveyUnitModel surveyUnitModel : suByMode){
                if(variablesMap == null){
                    variablesMap = metadataService.readMetadatas(
                            surveyUnitModel.getCampaignId(),
                            surveyUnitModel.getMode().getModeName(),
                            fileUtils,
                            new ArrayList<>());
                }
                extractVariables(surveyUnitModel, collectedVariableMap,
                        externalVariableMap, variablesMap);
            }
        });
        collectedVariableMap.keySet().forEach(variableTuple -> surveyUnitDto.getCollectedVariables().add(collectedVariableMap.get(variableTuple)));
        externalVariableMap.keySet().forEach(variableName -> surveyUnitDto.getExternalVariables().add(externalVariableMap.get(variableName)));
        return surveyUnitDto;
    }

    @Override
    public List<InterrogationId> findDistinctInterrogationIdsByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findInterrogationIdsByQuestionnaireId(questionnaireId);
        List<InterrogationId> suIds = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> suIds.add(new InterrogationId(surveyUnitModel.getInterrogationId())));
        return suIds.stream().distinct().toList();
    }

    //============ OPTIMISATIONS PERFS (START) ============

    /**
     * @author Adrien Marchal
     * Calculations made to establish the data a worker will be responsible of, among the whole data to be processed.
     * (needed for distributed process / horizontal scaling)
     */
    @Override
    public List<InterrogationId> findDistinctPageableInterrogationIdsByQuestionnaireId(String questionnaireId, long totalSize,
                                                                                       long blockSize, long page) {
        long calculatedTotalSize;
        if(totalSize == 0) {
            calculatedTotalSize = countInterrogationIdsByQuestionnaireId(questionnaireId);
        } else {
            calculatedTotalSize = totalSize;
        }

        //Check arguments
        long skip = page * blockSize;
        if(page < 0 || skip > calculatedTotalSize) {
            //return empty list
            return List.of();
        }
        long calculatedBlockSize = (skip + blockSize) > calculatedTotalSize ? calculatedTotalSize - skip : blockSize;

        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findPageableInterrogationIdsByQuestionnaireId(questionnaireId, skip, calculatedBlockSize);
        List<InterrogationId> suIds = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> suIds.add(new InterrogationId(surveyUnitModel.getInterrogationId())));
        return suIds.stream().distinct().toList();
    }


    /**
     * @author Adrien Marchal
     */
    @Override
    public long countInterrogationIdsByQuestionnaireId(String questionnaireId) {
        return surveyUnitPersistencePort.countInterrogationIdsByQuestionnaireId(questionnaireId);
    }
    //=========== OPTIMISATIONS PERFS (END) =============


    @Override
    public List<SurveyUnitModel> findInterrogationIdsAndModesByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findInterrogationIdsByQuestionnaireId(questionnaireId);
        return surveyUnitModels.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findInterrogationIdsByQuestionnaireId(questionnaireId);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByCampaignId(String campaignId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findInterrogationIdsByCampaignId(campaignId);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    @Override
    public List<Mode> findModesByQuestionnaireIdV2(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findModesByQuestionnaireIdV2(questionnaireId);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByCampaignIdV2(String campaignId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findModesByCampaignIdV2(campaignId);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }
    //========= OPTIMISATIONS PERFS (END) ==========

    @Override
    public Long deleteByQuestionnaireId(String questionnaireId) {
        return surveyUnitPersistencePort.deleteByQuestionnaireId(questionnaireId);
    }

    @Override
    public long countResponses() {
        return surveyUnitPersistencePort.count();
    }

    @Override
    public Set<String> findQuestionnaireIdsByCampaignId(String campaignId) {
            return surveyUnitPersistencePort.findQuestionnaireIdsByCampaignId(campaignId);
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    @Override
    public Set<String> findQuestionnaireIdsByCampaignIdV2(String campaignId) {
        return surveyUnitPersistencePort.findQuestionnaireIdsByCampaignIdV2(campaignId);
    }
    //========= OPTIMISATIONS PERFS (END) ==========

    @Override
    public Set<String> findDistinctCampaignIds() {
        return surveyUnitPersistencePort.findDistinctCampaignIds();
    }

    @Override
    public List<CampaignWithQuestionnaire> findCampaignsWithQuestionnaires() {
        List<CampaignWithQuestionnaire> campaignsWithQuestionnaireList = new ArrayList<>();
        for(String campaignId : findDistinctCampaignIds()){
            Set<String> questionnaires = findQuestionnaireIdsByCampaignId(campaignId);
            campaignsWithQuestionnaireList.add(new CampaignWithQuestionnaire(campaignId,questionnaires));
        }
        return campaignsWithQuestionnaireList;
    }

    @Override
    public long countResponsesByCampaignId(String campaignId){
        return surveyUnitPersistencePort.countByCampaignId(campaignId);
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        return surveyUnitPersistencePort.findDistinctQuestionnaireIds();
    }

    @Override
    public List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns() {
        List<QuestionnaireWithCampaign> questionnaireWithCampaignList = new ArrayList<>();
        for(String questionnaireId : findDistinctQuestionnaireIds()){
            Set<String> campaigns = surveyUnitPersistencePort.findCampaignIdsByQuestionnaireId(questionnaireId);
            questionnaireWithCampaignList.add(new QuestionnaireWithCampaign(
                    questionnaireId,
                    campaigns)
            );

        }
        return questionnaireWithCampaignList;
    }

    @Override
    public List<SurveyUnitModel> parseEditedVariables(
            SurveyUnitInputDto surveyUnitInputDto,
            String userIdentifier,
            VariablesMap variablesMap
    ) throws GenesisException {

        List<DataState> statesReceived = surveyUnitInputDto.getCollectedVariables().stream()
                .map(colVar -> colVar.getVariableStateInputDto().getState())
                .distinct()
                .toList();

        if (statesReceived.contains(DataState.COLLECTED)){
            throw new GenesisException(400,"You can not persist in database a new value with the state COLLECTED");
        }

        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();

        for (DataState state : statesReceived){
            SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                    .campaignId(surveyUnitInputDto.getCampaignId())
                    .mode(surveyUnitInputDto.getMode())
                    .questionnaireId(surveyUnitInputDto.getQuestionnaireId())
                    .interrogationId(surveyUnitInputDto.getInterrogationId())
                    .state(state)
                    .recordDate(LocalDateTime.now())
                    .collectedVariables(new ArrayList<>())
                    .externalVariables(new ArrayList<>())
                    .modifiedBy(userIdentifier)
                    .build();

            //Keep only variable dtos who has the corresponding state
            List<VariableInputDto> editedCollectedVariables = surveyUnitInputDto.getCollectedVariables().stream()
                    .filter(colVar -> colVar.getVariableStateInputDto().getState() == state).toList();

            //Collected variables management
            for(VariableInputDto editedVariableDto : editedCollectedVariables){
                VariableModel collectedVariable = VariableModel.builder()
                        .varId(editedVariableDto.getVariableName())
                        .value(editedVariableDto.getVariableStateInputDto().getValue() == null ?
                                null
                                : editedVariableDto.getVariableStateInputDto().getValue().toString()
                            )
                        .parentId(GroupUtils.getParentGroupName(editedVariableDto.getVariableName(), variablesMap))
                        .scope(variablesMap.getVariable(editedVariableDto.getVariableName()).getGroupName())
                        .iteration(editedVariableDto.getIteration())
                        .build();

                surveyUnitModel.getCollectedVariables().add(collectedVariable);

            }
            surveyUnitModels.add(surveyUnitModel);
        }

        return surveyUnitModels;
    }

    @Override
    public String findQuestionnaireIdByInterrogationId(String interrogationId) throws GenesisException {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByInterrogationId(interrogationId);
        if (surveyUnitModels.isEmpty()){
            throw new GenesisException(404,String.format("The interrogationId %s is not in database",interrogationId));
        }
        Set<String> questionnaireIds = new HashSet<>();
        for(SurveyUnitModel surveyUnitModel : surveyUnitModels){
            questionnaireIds.add(surveyUnitModel.getQuestionnaireId());
        }
        if(questionnaireIds.size() > 1){
            throw new GenesisException(207,String.format("Multiple questionnaires for %s :%n%s",
                    interrogationId,
                    String.join("\n", questionnaireIds)
            ));
        }

        return questionnaireIds.iterator().next(); //Return first (and supposed only) element of set
    }

    @Override
    public Set<String> findCampaignIdsFrom(SurveyUnitInputDto dto) {
        List<SurveyUnitModel> responses = findByIdsInterrogationAndQuestionnaire(
                dto.getInterrogationId(),
                dto.getQuestionnaireId()
        );
        return responses.stream()
                .map(SurveyUnitModel::getCampaignId)
                .collect(Collectors.toSet());
    }

    //Utils
    private static List<Mode> getDistinctsModes(List<SurveyUnitModel> surveyUnitModels) {
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }

    /**
     * Extract collected variables from a model class to a variable map
     * @param surveyUnitModel survey unit model
     * @param collectedVariableMap Collected variable DTO map to populate
     * @param externalVariableMap External variable DTO map to populate
     */
    private void extractVariables(SurveyUnitModel surveyUnitModel,
                                  Map<VarIdScopeTuple, VariableDto> collectedVariableMap,
                                  Map<VarIdScopeTuple, VariableDto> externalVariableMap,
                                  VariablesMap variablesMap
                                  ) {

        if(surveyUnitModel.getCollectedVariables() == null){
            surveyUnitModel.setCollectedVariables(new ArrayList<>());
        }
        for (VariableModel collectedVariable : surveyUnitModel.getCollectedVariables()) {
            VarIdScopeTuple loopIdTuple = new VarIdScopeTuple(collectedVariable.varId(), collectedVariable.scope(),
                    collectedVariable.iteration());
            VariableDto variableDto = collectedVariableMap.get(loopIdTuple);

            //Create variable into map if not exists
            if (variableDto == null) {
                variableDto = VariableDto.builder()
                        .variableName(collectedVariable.varId())
                        .scope(collectedVariable.scope())
                        .iteration(collectedVariable.iteration())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                collectedVariableMap.put(loopIdTuple, variableDto);
            }
            //Extract variable state
            if (isMostRecentForSameState(surveyUnitModel, variableDto)) {
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(getValueWithType(
                                        collectedVariable.varId(),
                                        collectedVariable.value(),
                                        variablesMap))
                                .date(surveyUnitModel.getRecordDate())
                                .build()
                );
            }
        }

        if(surveyUnitModel.getExternalVariables() == null){
            surveyUnitModel.setExternalVariables(new ArrayList<>());
        }
        for(VariableModel externalVariable : surveyUnitModel.getExternalVariables()){
            VarIdScopeTuple loopIdTuple = new VarIdScopeTuple(externalVariable.varId(), externalVariable.scope(), externalVariable.iteration());
            VariableDto variableDto = externalVariableMap.get(loopIdTuple);

            //Create variable into map if not exists
            if(variableDto == null){
                variableDto = VariableDto.builder()
                        .variableName(externalVariable.varId())
                        .scope(externalVariable.scope())
                        .iteration(externalVariable.iteration())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                externalVariableMap.put(loopIdTuple, variableDto);
            }
            //Extract variable state
            if(isMostRecentForSameState(surveyUnitModel, variableDto)){
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(getValueWithType(
                                        externalVariable.varId(),
                                        externalVariable.value(),
                                        variablesMap))
                                .date(surveyUnitModel.getRecordDate())
                                .build()
                );
            }
        }
    }

    private Object getValueWithType(String variableName, String value, VariablesMap variablesMap) {
        if(!variablesMap.hasVariable(variableName)){
            log.warn("Variable {} not found in variableMap", variableName);
            return value;
        }
        if(value == null) return null;
        if(value.isEmpty()) return value;
        VariableType variableType = variablesMap.getVariable(variableName).getType();
        try {
            switch (variableType) {
                case INTEGER -> {
                    return Integer.parseInt(value);
                }
                case BOOLEAN -> {
                    return Boolean.parseBoolean(value);
                }
                case NUMBER -> {
                    return Double.parseDouble(value);
                }
                default -> {
                    return value;
                }
            }
        }catch (NumberFormatException e){
            log.warn("Invalid value {} for variable {}, expected type {}", value, variableName, variableType);
            return value;
        }
    }


    /**
     * Check if there is any other more recent variable value for a same state in Variable DTO
     * @param surveyUnitModel model containing variable
     * @param variableDto DTO to check in
     * @return true if there's no more recent variable for the same state
     */
    private boolean isMostRecentForSameState(SurveyUnitModel surveyUnitModel, VariableDto variableDto) {
        List<VariableStateDto> variableStatesSameState = variableDto.getVariableStateDtoList().stream().filter(
                variableStateDto -> variableStateDto.getState().equals(surveyUnitModel.getState())
        )
                .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
                .toList();
        if(variableStatesSameState.isEmpty()){
            //Variable doesn't contain state
            return true;
        }
        LocalDateTime mostRecentStateDateTime = variableStatesSameState.getFirst().getDate();
        return mostRecentStateDateTime.isBefore(surveyUnitModel.getRecordDate());
    }

    /**
     * Check if model is more recent that any variable state in variable DTO regardless of state
     * Used for active variable
     * @param surveyUnitModel model used to compare
     * @param variableDto variable to check
     * @return false if there is any variable state that comes from a more recent model already
     */
    private boolean isLastVariableState(SurveyUnitModel surveyUnitModel, VariableDto variableDto) {
        for(VariableStateDto variableStateDTO : variableDto.getVariableStateDtoList()){
            if(variableStateDTO.getDate().isAfter(surveyUnitModel.getRecordDate())){
                return false;
            }
            variableStateDTO.setActive(false);
        }
        return true;
    }


    @Override
    public void saveResponsesFromXmlFile(String xmlFile, String metadataFilePath, Mode modeSpecified, boolean withDDI) throws Exception {
        VariablesMap variablesMap;
        if(withDDI) {
            //Parse DDI
            log.info("Try to read DDI file : {}", metadataFilePath);
            try {
                variablesMap =
                        DDIReader.getMetadataFromDDI(Path.of(metadataFilePath).toFile().toURI().toURL().toString(),
                                new FileInputStream(metadataFilePath)).getVariables();
            } catch (MetadataParserException e) {
                throw new MetadataParserException(e.getMessage());
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException(e.getMessage());
            } catch(MalformedURLException e) {
                throw new MalformedURLException(e.getMessage());
            }
        }else{
            //Parse Lunatic
            log.info("Try to read lunatic file : {}", metadataFilePath);

            variablesMap = LunaticReader.getMetadataFromLunatic(new FileInputStream(metadataFilePath)).getVariables();
        }

        log.info("Try to read Xml file : {}", xmlFile);
        Path filepath = Paths.get(xmlFile);

        try {
            if (getFileSizeInMB(filepath) <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
                processXmlFileWithMemory(filepath, modeSpecified, variablesMap);
                return;
            }
            processXmlFileSequentially(filepath, modeSpecified, variablesMap);
        } catch(IOException e) {
            throw new IOException(e.getMessage());
        } catch(ParserConfigurationException e) {
            throw new ParserConfigurationException(e.getMessage());
        } catch(SAXException e) {
            throw new SAXException(e.getMessage());
        } catch(XMLStreamException e) {
            throw new XMLStreamException(e.getMessage());
        } catch(GenesisException e) {
            throw new GenesisException(e.getStatus(), e.getMessage());
        }
    }


    @Override
    public boolean saveResponsesFromXmlCampaignFolder(String campaignName, Mode modeSpecified) throws Exception {
        List<GenesisError> errors = new ArrayList<>();
        boolean isAnyDataSaved = false;

        log.info("Try to import XML data for campaign: {}", campaignName);

        List<Mode> modesList = controllerUtils.getModesList(campaignName, modeSpecified);
        for (Mode currentMode : modesList) {
            try {
                processCampaignWithMode(campaignName, currentMode, errors, null);
                isAnyDataSaved = true;
            }catch (NoDataException nde){
                //Don't stop if NoDataError thrown
                log.warn(nde.getMessage());
            }catch (Exception e){
                log.error(ResponseController.CAMPAIGN_ERROR, campaignName, e.toString());
                throw new Exception(e.getMessage());
            }
        }

        if (!errors.isEmpty()){
            throw new GenesisException(500, errors.getFirst().getMessage());
        }
        return isAnyDataSaved;
    }


    @Override
    public void saveResponsesFromAllCampaignFolders()  throws NoDataException, GenesisException {
        List<GenesisError> errors = new ArrayList<>();
        List<File> campaignFolders = fileUtils.listAllSpecsFolders();

        if (campaignFolders.isEmpty()) {
            throw new NoDataException("No campaign to save");
        }

        for (File  campaignFolder: campaignFolders) {
            String campaignName = campaignFolder.getName();
            log.info("Try to import data for campaign: {}", campaignName);

            try {
                List<Mode> modesList = controllerUtils.getModesList(campaignName, null); //modeSpecified null = all modes
                for (Mode currentMode : modesList) {
                    processCampaignWithMode(campaignName, currentMode, errors, Constants.DIFFERENTIAL_DATA_FOLDER_NAME);
                }
            }catch (NoDataException nde){
                log.warn(nde.getMessage());
            }
            catch (Exception e) {
                log.warn(ResponseController.CAMPAIGN_ERROR, campaignName, e.toString());
                errors.add(new GenesisError(e.getMessage()));
            }
        }

        if (!errors.isEmpty()){
            throw new GenesisException(209, "Data saved with " + errors.size() + " errors");
        }
    }


    @Override
    public SurveyUnitQualityToolDto findResponsesByInterrogationAndQuestionnaireLatestStates(String interrogationId, String questionnaireId) throws GenesisException {
        //Check context
        DataProcessingContextModel dataProcessingContextModel =
                contextService.getContext(interrogationId);

        if(dataProcessingContextModel == null || !dataProcessingContextModel.isWithReview()){
            throw new GenesisException(403, new ApiError("Review is disabled for that partition").message());
        }

        SurveyUnitDto response = findLatestValuesByStateByIdAndByQuestionnaireId(interrogationId, questionnaireId);
        return DataTransformer.transformSurveyUnitDto(response);
    }


    @Override
    public void saveEditedVariables(SurveyUnitInputDto surveyUnitInputDto) throws GenesisException {
        log.debug("Received in save edited : {}",surveyUnitInputDto.toString());
        //Code quality : we need to put all that logic out of this controller
        //Parse metadata
        //Try to look for DDI first, if no DDI found looks for lunatic components
        List<GenesisError> errors = new ArrayList<>();
        //We need to retrieve campaignId
        Set<String> campaignIds = findCampaignIdsFrom(surveyUnitInputDto);
        if (campaignIds.size() != 1){
            throw new GenesisException(500, "Impossible to assign one campaignId to that response");
        }
        // If the size is equal to 1 we get this campaignId
        String campaignId = campaignIds.iterator().next();
        surveyUnitInputDto.setCampaignId(campaignId);
        VariablesMap variablesMap = metadataService.readMetadatas(surveyUnitInputDto.getCampaignId(),
                surveyUnitInputDto.getMode().getModeName(), fileUtils, errors);
        if(variablesMap == null){
            log.warn("Can't find DDI, trying with lunatic...");
            variablesMap = metadataService.readMetadatas(surveyUnitInputDto.getCampaignId(),
                    surveyUnitInputDto.getMode().getModeName(), fileUtils, errors);
            if(variablesMap == null){
                throw new GenesisException(404, errors.getLast().getMessage());
            }
        }

        //Check if input edited variables are in metadatas
        List<String> absentCollectedVariableNames =
                surveyUnitQualityService.checkVariablesPresentInMetadata(surveyUnitInputDto.getCollectedVariables(),
                        variablesMap);
        if (!absentCollectedVariableNames.isEmpty()) {
            String absentVariables = String.join("\n", absentCollectedVariableNames);
            //400 = Bad Request
            throw new GenesisException(400, String.format("The following variables are absent in metadatas : %n%s", absentVariables));
        }

        //Fetch user identifier from OIDC token
        String userIdentifier = authUtils.getIDEP();


        //Create surveyUnitModel for each STATE received (Quality tool could send variables with another STATE than EDITED)
        List<SurveyUnitModel> surveyUnitModels;
        try{
            surveyUnitModels = parseEditedVariables(
                    surveyUnitInputDto,
                    userIdentifier,
                    variablesMap
            );
        }catch (GenesisException e){
            throw new GenesisException(e.getStatus(), e.getMessage());
        }

        //Check data with dataverifier (might create a FORCED document)
        surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);

        //Save documents
        saveSurveyUnits(surveyUnitModels);
    }


    //Utilities
    /**
     * Process a campaign with a specific mode
     * @param campaignName name of campaign
     * @param mode mode of collected data
     * @param errors error list to fill
     */
    private void processCampaignWithMode(String campaignName, Mode mode, List<GenesisError> errors, String rootDataFolder)
            throws IOException, ParserConfigurationException, SAXException, XMLStreamException, NoDataException, GenesisException {
        log.info("Starting data import for mode: {}", mode.getModeName());
        String dataFolder = rootDataFolder == null ?
                fileUtils.getDataFolder(campaignName, mode.getFolder(), null)
                : fileUtils.getDataFolder(campaignName, mode.getFolder(), rootDataFolder);
        List<String> dataFiles = fileUtils.listFiles(dataFolder);
        log.info("Number of files to load in folder {} : {}", dataFolder, dataFiles.size());
        if (dataFiles.isEmpty()) {
            throw new NoDataException("No data file found in folder %s".formatted(dataFolder));
        }

        VariablesMap variablesMap = metadataService.readMetadatas(campaignName, mode.getModeName(), fileUtils, errors);
        if (variablesMap == null){
            return;
        }

        //For each XML data file
        for (String fileName : dataFiles.stream().filter(s -> s.endsWith(".xml")).toList()) {
            processOneXmlFileForCampaign(campaignName, mode, fileName, dataFolder, variablesMap);
        }

        //Create context if not exist
        if(contextService.getContextByPartitionId(campaignName) == null){
            contextService.saveContext(campaignName, false);
        }

    }


    private void processOneXmlFileForCampaign(String campaignName,
                                              Mode mode,
                                              String fileName,
                                              String dataFolder,
                                              VariablesMap variablesMap) throws IOException, ParserConfigurationException, SAXException, XMLStreamException, GenesisException {
        String filepathString = String.format(PATH_FORMAT, dataFolder, fileName);
        Path filepath = Paths.get(filepathString);
        //Check if file not in done folder, delete if true
        if(isDataFileInDoneFolder(filepath, campaignName, mode.getFolder())){
            log.warn("File {} already exists in DONE folder ! Deleting...", fileName);
            Files.deleteIfExists(filepath);
            return;
        }
        //Read file
        log.info("Try to read Xml file : {}", fileName);
        try {
            if (getFileSizeInMB(filepath) <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
                processXmlFileWithMemory(filepath, mode, variablesMap);
            } else {
                processXmlFileSequentially(filepath, mode, variablesMap);
            }
            log.debug("File {} saved", fileName);
            //If no exception has been thrown at this step, all is good then : we can move the file!
            fileUtils.moveDataFile(campaignName, mode.getFolder(), filepath);
        } catch(GenesisException e) {
            log.error("Error {} on file {} : {}", e.getStatus(), fileName,  e.getMessage());
            throw new GenesisException(e.getStatus(), e.getMessage());
        }
    }


    private static long getFileSizeInMB(Path filepath) {
        return filepath.toFile().length() / 1024 / 1024;
    }


    private boolean isDataFileInDoneFolder(Path filepath, String campaignName, String modeFolder) {
        return Path.of(fileUtils.getDoneFolder(campaignName, modeFolder)).resolve(filepath.getFileName()).toFile().exists();
    }


    private void processXmlFileWithMemory(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, ParserConfigurationException, SAXException, GenesisException {
        LunaticXmlCampaign campaign;
        // DOM method
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        try {
            campaign = parser.parseDataFile(filepath);
        } catch (GenesisException e) {
            log.error(e.toString());
            throw new GenesisException(e.getStatus(), e.getMessage());
        }

        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            surveyUnitModels.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getCampaignId(), modeSpecified));
        }
        surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);

        log.debug("Saving {} survey units updates", surveyUnitModels.size());
        saveSurveyUnits(surveyUnitModels);
        log.debug("Survey units updates saved");

        log.info("File {} processed with {} survey units", filepath.getFileName(), surveyUnitModels.size());
    }


    private void processXmlFileSequentially(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, XMLStreamException, GenesisException {
        LunaticXmlCampaign campaign;
        //Sequential method
        log.warn("File size > {} MB! Parsing XML file using sequential method...", Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL);
        try (final InputStream stream = new FileInputStream(filepath.toFile())) {
            LunaticXmlDataSequentialParser parser = new LunaticXmlDataSequentialParser(filepath, stream);
            int suCount = 0;

            campaign = parser.getCampaign();
            LunaticXmlSurveyUnit su = parser.readNextSurveyUnit();
            contextService.saveContext(campaign.getCampaignId(), false);

            while (su != null) {
                List<SurveyUnitModel> surveyUnitModels = new ArrayList<>(LunaticXmlAdapter.convert(su, variablesMap, campaign.getCampaignId(), modeSpecified));

                surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
                saveSurveyUnits(surveyUnitModels);
                suCount++;

                su = parser.readNextSurveyUnit();
            }

            log.info("Saved {} survey units updates from Xml file {}", suCount,  filepath.getFileName());
        }
    }


}
