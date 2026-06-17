package fr.insee.genesis.controller.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    void shouldReturnUtcDateWhenUtcDateIsProvided() {
        Instant utcDate = Instant.parse("2026-06-10T12:00:00Z");

        Instant result = DateTimeUtils.resolveInstant(utcDate, null);

        assertEquals(utcDate, result);
    }

    @Test
    void shouldConvertLocalDateToInstantWhenLocalDateIsProvided() {
        LocalDateTime localDate = LocalDateTime.of(2026, 6, 10, 14, 0);

        Instant result = DateTimeUtils.resolveInstant(null, localDate);

        assertEquals(
                localDate.atZone(ZoneId.of("Europe/Paris")).toInstant(),
                result
        );
    }

    @Test
    void shouldReturnNullWhenNoDateIsProvided() {
        Instant result = DateTimeUtils.resolveInstant(null, null);

        assertNull(result);
    }

    @Test
    void shouldConvertInstantToFranceDateTime() {
        Instant instant = Instant.parse("2026-06-10T12:00:00Z");

        ZonedDateTime result = DateTimeUtils.toFranceDateTime(instant);

        assertEquals(
                instant.atZone(ZoneId.of("Europe/Paris")),
                result
        );
    }

    @Test
    void shouldReturnNullWhenInstantIsNull() {
        assertNull(DateTimeUtils.toFranceDateTime(null));
    }
}