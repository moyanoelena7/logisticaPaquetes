package com.logistica.ms_logistica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class PaquetesClientConfig {

    @Bean
    OAuth2AuthorizedClientManager msPaquetesAuthorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        OAuth2AuthorizedClientProvider provider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        registrations,
                        authorizedClientService
                );

        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    @Bean
    RestClient paquetesRestClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager manager,
            @Value("${app.ms-paquetes.base-url}") String baseUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    OAuth2AuthorizedClient authorizedClient = manager.authorize(
                            OAuth2AuthorizeRequest
                                    .withClientRegistrationId("ms-paquetes")
                                    .principal("ms-logistica")
                                    .build()
                    );

                    if (authorizedClient == null) {
                        throw new IllegalStateException(
                                "No se pudo obtener el token de servicio para ms-paquetes"
                        );
                    }

                    request.getHeaders().setBearerAuth(
                            authorizedClient.getAccessToken().getTokenValue()
                    );

                    return execution.execute(request, body);
                })
                .build();
    }
}
