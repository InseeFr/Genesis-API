package fr.insee.genesis.domain.model.surveyunit;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Mode {

	WEB("WEB", "WEB", "CAWI"),
	TEL("TEL", "ENQ", "CATI"),
	F2F("F2F", "ENQ", "CAPI"),
	OTHER("OTHER", "", ""),
	PAPER("PAPER", "", "PAPI");

	@Nullable
	@Schema(nullable = true, type = "string", allowableValues = { "WEB", "TEL", "F2F", "PAPER", "OTHER" })
	private final String modeName;

	private final String folder;

	private final String jsonName;

	Mode(@Nullable String modeName, String folder, String jsonName) {
		this.modeName = modeName;
		this.folder = folder;
		this.jsonName = jsonName;
	}

    // lookup Maps
    private static final Map<String, Mode> BY_MODE_NAME = new HashMap<>();
    private static final Map<String, Mode> BY_JSON_NAME = new HashMap<>();
    private static final Map<String, Mode> BY_ANY_NAME = new HashMap<>();

    static {
        for (Mode mode : values()) {
            if (mode.modeName != null && !mode.modeName.isBlank()) {
                BY_MODE_NAME.put(mode.modeName.toUpperCase(), mode);
                BY_ANY_NAME.put(mode.modeName.toUpperCase(), mode);
            }
            if (mode.jsonName != null && !mode.jsonName.isBlank()) {
                BY_JSON_NAME.put(mode.jsonName.toUpperCase(), mode);
                BY_ANY_NAME.put(mode.jsonName.toUpperCase(), mode);
            }
        }
    }

    @JsonCreator
    public static Mode fromString(String value) {
        if (value == null){ return null;}

        Mode mode = BY_ANY_NAME.get(value.trim().toUpperCase());
        if (mode != null) { return mode;  }

        throw new IllegalArgumentException("Invalid Mode: " + value);
    }

    public static Mode getEnumFromModeName(@Nullable String modeName) {
        if (modeName == null) { return null;     }
        return BY_MODE_NAME.get(modeName.trim().toUpperCase());
    }

    public static Mode getEnumFromJsonName(@Nullable String jsonName) {
        if (jsonName == null) {  return null;   }
        return BY_JSON_NAME.get(jsonName.trim().toUpperCase());
    }

}
