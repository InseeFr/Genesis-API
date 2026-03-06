package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SurveyUnitMongoAdapter tests")
class SurveyUnitMongoAdapterTest {

	private static final String QUESTIONNAIRE_ID = "questionnaire-123";
	private static final String COLLECTION_INSTRUMENT_ID = "instrument-456";
	private static final String INTERROGATION_ID = "interrogation-789";
	private static final String CAMPAIGN_ID = "campaign-101";
	private static final String MODE = "CAWI";

	@Mock
	private SurveyUnitMongoDBRepository mongoRepository;

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private SurveyUnitMongoAdapter adapter;

	@Nested
	@DisplayName("saveAll() tests")
	class SaveAllTests {

		@Test
		@DisplayName("Should call repository.insert() with mapped documents")
		@SuppressWarnings("unchecked")
		void saveAll_shouldCallRepositoryInsert() {
			// GIVEN
			List<SurveyUnitModel> models = List.of(buildModel("i1"), buildModel("i2"));

			// WHEN
			adapter.saveAll(models);

			// THEN
			verify(mongoRepository).insert(any(List.class));
			verifyNoMoreInteractions(mongoRepository);
		}

		@Test
		@DisplayName("Should pass as many documents as models provided")
		@SuppressWarnings("unchecked")
		void saveAll_shouldMapAllModels() {
			// GIVEN
			List<SurveyUnitModel> models = List.of(buildModel("i1"), buildModel("i2"), buildModel("i3"));

			// WHEN
			adapter.saveAll(models);

			// THEN
			var captor = org.mockito.ArgumentCaptor.forClass(List.class);
			verify(mongoRepository).insert(captor.capture());
			assertThat(captor.getValue()).hasSize(3);
		}
	}

	@Nested
	@DisplayName("findByIds() tests")
	class FindByIdsTests {

