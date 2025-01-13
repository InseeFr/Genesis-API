package fr.insee.genesis.infrastructure.document.surveymetadata;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.VariableType;

public record VariableDocument (
    String name,
    Group group,
    VariableType type,
    String sasFormat,
    int maxLengthData,
    String questionName,
    boolean isInQuestionGrid
){}
