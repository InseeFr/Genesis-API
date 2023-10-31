package fr.insee.genesis.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class GenesisException extends Exception{

    /**
	 * 
	 */
	private static final long serialVersionUID = 3356078796351491095L;

	@Getter
    private final int status;

    @Getter
    private final String message;

}
