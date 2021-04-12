package com.auth.server.authServer.security.controller;


import com.auth.server.authServer.security.dto.ResponseToken;
import com.auth.server.authServer.security.dto.UserDTO;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequestMapping(value = "/users")
@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private String authServerUrl = "http://localhost:8080/auth";
    private String realm = "tutorialsbuddy-test";
    private String clientId = "tutorialsbuddy-app";
    private String role = "student";
    //Get client secret from the Keycloak admin console (in the credential tab)
    private String clientSecret = "c073a221-686f-4c56-a75a-7599453fa76c";


    @PostMapping(path = "/create")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {

        Keycloak keycloak = KeycloakBuilder.builder().serverUrl(authServerUrl)
                .grantType(OAuth2Constants.PASSWORD).realm("master").clientId("admin-cli")
                .username("iska").password("iska")
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();

        keycloak.tokenManager().getAccessToken();


        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstname());
        user.setLastName(userDTO.getLastname());
        user.setEmail(userDTO.getUsername());

        // Get realm
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersRessource = realmResource.users();

        Response response = usersRessource.create(user);

        userDTO.setStatusCode(response.getStatus());
        userDTO.setStatus(response.getStatusInfo().toString());

        if (response.getStatus() == 201) {

            String userId = CreatedResponseUtil.getCreatedId(response);

            log.info("Created userId {}", userId);
            // create password credential
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userDTO.getPassword());

            UserResource userResource = usersRessource.get(userId);
            // Set password credential
            userResource.resetPassword(passwordCred);

            // Get realm role student
            RoleRepresentation realmRoleUser = realmResource.roles().get(role).toRepresentation();

            // Assign realm role student to user
            userResource.roles().realmLevel().add(Arrays.asList(realmRoleUser));

        }
        return ResponseEntity.ok(userDTO);

    }

//
//    @PostMapping(path = "/resetPassword")
//    public ResponseEntity<?> resetPassword(String newPassword)
//    {
//        UsersResource usersRessource =
//        userResource.resetPassword(newPassword);
//
//    }

    @PostMapping(path = "/signin")
    public ResponseEntity<?> signin(@RequestBody UserDTO userDTO) {

         Map<String, Object> clientCredentials = new HashMap<>();
         clientCredentials.put("secret", clientSecret);
         clientCredentials.put("grant_type", "password");

         Configuration configuration =
                 new Configuration(authServerUrl, realm, clientId, clientCredentials, null);
         AuthzClient authzClient = AuthzClient.create(configuration);

         AccessTokenResponse response =
                 authzClient.obtainAccessToken(userDTO.getUsername(), userDTO.getPassword());


         return ResponseEntity.ok(response);

    }


}

