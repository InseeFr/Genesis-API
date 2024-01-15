package fr.insee.genesis.domain.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
/*
 * An object indicating which values are invalid in a external variable DTO
 */
public class CorrectedExternalVariable extends VariableDto{
    private List<Integer> incorrectValueIndexes;

    public CorrectedExternalVariable(String idVar, List<String> values) {
        super(idVar, values);
        this.incorrectValueIndexes = new ArrayList<>();
    }
}
