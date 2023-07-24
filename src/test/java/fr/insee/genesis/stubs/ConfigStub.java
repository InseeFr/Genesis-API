package fr.insee.genesis.stubs;

import fr.insee.genesis.configuration.Config;

public class ConfigStub extends Config {

	private static final String GENESIS_FOLDER = "/test/genesis";

	@Override
	public String getGenesisFolder() {
		return GENESIS_FOLDER;
	}

}
