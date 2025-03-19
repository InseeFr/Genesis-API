package fr.insee.genesis.controller.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;

@Component
@Slf4j
public class ControllerUtils {

	private final FileUtils fileUtils;

	@Autowired
	public ControllerUtils(FileUtils fileUtils) {
		this.fileUtils = fileUtils;
	}

	public List<Mode> getModesList(String campaign, Mode modeSpecified) throws GenesisException {
		// If a mode is specified, we treat only this mode.
		// If no mode is specified, we treat all modes in the campaign.
		// If no node is specified and no specs are found, we return an error
		if (modeSpecified != null){
			return Collections.singletonList(modeSpecified);
		}
		List<Mode> modes = new ArrayList<>();
		String specFolder = fileUtils.getSpecFolder(campaign);
		List<String> modeSpecFolders = fileUtils.listFolders(specFolder);
		if (modeSpecFolders.isEmpty()) {
			throw new GenesisException(404, "No specification folder found " + specFolder);
		}
		for(String modeSpecFolder : modeSpecFolders){
			if(Mode.getEnumFromModeName(modeSpecFolder) == null) {
				log.warn("There is an invalid mode folder name in spec folder : {}", modeSpecFolder);
				continue;
			}
			modes.add(Mode.getEnumFromModeName(modeSpecFolder));
		}
		if (modes.contains(Mode.F2F) && modes.contains(Mode.TEL)) {
			throw new GenesisException(409, "Cannot treat simultaneously TEL and FAF modes");
		}
		return modes;
	}

}
