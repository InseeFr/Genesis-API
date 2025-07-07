package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.infrastructure.document.surveyunit.GroupedInterrogationDocument;
import fr.insee.genesis.infrastructure.mappers.GroupedInterrogationDocumentMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class GroupedInterrogationDocumentMapperTest {

    private final GroupedInterrogationDocumentMapper mapper = GroupedInterrogationDocumentMapper.INSTANCE;

    @Test
    void testDocumentToModel() {
        GroupedInterrogationDocument doc = new GroupedInterrogationDocument();
        doc.setQuestionnaireId("Q1");
        doc.setPartitionOrCampaignId("CAMP1");
        doc.setInterrogationIds(List.of("I1", "I2"));

        GroupedInterrogation model = mapper.documentToModel(doc);

        Assertions.assertThat(model).isNotNull();
        Assertions.assertThat(model.questionnaireId()).isEqualTo("Q1");
        Assertions.assertThat(model.partitionOrCampaignId()).isEqualTo("CAMP1");
        Assertions.assertThat(model.interrogationIds()).isEqualTo(List.of("I1", "I2"));
    }

    @Test
    void testModelToDocument() {
        GroupedInterrogation model = GroupedInterrogation.builder()
                .questionnaireId("Q2")
                .partitionOrCampaignId("CAMP2")
                .interrogationIds(List.of("I3", "I4"))
                .build();

        GroupedInterrogationDocument doc = mapper.modelToDocument(model);

        Assertions.assertThat(doc).isNotNull();
        Assertions.assertThat(doc.getQuestionnaireId()).isEqualTo("Q2");
        Assertions.assertThat(doc.getPartitionOrCampaignId()).isEqualTo("CAMP2");
        Assertions.assertThat(doc.getInterrogationIds()).isEqualTo(List.of("I3", "I4"));
    }

    @Test
    void testListDocumentToListModel() {
        GroupedInterrogationDocument doc1 = new GroupedInterrogationDocument();
        doc1.setQuestionnaireId("Q1");
        doc1.setPartitionOrCampaignId("C1");
        doc1.setInterrogationIds(List.of("I1"));

        GroupedInterrogationDocument doc2 = new GroupedInterrogationDocument();
        doc2.setQuestionnaireId("Q2");
        doc2.setPartitionOrCampaignId("C2");
        doc2.setInterrogationIds(List.of("I2"));

        List<GroupedInterrogation> models = mapper.listDocumentToListModel(List.of(doc1, doc2));

        Assertions.assertThat(models).hasSize(2);
        Assertions.assertThat(models.get(0).questionnaireId()).isEqualTo("Q1");
        Assertions.assertThat(models.get(1).questionnaireId()).isEqualTo("Q2");
    }

    @Test
    void testListModelToListDocument() {
        GroupedInterrogation model1 = GroupedInterrogation.builder()
                .questionnaireId("Q3")
                .partitionOrCampaignId("C3")
                .interrogationIds(List.of("I3"))
                .build();

        GroupedInterrogation model2 = GroupedInterrogation.builder()
                .questionnaireId("Q4")
                .partitionOrCampaignId("C4")
                .interrogationIds(List.of("I4"))
                .build();

        List<GroupedInterrogationDocument> docs = mapper.listModelToListDocument(List.of(model1, model2));

        Assertions.assertThat(docs).hasSize(2);
        Assertions.assertThat(docs.get(0).getQuestionnaireId()).isEqualTo("Q3");
        Assertions.assertThat(docs.get(1).getQuestionnaireId()).isEqualTo("Q4");
    }
}
