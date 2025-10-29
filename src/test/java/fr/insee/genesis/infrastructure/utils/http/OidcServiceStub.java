package fr.insee.genesis.infrastructure.utils.http;

import fr.insee.genesis.configuration.Config;

public class OidcServiceStub extends OidcService{

    String token  ;

    public OidcServiceStub(Config config) {
        super(config);
    }

    public OidcServiceStub(String token) {
        super(new Config(token));
        this.token = token;
    }

    @Override public String getServiceAccountToken() {
        return token; }
}
