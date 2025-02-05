package fr.insee.genesis.controller.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoopIdParser {

    private static final Pattern LOOP_PATTERN = Pattern.compile(".*_(\\d+)$");

    public static Integer extractIndex(String loopId) {
        if (loopId == null) {
            return null;
        }

        Matcher matcher = LOOP_PATTERN.matcher(loopId);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
}
