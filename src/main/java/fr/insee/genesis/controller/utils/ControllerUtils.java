package fr.insee.genesis.controller.utils;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.ModesConflictException;
import fr.insee.genesis.exceptions.UndefinedModesException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Note: this class should be moved in the domain service layer.

@Component
@Slf4j
public class ControllerUtils {

	private final FileUtils fileUtils;

	@Autowired
	public ControllerUtils(FileUtils fileUtils) {
		this.fileUtils = fileUtils;
	}

	/**
	 * If a mode is specified, we treat only this mode.
	 * If no mode is specified, we treat all modes in the questionnaireId.
	 * If no mode is specified and no specs are found, we return an error
	 * @param questionnaireId questionnaireId id to get modes
	 * @param modeSpecified a Mode to use, null if we want all modes available
	 * @return a list with the mode in modeSpecified or all modes if null
	 */
	public List<Mode> getModesList(String questionnaireId, Mode modeSpecified) {
		if (modeSpecified != null){
			return Collections.singletonList(modeSpecified);
		}
		List<Mode> modes = new ArrayList<>();
		String specFolder = fileUtils.getSpecFolder(questionnaireId);
		List<String> modeSpecFolders = fileUtils.listFolders(specFolder);
		if (modeSpecFolders.isEmpty()) {
			throw new UndefinedModesException("No specification folder found " + specFolder);
		}
		for(String modeSpecFolder : modeSpecFolders){
			if(Mode.getEnumFromModeName(modeSpecFolder) == null) {
				log.warn("There is an invalid mode folder name in spec folder : {}", modeSpecFolder);
				continue;
			}
			modes.add(Mode.getEnumFromModeName(modeSpecFolder));
		}
		if (modes.contains(Mode.F2F) && modes.contains(Mode.TEL)) {
			throw new ModesConflictException("Cannot treat simultaneously TEL and FAF modes");
		}
		return modes;
	}

	/**
	 * Returns the applicable modes for the collection instrument with the given identifier.
	 * @param collectionInstrumentId Collection instrument identifier.
	 * @return A list of modes.
	 */
	public List<Mode> getModesList(String collectionInstrumentId) {
		return getModesList(collectionInstrumentId, null);
	}

}
