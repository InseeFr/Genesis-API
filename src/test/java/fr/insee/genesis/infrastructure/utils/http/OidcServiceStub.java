package fr.insee.genesis.infrastructure.utils.http;

import fr.insee.genesis.configuration.Config;

import java.io.IOException;

public class OidcServiceStub extends OidcService{

    private String token;

    private String refreshedToken;
    private boolean throwOnGet;
    private boolean throwOnRefresh;
    private int nbCalls =0;
    private int nbRefresh =0;


    public OidcServiceStub(String token) {
        super(new Config(token));
        this.token = token;
    }

    public OidcServiceStub(String initialToken, String refreshedToken) {
        super(new Config(initialToken));
        this.token = initialToken;
        this.refreshedToken = refreshedToken;
        this.throwOnGet=false;
        this.throwOnRefresh=false;
    }

    public OidcServiceStub(String initialToken, String refreshedToken, boolean throwOnGet, boolean throwOnRefresh) {
        super(new Config(initialToken));
        this.token = initialToken;
        this.refreshedToken = refreshedToken;
        this.throwOnGet=throwOnGet;
        this.throwOnRefresh=throwOnRefresh;
    }

    @Override public String getServiceAccountToken() {
        nbCalls++;
        return token; }



    @Override
    public void retrieveServiceAccountToken() throws IOException {
        nbRefresh++;
        if (throwOnRefresh) throw new IOException("refresh failed");
        this.token = refreshedToken;
    }

    public int getCalls(){
        return nbCalls;
    }

    public int refreshCalls(){
        return nbRefresh;
    }
}
