package fr.insee.genesis.controller.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoopIdParserTest {

    @Test
    void shouldReturn42(){
        assertEquals(42, LoopIdParser.extractIndex("BOUCLE_42"));
    }

    @Test
    void shouldReturnWhen9(){
        assertEquals(9, LoopIdParser.extractIndex("MULTIPLE_UNDERSCORE_BEFORE_SUFFIX_9"));
    }

    @Test
    void shouldReturnNullIfNoSuffix(){
        assertNull(LoopIdParser.extractIndex("NO_INDEX"));
    }

}