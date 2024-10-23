package fr.insee.genesis.controller.sources.contextJson;

import lombok.Data;

@Data
public class ContextJsonMetadata {

    private int annee;
    private String periodicite;
    private String periode;
    private String operationId;
    private String operationLabelCourt;
    private String serieId;
    private String serieLabelCourt;
    private String objectifsCourts;
    private String objectifsLongs;
    private boolean caractereObligatoire;
    private boolean qualiteStatistique;
    private boolean testNonLabellise;
      private String ministereTutelle;
    private boolean parutionJo;
    private String dateParutionJo;
    private String responsableOperationnel;
    private String responsableTraitement;
    private String cnisUrl;
    private String diffusionUrl;
    private String noticeUrl;
    private String specimenUrl;
    private String proprietaireId;
    private String proprietaireLabel;
    private String proprietaireLogo;
    private String assistanceNiveau2Id;
    private String assistanceNiveau2Label;
    private String assistanceNiveau2Tel;
    private String assitanceNiveau2Mail;
    private String asssistanceNiveau2Pays;
    private String assistanceNiveau2NumeroVoie;
    private String assistanceNiveau2NomVoie;
    private String assistanceNiveau2Commune;
    private String assistanceNiveau2CodePostal;

}
