package fr.insee.genesis.configuration.auth.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class RoleConfiguration {

    //Mapping des claims du jeton sur les roles applicatifs
    @Value("#{'${app.role.admin.claims}'.split(',')}")
    private List<String> adminClaims;
    @Value("#{'${app.role.user-kraftwerk.claims}'.split(',')}")
    private List<String> userKraftwerkClaims;
    @Value("#{'${app.role.user-platine.claims}'.split(',')}")
    private List<String> userPlatineClaims;
    @Value("#{'${app.role.reader.claims}'.split(',')}")
    private List<String> readerClaims;
    @Value("#{'${app.role.collect-platform.claims}'.split(',')}")
    private List<String> collectPlatformClaims;

    public Map<String, List<String>> getRolesByClaim() {
        return rolesByClaim;
    }

    private Map<String, List<String>> rolesByClaim;

    //Defines a role hierarchy
    //ADMIN implies USER   role too
    //USER  implies READER role too
    //so an admin has 2 roles: ADMIN/USER
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(ApplicationRole.ADMIN.toString()).implies(ApplicationRole.USER_KRAFTWERK.toString())
                .role(ApplicationRole.ADMIN.toString()).implies(ApplicationRole.USER_PLATINE.toString())
                .role(ApplicationRole.ADMIN.toString()).implies(ApplicationRole.COLLECT_PLATFORM.toString())
                .role(ApplicationRole.USER_KRAFTWERK.toString()).implies(ApplicationRole.READER.toString())
                .role(ApplicationRole.USER_PLATINE.toString()).implies(ApplicationRole.READER.toString())
                .build();
    }

    // and, if using pre-post method security also add
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

    @PostConstruct
    public void initialization() {

        rolesByClaim = new HashMap<>();

        // Ajout des claims pour le rôle ADMIN
        adminClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.ADMIN)));

        // Ajout des claims pour le rôle USER_KRAFTWERK
        userKraftwerkClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.USER_KRAFTWERK)));

        // Ajout des claims pour le rôle USER_PLATINE
        userPlatineClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.USER_PLATINE)));

        // Ajout des claims pour le rôle COLLECT_PLATFORM
        collectPlatformClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.COLLECT_PLATFORM)));

        // Ajout des claims pour le rôle READER
        readerClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.READER)));


        log.info("Roles configuration : {}", rolesByClaim);
    }

}
