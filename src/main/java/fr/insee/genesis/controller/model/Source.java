package fr.insee.genesis.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
public class Source {

	@Getter
	@Setter
	private String sourceLabel;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Source source1 = (Source) o;
		return Objects.equals(sourceLabel, source1.sourceLabel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sourceLabel);
	}
}
