package fr.insee.genesis.exceptions;

public class ReviewDisabledException extends RuntimeException {
    public ReviewDisabledException() {
        super("Review is disabled for that partition");
    }
}
