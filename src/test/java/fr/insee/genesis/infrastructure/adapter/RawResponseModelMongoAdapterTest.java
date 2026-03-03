package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.infrastructure.repository.RawResponseRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RawResponseModelMongoAdapterTest {
	
    static RawResponseRepository mongoRepository = Mockito.mock(RawResponseRepository.class);
	static RawResponseMongoAdapter rawResponseMongoAdapter;

	@BeforeEach
	void setUp() {
		rawResponseMongoAdapter = new RawResponseMongoAdapter(mongoRepository, null);
	}

	@Test
	void existsByInterrogationId_shouldReturnTrue_whenRepositoryReturnsTrue() {
		// Given
		when(mongoRepository.existsByInterrogationId("UE1100000001")).thenReturn(true);

		// When
		boolean exists = rawResponseMongoAdapter.existsByInterrogationId("UE1100000001");

		// Then
		Assertions.assertThat(exists).isTrue();
		Mockito.verify(mongoRepository).existsByInterrogationId("UE1100000001");
	}

	@Test
	void existsByInterrogationId_shouldReturnFalse_whenRepositoryReturnsFalse() {
		// Given
		when(mongoRepository.existsByInterrogationId("UE9999999999")).thenReturn(false);

		// When
		boolean exists = rawResponseMongoAdapter.existsByInterrogationId("UE9999999999");

		// Then
		Assertions.assertThat(exists).isFalse();
		Mockito.verify(mongoRepository).existsByInterrogationId("UE9999999999");
	}
}
