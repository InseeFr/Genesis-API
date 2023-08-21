package fr.insee.genesis.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.Nullable;

public enum Mode {

	WEB("WEB", "WEB"),TEL("TEL", "ENQ"),FAF("FAF", "ENQ");

	@Nullable
	@Schema(nullable = true, type = "string", allowableValues = { "WEB", "TEL", "FAF" })
	private final String modeName;

	private final String folder;

	Mode(String modeName, String folder) {
		this.modeName = modeName;
		this.folder = folder;
	}

	public static Mode getEnumFromModeName(String modeName) {
		for (Mode mode : Mode.values()) {
			if (mode.getModeName().equals(modeName)) {
				return mode;
			}
		}
		return null;
	}

	public String getModeName() {
		return modeName;
	}

	public String getFolder() {
		return folder;
	}
}
