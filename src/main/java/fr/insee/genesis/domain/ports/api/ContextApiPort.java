package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.context.ContextModel;

public interface ContextApiPort {

    void addContext(ContextModel context
                     ) ;

}
