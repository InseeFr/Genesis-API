package fr.insee.genesis.stubs;

import fr.insee.genesis.configuration.Config;

public class ConfigStub extends Config {

	private static final String DATA_SOURCE = "/test/genesis";

	private static final String SPEC_SOURCE = "/test/kraftwerk";



	@Override
	public String getDataFolderSource() {
		return DATA_SOURCE;
	}

	@Override
	public String getSpecFolderSource() {
		return SPEC_SOURCE;
	}


}
