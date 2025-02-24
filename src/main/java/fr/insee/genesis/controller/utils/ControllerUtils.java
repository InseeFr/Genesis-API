package fr.insee.genesis.controller.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;

@Component
public class ControllerUtils {

	private final FileUtils fileUtils;

	@Autowired
	public ControllerUtils(FileUtils fileUtils) {
		this.fileUtils = fileUtils;
	}


	/**
	 * If a mode is specified, we treat only this mode.
	 * If no mode is specified, we treat all modes in the campaign.
	 * If no mode is specified and no specs are found, we return an error
	 * @param campaign campaign id to get modes
	 * @param modeSpecified a Mode to use, null if we want all modes available
	 * @return a list with the mode in modeSpecified or all modes if null
	 * @throws GenesisException if error in specs structure
	 */
	public List<Mode> getModesList(String campaign, Mode modeSpecified) throws GenesisException {
		if (modeSpecified != null){
			return Collections.singletonList(modeSpecified);
		}
		List<Mode> modes = new ArrayList<>();
		String specFolder = fileUtils.getSpecFolder(campaign);
		List<String> specFolders = fileUtils.listFolders(specFolder);
		if (specFolders.isEmpty()) {
			throw new GenesisException(404, "No specification folder found " + specFolder);
		}
		specFolders.forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		if (modes.contains(Mode.F2F) && modes.contains(Mode.TEL)) {
			throw new GenesisException(409, "Cannot treat simultaneously TEL and FAF modes");
		}
		return modes;
	}

}
