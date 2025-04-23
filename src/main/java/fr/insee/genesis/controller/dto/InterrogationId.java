package fr.insee.genesis.controller.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class InterrogationId {

	private String identifier;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InterrogationId that = (InterrogationId) o;
		return Objects.equals(identifier, that.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier);
	}

	public String getInterrogationId() {
		return identifier;
	}

	public void setInterrogationId(String identifier) {
		this.identifier = identifier;
	}
}
