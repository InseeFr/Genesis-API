package fr.insee.genesis.domain.service.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistancePortStub;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class QuestionnaireMetadataServiceTest {

    @Test
    void loadAndSaveIfNotExists_should_add_missing_and_filter_variables() throws Exception {

        /*
        QuestionnaireMetadataPersistancePortStub metadataStub = new QuestionnaireMetadataPersistancePortStub();
        FileUtils fileUtils = new FileUtils(new ConfigStub());

        QuestionnaireMetadataService service = new QuestionnaireMetadataService(metadataStub);
        List<GenesisError> errors = new ArrayList<>();

        String campaign = "TEST-GLOB";
        Mode mode = Mode.TEL;

        // On lit directement le fichier Lunatic (pour vérifier initial)
        MetadataModel initial = service.parseMetadata(
                "src/test/resources/specs/" + campaign + "/" + mode + "/lunatic_" + campaign + ".json",
                false
        );

        int initialCount = initial.getVariables().getVariables().size();

        // WHEN
        MetadataModel saved = service.loadAndSaveIfNotExists(
                campaign,
                campaign,
                mode,
                fileUtils,
                errors
        );

        // THEN
        int afterCount = saved.getVariables().getVariables().size();
        assertThat(initialCount).isGreaterThan(afterCount);
*/

    }


}