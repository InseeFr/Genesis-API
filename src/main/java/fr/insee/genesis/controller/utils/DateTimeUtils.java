package fr.insee.genesis.controller.utils;

import fr.insee.genesis.exceptions.InvalidDateIntervalException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeUtils {

    private static final ZoneId FRANCE_ZONE = ZoneId.of("Europe/Paris");

    private DateTimeUtils() {
    }

    public static Instant resolveInstant(
            Instant utcDate,
            LocalDateTime localDate
    ) {
        if (utcDate != null && localDate != null) {
            throw new InvalidDateIntervalException(
                    "Use either UTC date or local date, not both"
            );
        }

        if (localDate != null) {
            return localDate
                    .atZone(FRANCE_ZONE)
                    .toInstant();
        }

        return utcDate;
    }

    public static ZonedDateTime toFranceDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(FRANCE_ZONE);
    }
}
