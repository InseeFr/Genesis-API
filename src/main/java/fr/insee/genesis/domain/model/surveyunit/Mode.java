package fr.insee.genesis.domain.model.surveyunit;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.lang.Nullable;

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

	@JsonCreator
	public static Mode fromString(String value) {
		if (value == null) return null;

		for (Mode m : values()) {
			if (value.equalsIgnoreCase(m.modeName) ||
					value.equalsIgnoreCase(m.jsonName)) {
				return m;
			}
		}
		throw new IllegalArgumentException("Invalid Mode: " + value);
	}

	public static Mode getEnumFromModeName(String modeName) {
		if (modeName == null){
			return null;
		}
		for (Mode mode : Mode.values()) {
			if (modeName.equals(mode.getModeName())) {
				return mode;
			}
		}
		return null;
	}

	public static Mode getEnumFromJsonName(String modeName) {
		if (modeName == null){
			return null;
		}
		for (Mode mode : Mode.values()) {
			if (modeName.equals(mode.getJsonName())) {
				return mode;
			}
		}
		return null;
	}

}
