package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import fr.insee.genesis.infrastructure.repository.RawResponseRepository;
import fr.insee.modelefiliere.ModeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RawResponseMongoAdapter tests")
class RawResponseMongoAdapterTest {

	private static final String COLLECTION_INSTRUMENT_ID = "instrument-123";
	private static final String INTERROGATION_ID = "interrogation-789";
	private static final Mode MODE = Mode.WEB;

	@Mock
	private RawResponseRepository repository;

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private RawResponseMongoAdapter adapter;

	@Nested
	@DisplayName("findRawResponses() tests")
	class FindRawResponsesTests {

		@Test
		@DisplayName("Should delegate to repository using mode jsonName and return mapped models")
		void findRawResponses_shouldCallRepositoryWithJsonNameAndReturnModels() {
			// GIVEN
			List<String> interrogationIds = List.of("i1", "i2");
			when(repository.findByCollectionInstrumentIdAndModeAndInterrogationIdList(
					COLLECTION_INSTRUMENT_ID, MODE.getJsonName(), interrogationIds))
					.thenReturn(List.of(getDocument()));

			// WHEN
			List<RawResponseModel> result = adapter.findRawResponses(COLLECTION_INSTRUMENT_ID, MODE, interrogationIds);

			// THEN
			verify(repository).findByCollectionInstrumentIdAndModeAndInterrogationIdList(
					COLLECTION_INSTRUMENT_ID, MODE.getJsonName(), interrogationIds);
			assertThat(result).isNotNull().hasSize(1);
		}

		@Test
		@DisplayName("Should use mode.getJsonName() and not mode.name() or mode.getModeName()")
		void findRawResponses_shouldUseJsonName() {
			// GIVEN
			when(repository.findByCollectionInstrumentIdAndModeAndInterrogationIdList(any(), any(), any()))
					.thenReturn(List.of());

			// WHEN
			adapter.findRawResponses(COLLECTION_INSTRUMENT_ID, MODE, List.of());

			// THEN
			ArgumentCaptor<String> modeCaptor = ArgumentCaptor.forClass(String.class);
			verify(repository).findByCollectionInstrumentIdAndModeAndInterrogationIdList(
					any(), modeCaptor.capture(), any());
			assertThat(modeCaptor.getValue()).isEqualTo(MODE.getJsonName());
		}

