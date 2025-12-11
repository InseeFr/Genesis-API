package fr.insee.genesis.exceptions;

public class QuestionnaireNotFoundException extends RuntimeException {
    public QuestionnaireNotFoundException(String questionnaireId) {
        super("No questionnaire found with id: " + questionnaireId);
    }
}
