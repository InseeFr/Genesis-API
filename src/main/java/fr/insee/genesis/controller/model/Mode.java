package fr.insee.genesis.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.Nullable;

public enum Mode {

	WEB("Web"),TEL("Tel"),FAF("FaF");

	@Nullable
	@Schema(nullable = true, type = "string", allowableValues = { "Web", "Tel", "FaF" })
	private final String mode;

	Mode(String mode) {
		this.mode = mode;
	}

	public String getMode() {
		return mode;
	}
}