		@Test
		@DisplayName("Should return empty list when repository returns no documents")
		void findRawResponses_noDocuments_shouldReturnEmptyList() {
			// GIVEN
			when(repository.findByCollectionInstrumentIdAndModeAndInterrogationIdList(any(), any(), any()))
					.thenReturn(List.of());

			// WHEN
			List<RawResponseModel> result = adapter.findRawResponses(COLLECTION_INSTRUMENT_ID, MODE, List.of());

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findRawResponsesByInterrogationID() tests")
	class FindRawResponsesByInterrogationIdTests {

		@Test
		@DisplayName("Should delegate to repository and return mapped models")
		void findRawResponsesByInterrogationId_shouldReturnMappedModels() {
			// GIVEN
			when(repository.findByInterrogationId(INTERROGATION_ID))
					.thenReturn(List.of(getDocument()));

			// WHEN
			List<RawResponseModel> result = adapter.findRawResponsesByInterrogationID(INTERROGATION_ID);

			// THEN
			verify(repository).findByInterrogationId(INTERROGATION_ID);
			assertThat(result).isNotNull().hasSize(1);
		}

		@Test
		@DisplayName("Should return empty list when no document found")
		void findRawResponsesByInterrogationId_noDocument_shouldReturnEmptyList() {
			// GIVEN
			when(repository.findByInterrogationId(INTERROGATION_ID)).thenReturn(List.of());

			// WHEN
			List<RawResponseModel> result = adapter.findRawResponsesByInterrogationID(INTERROGATION_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("updateProcessDates() tests")
	class UpdateProcessDatesTests {

		@Test
		@DisplayName("Should call mongoTemplate.updateMulti() on the correct collection")
		void updateProcessDates_shouldCallUpdateMultiOnCorrectCollection() {
			// GIVEN
			Set<String> interrogationIds = Set.of("i1", "i2");

			// WHEN
			adapter.updateProcessDates(COLLECTION_INSTRUMENT_ID, interrogationIds);

			// THEN
			verify(mongoTemplate).updateMulti(
					any(Query.class),
					any(Update.class),
					eq(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME)
			);
		}

		@Test
		@DisplayName("Should call updateMulti() exactly once")
		void updateProcessDates_shouldCallUpdateMultiOnce() {
			// GIVEN

			// WHEN
			adapter.updateProcessDates(COLLECTION_INSTRUMENT_ID, Set.of("i1"));

			// THEN
			verify(mongoTemplate, times(1)).updateMulti(any(), any(), any(String.class));
		}

		@Test
		@DisplayName("Should not interact with the repository")
		void updateProcessDates_shouldNotTouchRepository() {
			// GIVEN

			// WHEN
			adapter.updateProcessDates(COLLECTION_INSTRUMENT_ID, Set.of("i1"));

			// THEN
			verifyNoInteractions(repository);
		}
	}

	@Nested
	@DisplayName("getUnprocessedCollectionIds() tests")
	class GetUnprocessedCollectionIdsTests {

		@Test
		@DisplayName("Should return list from repository")
		void getUnprocessedCollectionIds_shouldReturnRepositoryValue() {
			// GIVEN
			when(repository.findDistinctCollectionInstrumentIdByProcessDateIsNull())
					.thenReturn(List.of("id1", "id2"));

			// WHEN
			List<String> result = adapter.getUnprocessedCollectionIds();

			// THEN
			assertThat(result).containsExactly("id1", "id2");
		}

		@Test
		@DisplayName("Should return empty list when no unprocessed collection ids exist")
		void getUnprocessedCollectionIds_empty_shouldReturnEmptyList() {
			// GIVEN
			when(repository.findDistinctCollectionInstrumentIdByProcessDateIsNull()).thenReturn(List.of());

			// WHEN
			List<String> result = adapter.getUnprocessedCollectionIds();

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findUnprocessedInterrogationIdsByCollectionInstrumentId() tests")
	class FindUnprocessedInterrogationIdsTests {

		@Test
		@DisplayName("Should return a Set of unprocessed interrogation ids")
		void findUnprocessed_shouldReturnSet() {
			// GIVEN
			when(repository.findInterrogationIdByCollectionInstrumentIdAndProcessDateIsNull(COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of("i1", "i2", "i3"));

			// WHEN
			Set<String> result = adapter.findUnprocessedInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).containsExactlyInAnyOrder("i1", "i2", "i3");
		}

		@Test
		@DisplayName("Should deduplicate ids returned by repository")
		void findUnprocessed_duplicates_shouldDeduplicate() {
			// GIVEN
			when(repository.findInterrogationIdByCollectionInstrumentIdAndProcessDateIsNull(COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of("i1", "i1", "i2"));

			// WHEN
			Set<String> result = adapter.findUnprocessedInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).hasSize(2).containsExactlyInAnyOrder("i1", "i2");
		}

		@Test
		@DisplayName("Should return empty set when repository returns empty list")
		void findUnprocessed_empty_shouldReturnEmptySet() {
			// GIVEN
			when(repository.findInterrogationIdByCollectionInstrumentIdAndProcessDateIsNull(COLLECTION_INSTRUMENT_ID))
					.thenReturn(List.of());

			// WHEN
			Set<String> result = adapter.findUnprocessedInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findModesByCollectionInstrument() tests")
	class FindModesByCollectionInstrumentTests {

		@Test
		@DisplayName("Should delegate to repository and return its result")
		void findModes_shouldReturnRepositoryValue() {
			// GIVEN
			List<ModeDto> modes = List.of(mock(ModeDto.class));
			when(repository.findModesByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(modes);

			// WHEN
			List<ModeDto> result = adapter.findModesByCollectionInstrument(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEqualTo(modes);
			verify(repository).findModesByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
		}

		@Test
		@DisplayName("Should return empty list when no mode found")
		void findModes_noModes_shouldReturnEmptyList() {
			// GIVEN
			when(repository.findModesByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of());

			// WHEN
			List<ModeDto> result = adapter.findModesByCollectionInstrument(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findByCampaignIdAndDate() tests")
	class FindByCampaignIdAndDateTests {
		private static final String CAMPAIGN_ID = "campaign-456";

		@Test
		@DisplayName("Should return a Page of mapped models")
		void findByCampaignIdAndDate_shouldReturnPage() {
			// GIVEN
			Pageable pageable = PageRequest.of(0, 10);
			Instant start = Instant.now().minusSeconds(3600);
			Instant end = Instant.now();
			Page<RawResponseDocument> docPage = new PageImpl<>(List.of(getDocument()), pageable, 1);
			when(repository.findByCampaignIdAndDate(CAMPAIGN_ID, start, end, pageable)).thenReturn(docPage);

			// WHEN
			Page<RawResponseModel> result = adapter.findByCampaignIdAndDate(CAMPAIGN_ID, start, end, pageable);

			// THEN
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getTotalElements()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should preserve pagination metadata from repository page")
		void findByCampaignIdAndDate_shouldPreservePaginationMetadata() {
			// GIVEN
			Pageable pageable = PageRequest.of(2, 5);
			Instant start = Instant.EPOCH;
			Instant end = Instant.now();
			when(repository.findByCampaignIdAndDate(CAMPAIGN_ID, start, end, pageable))
					.thenReturn(new PageImpl<>(List.of(), pageable, 77));

			// WHEN
			Page<RawResponseModel> result = adapter.findByCampaignIdAndDate(CAMPAIGN_ID, start, end, pageable);

			// THEN
			assertThat(result.getTotalElements()).isEqualTo(77);
			assertThat(result.getPageable()).isEqualTo(pageable);
		}

		@Test
		@DisplayName("Should return empty page when repository returns empty page")
		void findByCampaignIdAndDate_emptyPage_shouldReturnEmptyPage() {
			// GIVEN
			Pageable pageable = PageRequest.of(0, 10);
			when(repository.findByCampaignIdAndDate(any(), any(), any(), any()))
					.thenReturn(new PageImpl<>(List.of(), pageable, 0));

			// WHEN
			Page<RawResponseModel> result = adapter.findByCampaignIdAndDate(CAMPAIGN_ID, Instant.EPOCH, Instant.now(), pageable);

			// THEN
			assertThat(result.getContent()).isEmpty();
			assertThat(result.getTotalElements()).isZero();
		}
	}

	@Nested
	@DisplayName("countByCollectionInstrumentId() tests")
	class CountByCollectionInstrumentIdTests {

		@Test
		@DisplayName("Should return the count from repository")
		void count_shouldReturnRepositoryValue() {
			// GIVEN
			when(repository.countByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(42L);

			// WHEN
			long result = adapter.countByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEqualTo(42L);
		}

		@Test
		@DisplayName("Should return 0 when repository returns 0")
		void count_zero_shouldReturnZero() {
			// GIVEN
			when(repository.countByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(0L);

			// WHEN
			long result = adapter.countByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isZero();
		}
	}

	@Nested
	@DisplayName("findDistinctCollectionInstrumentIds() tests")
	class FindDistinctCollectionInstrumentIdsTests {

		@Test
		@DisplayName("Should return a Set of distinct ids from repository")
		void findDistinct_shouldReturnSet() {
			// GIVEN
			when(repository.findDistinctCollectionInstrumentId())
					.thenReturn(List.of("id1", "id2", "id3"));

			// WHEN
			Set<String> result = adapter.findDistinctCollectionInstrumentIds();

			// THEN
			assertThat(result).containsExactlyInAnyOrder("id1", "id2", "id3");
		}

		@Test
		@DisplayName("Should deduplicate ids when repository returns duplicates")
		void findDistinct_duplicates_shouldDeduplicate() {
			// GIVEN
			when(repository.findDistinctCollectionInstrumentId())
					.thenReturn(List.of("id1", "id1", "id2"));

			// WHEN
			Set<String> result = adapter.findDistinctCollectionInstrumentIds();

			// THEN
			assertThat(result).hasSize(2).containsExactlyInAnyOrder("id1", "id2");
		}

		@Test
		@DisplayName("Should return empty set when repository returns empty list")
		void findDistinct_empty_shouldReturnEmptySet() {
			// GIVEN
			when(repository.findDistinctCollectionInstrumentId()).thenReturn(List.of());

			// WHEN
			Set<String> result = adapter.findDistinctCollectionInstrumentIds();

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("countDistinctInterrogationIdsByCollectionInstrumentId() tests")
	class CountDistinctInterrogationIdsTests {

		@Test
		@DisplayName("Should return the count from repository")
		void countDistinct_shouldReturnRepositoryValue() {
			// GIVEN
			when(repository.countDistinctInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID))
					.thenReturn(15L);

			// WHEN
			long result = adapter.countDistinctInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isEqualTo(15L);
		}

		@Test
		@DisplayName("Should return 0 when repository returns null (null-safety)")
		void countDistinct_nullFromRepository_shouldReturnZero() {
			// GIVEN
			when(repository.countDistinctInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID))
					.thenReturn(null);

			// WHEN
			long result = adapter.countDistinctInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isZero();
		}

		@Test
		@DisplayName("Should return 0 when repository returns 0")
		void countDistinct_zero_shouldReturnZero() {
			// GIVEN
			when(repository.countDistinctInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID))
					.thenReturn(0L);

			// WHEN
			long result = adapter.countDistinctInterrogationIdsByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

			// THEN
			assertThat(result).isZero();
		}
	}

	@Nested
	@DisplayName("findByCollectionInstrumentId(pageable) tests")
	class FindByCollectionInstrumentIdPagedTests {

		@Test
		@DisplayName("Should return a Page of mapped models")
		void findPaged_shouldReturnPage() {
			// GIVEN
			Pageable pageable = PageRequest.of(0, 10);
			Page<RawResponseDocument> docPage = new PageImpl<>(List.of(getDocument()), pageable, 1);
			when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, pageable)).thenReturn(docPage);

			// WHEN
			Page<RawResponseModel> result = adapter.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, pageable);

			// THEN
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getTotalElements()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should preserve pagination metadata from repository page")
		void findPaged_shouldPreservePaginationMetadata() {
			// GIVEN
			Pageable pageable = PageRequest.of(3, 5);
			when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, pageable))
					.thenReturn(new PageImpl<>(List.of(), pageable, 50));

			// WHEN
			Page<RawResponseModel> result = adapter.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, pageable);

			// THEN
			assertThat(result.getTotalElements()).isEqualTo(50);
			assertThat(result.getPageable()).isEqualTo(pageable);
		}

		@Test
		@DisplayName("Should return empty page when repository returns empty page")
		void findPaged_emptyPage_shouldReturnEmptyPage() {
			// GIVEN
			Pageable pageable = PageRequest.of(0, 10);
			when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, pageable))
					.thenReturn(new PageImpl<>(List.of(), pageable, 0));

			// WHEN
			Page<RawResponseModel> result = adapter.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, pageable);

			// THEN
			assertThat(result.getContent()).isEmpty();
			assertThat(result.getTotalElements()).isZero();
		}
	}

	@Nested
	@DisplayName("existsByInterrogationId() tests")
	class ExistsByInterrogationIdTests {

		@Test
		@DisplayName("Should return true when repository finds a match")
		void exists_found_shouldReturnTrue() {
			// GIVEN
			when(repository.existsByInterrogationId(INTERROGATION_ID)).thenReturn(true);

			// WHEN
			boolean result = adapter.existsByInterrogationId(INTERROGATION_ID);

			// THEN
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when repository finds no match")
		void exists_notFound_shouldReturnFalse() {
			// GIVEN
			when(repository.existsByInterrogationId(INTERROGATION_ID)).thenReturn(false);

			// WHEN
			boolean result = adapter.existsByInterrogationId(INTERROGATION_ID);

			// THEN
			assertThat(result).isFalse();
		}
	}

	//UTILS
	private RawResponseDocument getDocument() {
		return new RawResponseDocument(
				null,
				INTERROGATION_ID,
				COLLECTION_INSTRUMENT_ID,
				MODE.toString(),
				new HashMap<>(),
				LocalDateTime.now(),
				LocalDateTime.now()
		);
	}

    @Nested
    @DisplayName("findRawResponseByCollectionInstrumentIdAndInterrogationId() tests")
    class FindRawResponseByCollectionInstrumentIdAndInterrogationIdTests {

        @Test
        @DisplayName("Should delegate to repository and return mapped model")
        void findRawData_shouldReturnMappedModel() {
            //GIVEN
            when(repository.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(getDocument());

            //WHEN
            RawResponseModel result = adapter.findRawResponseByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);

            //THEN
            assertThat(result).isNotNull();
            verify(repository).findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);
        }

        @Test
        @DisplayName("Should return null when no document found")
        void findRawData_noDocument_shouldReturnNull() {
            //GIVEN
            when(repository.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID)).thenReturn(null);

            //WHEN + THEN
            assertThat(adapter.findRawResponseByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID)).isNull();
        }
    }

}