package com.playground.controller;

import com.playground.domain.BankAccount;
import com.playground.domain.Client;
import com.playground.service.BankService;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;

@Controller
@Slf4j
public class AccountsController {
    @Autowired
    BankService bankService;

    @QueryMapping
    List<BankAccount> accounts(Authentication authentication) {
        if (authentication == null) {
            return Collections.emptyList();
        }

        String username = getPrincipleFromAuth(authentication);

        log.info("Getting Accounts for user: {}", username);
        return bankService.getAccounts();
    }

    @SchemaMapping(typeName = "BankAccount", field = "client")
    Client getClient(BankAccount account, Authentication authentication) {
        if (authentication == null) {
            log.warn("Authentication is null");
            return null;
        }

        String username = getPrincipleFromAuth(authentication);

        log.info("Getting Client for Account {} for User: {}", account.id(), username);
        return bankService.getClientByAccountId(account.id());
    }

    private static String getPrincipleFromAuth(Authentication authentication) {
        String username = "Unknown";
        if (authentication instanceof OAuth2AuthenticationToken) {
            username = ((OAuth2AuthenticationToken) authentication).getPrincipal().getName();
        } else if (authentication instanceof JwtAuthenticationToken) {
            username = ((JwtAuthenticationToken) authentication).getName();
        }
        return username;
    }

    @GraphQlExceptionHandler
    public GraphQLError handle(@NonNull Exception ex, @NonNull DataFetchingEnvironment environment) {
        return GraphQLError
                .newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .build();
    }
}
