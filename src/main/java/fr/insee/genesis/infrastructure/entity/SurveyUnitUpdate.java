package fr.insee.genesis.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table(name= "responses")
@Entity
public class SurveyUnitUpdate implements Serializable {

    @Serial
    private static final long serialVersionUID = 5662459807985554372L;
    @Id
    @Column(name = "idupdate")
    @GeneratedValue(generator = "seq_su", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_su", allocationSize = 1)
    private Long idUpdate;

    @Column(name = "idcampagne")
    private String idCampaign;

    @Column(name = "idquestionnaire")
    private String idQuestionnaire;

    @Column(name = "idue")
    private String idUE;

    @Column(name = "etat")
    private String state;

    private String source;

    private LocalDateTime date;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "json")
    private List<VariableState> variablesUpdate;

}
