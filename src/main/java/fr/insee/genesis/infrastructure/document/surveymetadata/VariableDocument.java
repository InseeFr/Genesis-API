package fr.insee.genesis.infrastructure.document.surveymetadata;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.VariableType;
import lombok.Data;

@Data
public class VariableDocument {
    protected String name;
    protected Group group;
    protected VariableType type;
    protected String sasFormat;
    protected int maxLengthData;
    protected String questionName;
    protected boolean isInQuestionGrid;
}
