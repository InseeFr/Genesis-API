package fr.insee.genesis.domain.model.surveyunit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public enum Mode {

	WEB("WEB", "WEB"),TEL("TEL", "ENQ"),F2F("F2F", "ENQ"),OTHER("OTHER", ""),PAPER("PAPER", "");

	@Nullable
	@Schema(nullable = true, type = "string", allowableValues = { "WEB", "TEL", "F2F", "PAPER", "OTHER" })
	private final String modeName;

	private final String folder;

	Mode(@Nullable String modeName, String folder) {
		this.modeName = modeName;
		this.folder = folder;
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

}