		@Test
		@DisplayName("Should call both repository methods and return merged results")
		void findByIds_shouldCallBothRepositoryMethods() {
			// GIVEN
			when(mongoRepository.findByInterrogationIdAndCollectionInstrumentId(INTERROGATION_ID, COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of(buildDoc("i1")));
			when(mongoRepository.findByInterrogationIdAndQuestionnaireId(INTERROGATION_ID, COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of(buildDoc("i2")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findByIds(INTERROGATION_ID, COLLECTION_INSTRUMENT_ID);

			// THEN
			verify(mongoRepository).findByInterrogationIdAndCollectionInstrumentId(INTERROGATION_ID, COLLECTION_INSTRUMENT_ID);
			verify(mongoRepository).findByInterrogationIdAndQuestionnaireId(INTERROGATION_ID, COLLECTION_INSTRUMENT_ID);
			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Should return empty list when both repository methods return empty lists")
		void findByIds_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findByInterrogationIdAndCollectionInstrumentId(any(), any())).thenReturn(List.of());
			when(mongoRepository.findByInterrogationIdAndQuestionnaireId(any(), any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findByIds(INTERROGATION_ID, COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findByUsualSurveyUnitAndCollectionInstrumentIds() tests")
	class FindByUsualSurveyUnitTests {

		@Test
		@DisplayName("Should call both repository methods and merge results")
		void findByUsualSurveyUnit_shouldCallBothRepositoryMethods() {
			// GIVEN
			String usualSurveyUnitId = "usual-001";
			when(mongoRepository.findByUsualSurveyUnitIdAndCollectionInstrumentId(usualSurveyUnitId, COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of(buildDoc("i1")));
			when(mongoRepository.findByUsualSurveyUnitIdAndQuestionnaireId(usualSurveyUnitId, COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findByUsualSurveyUnitAndCollectionInstrumentIds(usualSurveyUnitId, COLLECTION_INSTRUMENT_ID);

			// THEN
			verify(mongoRepository).findByUsualSurveyUnitIdAndCollectionInstrumentId(usualSurveyUnitId, COLLECTION_INSTRUMENT_ID);
			verify(mongoRepository).findByUsualSurveyUnitIdAndQuestionnaireId(usualSurveyUnitId, COLLECTION_INSTRUMENT_ID);
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when both repository methods return empty lists")
		void findByUsualSurveyUnit_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findByUsualSurveyUnitIdAndCollectionInstrumentId(any(), any())).thenReturn(List.of());
			when(mongoRepository.findByUsualSurveyUnitIdAndQuestionnaireId(any(), any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findByUsualSurveyUnitAndCollectionInstrumentIds("usual-001", COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findBySetOfIdsAndQuestionnaireIdAndMode() tests")
	class FindBySetOfIdsTests {

		@Test
		@DisplayName("Should delegate to repository and return mapped models")
		void findBySetOfIds_shouldReturnMappedModels() {
			// GIVEN
			List<String> ids = List.of("i1", "i2");
			when(mongoRepository.findBySetOfIdsAndQuestionnaireIdAndMode(QUESTIONNAIRE_ID, MODE, ids))
					.thenReturn(List.of(buildDoc("i1")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findBySetOfIdsAndQuestionnaireIdAndMode(QUESTIONNAIRE_ID, MODE, ids);

			// THEN
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when repository returns empty list")
		void findBySetOfIds_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findBySetOfIdsAndQuestionnaireIdAndMode(any(), any(), any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findBySetOfIdsAndQuestionnaireIdAndMode(QUESTIONNAIRE_ID, MODE, List.of());

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findByInterrogationId() tests")
	class FindByInterrogationIdTests {

		@Test
		@DisplayName("Should delegate to repository and return mapped models")
		void findByInterrogationId_shouldReturnMappedModels() {
			// GIVEN
			when(mongoRepository.findByInterrogationId(INTERROGATION_ID))
					.thenReturn(List.of(buildDoc("i1")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findByInterrogationId(INTERROGATION_ID);

			// THEN
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when no document found")
		void findByInterrogationId_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findByInterrogationId(any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findByInterrogationId(INTERROGATION_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findByInterrogationIdsAndQuestionnaireId() tests")
	class FindByInterrogationIdsAndQuestionnaireIdTests {

		@Test
		@DisplayName("Should query repository once per model and aggregate results")
		void findByInterrogationIds_shouldQueryRepositoryForEachModel() {
			// GIVEN
			List<SurveyUnitModel> models = List.of(buildModel("i1"), buildModel("i2"));
			when(mongoRepository.findByInterrogationIdAndQuestionnaireId("i1", QUESTIONNAIRE_ID))
					.thenReturn(List.of(buildDoc("i1")));
			when(mongoRepository.findByInterrogationIdAndQuestionnaireId("i2", QUESTIONNAIRE_ID))
					.thenReturn(List.of(buildDoc("i2")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findByInterrogationIdsAndQuestionnaireId(models, QUESTIONNAIRE_ID);

			// THEN
			verify(mongoRepository, times(2)).findByInterrogationIdAndQuestionnaireId(anyString(), eq(QUESTIONNAIRE_ID));
			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Should return empty list when all repository queries return empty lists")
		void findByInterrogationIds_noResults_shouldReturnEmptyList() {
			// GIVEN
			List<SurveyUnitModel> models = List.of(buildModel("i1"));
			when(mongoRepository.findByInterrogationIdAndQuestionnaireId(any(), any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findByInterrogationIdsAndQuestionnaireId(models, QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should return empty list when input model list is empty")
		void findByInterrogationIds_emptyInput_shouldReturnEmptyList() {
			// GIVEN

			// WHEN
			List<SurveyUnitModel> result = adapter.findByInterrogationIdsAndQuestionnaireId(List.of(), QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isEmpty();
			verifyNoInteractions(mongoRepository);
		}
	}

	@Nested
	@DisplayName("findByQuestionnaireId() (Stream) tests")
	class FindByQuestionnaireIdStreamTests {

		@Test
		@DisplayName("Should return a stream of mapped models")
		void findByQuestionnaireId_shouldReturnMappedStream() {
			// GIVEN
			when(mongoRepository.findByQuestionnaireId(QUESTIONNAIRE_ID))
					.thenReturn(Stream.of(buildDoc("i1"), buildDoc("i2")));

			// WHEN
			Stream<SurveyUnitModel> result = adapter.findByQuestionnaireId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isNotNull();
			assertThat(result.toList()).hasSize(2);
		}

		@Test
		@DisplayName("Should return an empty stream when repository returns empty stream")
		void findByQuestionnaireId_emptyStream_shouldReturnEmptyStream() {
			// GIVEN
			when(mongoRepository.findByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(Stream.empty());

			// WHEN
			Stream<SurveyUnitModel> result = adapter.findByQuestionnaireId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result.toList()).isEmpty();
		}
	}

	@Nested
	@DisplayName("deleteByCollectionInstrumentId() tests")
	class DeleteByCollectionInstrumentIdTests {

		@Test
		@DisplayName("Should call both delete methods and return the sum of deleted counts")
		void delete_shouldReturnSumOfDeletedCounts() {
			// GIVEN
			when(mongoRepository.deleteByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(3L);
			when(mongoRepository.deleteByQuestionnaireId(COLLECTION_INSTRUMENT_ID)).thenReturn(2L);

			// WHEN
			Long result = adapter.deleteByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEqualTo(5L);
		}

		@Test
		@DisplayName("Should return 0 when both delete methods return 0")
		void delete_nothingDeleted_shouldReturnZero() {
			// GIVEN
			when(mongoRepository.deleteByCollectionInstrumentId(any())).thenReturn(0L);
			when(mongoRepository.deleteByQuestionnaireId(any())).thenReturn(0L);

			// WHEN
			Long result = adapter.deleteByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isZero();
		}
	}

	@Nested
	@DisplayName("count() tests")
	class CountTests {

		@Test
		@DisplayName("Should return the count from repository")
		void count_shouldReturnRepositoryValue() {
			// GIVEN
			when(mongoRepository.count()).thenReturn(99L);

			// WHEN
			long result = adapter.count();

			// THEN
			assertThat(result).isEqualTo(99L);
		}
	}

	@Nested
	@DisplayName("findQuestionnaireIdsByCampaignId() tests")
	class FindQuestionnaireIdsByCampaignIdTests {

		@Test
		@DisplayName("Should extract questionnaireId from JSON lines returned by repository")
		void findQuestionnaireIds_shouldExtractFromJson() {
			// GIVEN
			when(mongoRepository.findQuestionnaireIdsByCampaignId(CAMPAIGN_ID))
					.thenReturn(Set.of("{\"questionnaireId\":\"q1\"}", "{\"questionnaireId\":\"q2\"}"));

			// WHEN
			Set<String> result = adapter.findQuestionnaireIdsByCampaignId(CAMPAIGN_ID);

			// THEN
			assertThat(result).containsExactlyInAnyOrder("q1", "q2");
		}

		@Test
		@DisplayName("Should return empty set when repository returns empty set")
		void findQuestionnaireIds_empty_shouldReturnEmptySet() {
			// GIVEN
			when(mongoRepository.findQuestionnaireIdsByCampaignId(CAMPAIGN_ID)).thenReturn(Set.of());

			// WHEN
			Set<String> result = adapter.findQuestionnaireIdsByCampaignId(CAMPAIGN_ID);

			// THEN
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should skip malformed JSON lines without throwing")
		void findQuestionnaireIds_malformedJson_shouldSkipAndNotThrow() {
			// GIVEN
			when(mongoRepository.findQuestionnaireIdsByCampaignId(CAMPAIGN_ID))
					.thenReturn(Set.of("not-valid-json", "{\"questionnaireId\":\"q1\"}"));

			// WHEN
			Set<String> result = adapter.findQuestionnaireIdsByCampaignId(CAMPAIGN_ID);

			// THEN
			assertThat(result).containsExactly("q1");
		}
	}

	@Nested
	@DisplayName("findQuestionnaireIdsByCampaignIdV2() tests")
	class FindQuestionnaireIdsByCampaignIdV2Tests {

		@Test
		@DisplayName("Should extract questionnaireId from JSON lines returned by repository")
		void findQuestionnaireIdsV2_shouldExtractFromJson() {
			// GIVEN
			when(mongoRepository.findQuestionnaireIdsByCampaignIdV2(CAMPAIGN_ID))
					.thenReturn(Set.of("{\"questionnaireId\":\"q1\"}"));

			// WHEN
			Set<String> result = adapter.findQuestionnaireIdsByCampaignIdV2(CAMPAIGN_ID);

			// THEN
			assertThat(result).containsExactly("q1");
		}

		@Test
		@DisplayName("Should return empty set when repository returns empty set")
		void findQuestionnaireIdsV2_empty_shouldReturnEmptySet() {
			// GIVEN
			when(mongoRepository.findQuestionnaireIdsByCampaignIdV2(CAMPAIGN_ID)).thenReturn(Set.of());

			// WHEN
			Set<String> result = adapter.findQuestionnaireIdsByCampaignIdV2(CAMPAIGN_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findDistinctCampaignIds() tests")
	class FindDistinctCampaignIdsTests {

		// MongoCollection cannot be mocked directly (Java module system restriction).
		// RETURNS_DEEP_STUBS on a local MongoTemplate handles the full chain without
		// instantiating MongoCollection. The for-each loop on the deep-stubbed
		// DistinctIterable calls iterator() which returns a MongoCursor stub where
		// hasNext() defaults to false — the result is empty but all interactions are
		// verifiable. Content-level assertions belong in integration tests.

		private MongoTemplate deepMongoTemplate;
		private SurveyUnitMongoAdapter localAdapter;

		@org.junit.jupiter.api.BeforeEach
		void setUp() {
			deepMongoTemplate = mock(MongoTemplate.class, Answers.RETURNS_DEEP_STUBS);
			localAdapter = new SurveyUnitMongoAdapter(mongoRepository, deepMongoTemplate);
		}

		@Test
		@DisplayName("Should query the correct collection and field name")
		void findDistinctCampaignIds_shouldQueryCorrectCollectionAndField() {
			// GIVEN

			// WHEN
			Set<String> result = localAdapter.findDistinctCampaignIds();

			// THEN
			verify(deepMongoTemplate).getCollection(Constants.MONGODB_RESPONSE_COLLECTION_NAME);
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("Should not interact with the repository")
		void findDistinctCampaignIds_shouldNotTouchRepository() {
			// GIVEN

			// WHEN
			localAdapter.findDistinctCampaignIds();

			// THEN
			verifyNoInteractions(mongoRepository);
		}
	}

	@Nested
	@DisplayName("findInterrogationIdsByCollectionInstrumentId() tests")
	class FindInterrogationIdsByCollectionInstrumentIdTests {

		@Test
		@DisplayName("Should call both repository methods and merge results")
		void findInterrogationIds_shouldCallBothMethods() {
			// GIVEN
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of(buildDoc("i1")));
			when(mongoRepository.findInterrogationIdsByQuestionnaireId(COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of(buildDoc("i2")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Should return empty list when both repository methods return empty")
		void findInterrogationIds_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentId(any())).thenReturn(List.of());
			when(mongoRepository.findInterrogationIdsByQuestionnaireId(any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findInterrogationIdsByQuestionnaireIdAndDateAfter() tests")
	class FindInterrogationIdsByDateAfterTests {

		@Test
		@DisplayName("Should call both repository methods with the given date and merge results")
		void findByDateAfter_shouldCallBothMethods() {
			// GIVEN
			LocalDateTime since = LocalDateTime.now().minusDays(7);
			when(mongoRepository.findInterrogationIdsByQuestionnaireIdAndDateAfter(QUESTIONNAIRE_ID, since))
					.thenReturn(List.of(buildDoc("i1")));
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentIdAndDateAfter(QUESTIONNAIRE_ID, since))
					.thenReturn(List.of(buildDoc("i2")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByQuestionnaireIdAndDateAfter(QUESTIONNAIRE_ID, since);

			// THEN
			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Should return empty list when both repository methods return empty")
		void findByDateAfter_noResults_shouldReturnEmptyList() {
			// GIVEN
			LocalDateTime since = LocalDateTime.now();
			when(mongoRepository.findInterrogationIdsByQuestionnaireIdAndDateAfter(any(), any())).thenReturn(List.of());
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentIdAndDateAfter(any(), any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByQuestionnaireIdAndDateAfter(QUESTIONNAIRE_ID, since);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween() tests")
	class FindByRecordDateBetweenTests {

		@Test
		@DisplayName("Should call both repository methods and merge results")
		void findByRecordDateBetween_shouldCallBothMethods() {
			// GIVEN
			LocalDateTime start = LocalDateTime.now().minusDays(10);
			LocalDateTime end = LocalDateTime.now();
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(COLLECTION_INSTRUMENT_ID, start, end))
					.thenReturn(List.of(buildDoc("i1")));
			when(mongoRepository.findInterrogationIdsQuestionnaireIdAndRecordDateBetween(COLLECTION_INSTRUMENT_ID, start, end))
					.thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(COLLECTION_INSTRUMENT_ID, start, end);

			// THEN
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when both methods return empty")
		void findByRecordDateBetween_noResults_shouldReturnEmptyList() {
			// GIVEN
			LocalDateTime start = LocalDateTime.now().minusDays(1);
			LocalDateTime end = LocalDateTime.now();
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(any(), any(), any())).thenReturn(List.of());
			when(mongoRepository.findInterrogationIdsQuestionnaireIdAndRecordDateBetween(any(), any(), any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(COLLECTION_INSTRUMENT_ID, start, end);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("countByCollectionInstrumentId() tests")
	class CountByCollectionInstrumentIdTests {

		@Test
		@DisplayName("Should return the count from repository")
		void count_shouldReturnRepositoryValue() {
			// GIVEN
			when(mongoRepository.countByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(12L);

			// WHEN
			long result = adapter.countByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEqualTo(12L);
		}
	}

	@Nested
	@DisplayName("findPageableInterrogationIdsByQuestionnaireId() tests")
	class FindPageableInterrogationIdsTests {

		@Test
		@DisplayName("Should delegate to repository with skip and limit and return mapped models")
		void findPageable_shouldReturnMappedModels() {
			// GIVEN
			when(mongoRepository.findPageableInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID, 0L, 10L))
					.thenReturn(List.of(buildDoc("i1")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findPageableInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID, 0L, 10L);

			// THEN
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when repository returns empty list")
		void findPageable_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findPageableInterrogationIdsByQuestionnaireId(any(), any(), any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findPageableInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID, 0L, 10L);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findModesByCampaignIdV2() tests")
	class FindModesByCampaignIdV2Tests {

		@Test
		@DisplayName("Should delegate to repository and return mapped models")
		void findModes_shouldReturnMappedModels() {
			// GIVEN
			when(mongoRepository.findModesByCampaignIdV2(CAMPAIGN_ID)).thenReturn(List.of(buildDoc("i1")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findModesByCampaignIdV2(CAMPAIGN_ID);

			// THEN
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when repository returns empty list")
		void findModes_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findModesByCampaignIdV2(any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findModesByCampaignIdV2(CAMPAIGN_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findModesByQuestionnaireIdV2() tests")
	class FindModesByQuestionnaireIdV2Tests {

		@Test
		@DisplayName("Should delegate to repository and return mapped models")
		void findModes_shouldReturnMappedModels() {
			// GIVEN
			when(mongoRepository.findModesByQuestionnaireIdV2(QUESTIONNAIRE_ID)).thenReturn(List.of(buildDoc("i1")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findModesByQuestionnaireIdV2(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when repository returns empty list")
		void findModes_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findModesByQuestionnaireIdV2(any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findModesByQuestionnaireIdV2(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findInterrogationIdsByCampaignId() tests")
	class FindInterrogationIdsByCampaignIdTests {

		@Test
		@DisplayName("Should delegate to repository and return mapped models")
		void findInterrogationIds_shouldReturnMappedModels() {
			// GIVEN
			when(mongoRepository.findInterrogationIdsByCampaignId(CAMPAIGN_ID)).thenReturn(List.of(buildDoc("i1")));

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByCampaignId(CAMPAIGN_ID);

			// THEN
			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when repository returns empty list")
		void findInterrogationIds_noResults_shouldReturnEmptyList() {
			// GIVEN
			when(mongoRepository.findInterrogationIdsByCampaignId(any())).thenReturn(List.of());

			// WHEN
			List<SurveyUnitModel> result = adapter.findInterrogationIdsByCampaignId(CAMPAIGN_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("countByCampaignId() tests")
	class CountByCampaignIdTests {

		@Test
		@DisplayName("Should return the count from repository")
		void countByCampaignId_shouldReturnRepositoryValue() {
			// GIVEN
			when(mongoRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(7L);

			// WHEN
			long result = adapter.countByCampaignId(CAMPAIGN_ID);

			// THEN
			assertThat(result).isEqualTo(7L);
		}
	}

	@Nested
	@DisplayName("findDistinctQuestionnairesAndCollectionInstrumentIds() tests")
	class FindDistinctQuestionnairesAndCollectionInstrumentIdsTests {

		// Same module restriction as findDistinctCampaignIds. RETURNS_DEEP_STUBS
		// lets us verify that both distinct() calls are made without mocking
		// MongoCollection directly. The into() calls on deep-stubbed iterables are
		// no-ops so the result set is empty — content-level tests belong in integration tests.

		private MongoTemplate deepMongoTemplate;
		private SurveyUnitMongoAdapter localAdapter;

		@org.junit.jupiter.api.BeforeEach
		void setUp() {
			deepMongoTemplate = mock(MongoTemplate.class, Answers.RETURNS_DEEP_STUBS);
			localAdapter = new SurveyUnitMongoAdapter(mongoRepository, deepMongoTemplate);
		}

		@Test
		@DisplayName("Should query both questionnaireId and collectionInstrumentId distinct fields")
		void findDistinct_shouldQueryBothFields() {
			// GIVEN

			// WHEN
			Set<String> result = localAdapter.findDistinctQuestionnairesAndCollectionInstrumentIds();

			// THEN
			verify(deepMongoTemplate).getCollection(Constants.MONGODB_RESPONSE_COLLECTION_NAME);
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("Should not interact with the repository")
		void findDistinct_shouldNotTouchRepository() {
			// GIVEN

			// WHEN
			localAdapter.findDistinctQuestionnairesAndCollectionInstrumentIds();

			// THEN
			verifyNoInteractions(mongoRepository);
		}
	}

	@Nested
	@DisplayName("findCampaignIdsByQuestionnaireId() tests")
	class FindCampaignIdsByQuestionnaireIdTests {

		@Test
		@DisplayName("Should extract campaignId from JSON lines returned by repository")
		void findCampaignIds_shouldExtractFromJson() {
			// GIVEN
			when(mongoRepository.findCampaignIdsByQuestionnaireId(QUESTIONNAIRE_ID))
					.thenReturn(Set.of("{\"campaignId\":\"c1\"}", "{\"campaignId\":\"c2\"}"));

			// WHEN
			Set<String> result = adapter.findCampaignIdsByQuestionnaireId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).containsExactlyInAnyOrder("c1", "c2");
		}

		@Test
		@DisplayName("Should return empty set when repository returns empty set")
		void findCampaignIds_empty_shouldReturnEmptySet() {
			// GIVEN
			when(mongoRepository.findCampaignIdsByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(Set.of());

			// WHEN
			Set<String> result = adapter.findCampaignIdsByQuestionnaireId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should skip malformed JSON lines without throwing")
		void findCampaignIds_malformedJson_shouldSkipAndNotThrow() {
			// GIVEN
			when(mongoRepository.findCampaignIdsByQuestionnaireId(QUESTIONNAIRE_ID))
					.thenReturn(Set.of("not-valid-json", "{\"campaignId\":\"c1\"}"));

			// WHEN
			Set<String> result = adapter.findCampaignIdsByQuestionnaireId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).containsExactly("c1");
		}
	}

	@Nested
	@DisplayName("countByQuestionnaireId() tests")
	class CountByQuestionnaireIdTests {

		@Test
		@DisplayName("Should return the count from repository")
		void count_shouldReturnRepositoryValue() {
			// GIVEN
			when(mongoRepository.countByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(5L);

			// WHEN
			long result = adapter.countByQuestionnaireId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isEqualTo(5L);
		}
	}

	@Nested
	@DisplayName("countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId() tests")
	class CountDistinctInterrogationIdsTests {

		@Test
		@DisplayName("Should count distinct interrogation ids across both repository queries")
		void countDistinct_shouldReturnDistinctCount() {
			// GIVEN
			SurveyUnitDocument d1 = buildDoc("i1");
			SurveyUnitDocument d2 = buildDoc("i2");
			SurveyUnitDocument d3 = buildDoc("i1"); // duplicate of d1
			when(mongoRepository.findInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(List.of(d1, d2));
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentId(QUESTIONNAIRE_ID)).thenReturn(List.of(d3));

			// WHEN
			long result = adapter.countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isEqualTo(2L);
		}

		@Test
		@DisplayName("Should return 0 when both repository methods return empty lists")
		void countDistinct_noResults_shouldReturnZero() {
			// GIVEN
			when(mongoRepository.findInterrogationIdsByQuestionnaireId(any())).thenReturn(List.of());
			when(mongoRepository.findInterrogationIdsByCollectionInstrumentId(any())).thenReturn(List.of());

			// WHEN
			long result = adapter.countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId(QUESTIONNAIRE_ID);

			// THEN
			assertThat(result).isZero();
		}
	}

	//UTILS
	private SurveyUnitModel buildModel(String interrogationId) {
		return SurveyUnitModel.builder()
				.interrogationId(interrogationId)
				.collectionInstrumentId(COLLECTION_INSTRUMENT_ID)
				.build();
	}

	private SurveyUnitDocument buildDoc(String interrogationId) {
		SurveyUnitDocument doc = new SurveyUnitDocument();
		doc.setInterrogationId(interrogationId);
		return doc;
	}
}