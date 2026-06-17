package fr.insee.genesis.domain.ports.api;

import fr.insee.modelefiliere.RawResponseDto;

public interface RawResponseInputApiPort {
    void saveAsRawJson(RawResponseDto dto);
}
