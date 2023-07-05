package fr.insee.genesis.infrastructure.model.entity;

import fr.insee.genesis.domain.dtos.DataType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class VariableState implements Serializable {

    @Serial
    private static final long serialVersionUID = -1576556180669134053L;
    private String idVar;
    private DataType type;
    private String idLoop;
    private String idParent;
    private List<String> values;
}
