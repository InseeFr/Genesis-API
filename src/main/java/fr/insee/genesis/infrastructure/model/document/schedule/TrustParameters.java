package fr.insee.genesis.infrastructure.model.document.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrustParameters {
    private String inputPath;
    private String outputFolder;
    private String vaultPath;
    private boolean isSigned;
}
