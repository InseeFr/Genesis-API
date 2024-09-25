package fr.insee.genesis.domain.model.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrustParameters {
    private String inputPath;
    private String outputFolder;
    private String vaultPath;
    private boolean useSignature;
}
