package io.smallrye.graphql.client.typesafe.vertx;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;

import io.smallrye.graphql.client.ErrorMessageProvider;
import io.smallrye.graphql.client.GraphQLClientConfiguration;
import io.smallrye.graphql.client.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxTypesafeGraphQLClientBuilder implements TypesafeGraphQLClientBuilder {
    private String configKey = null;
    private URI endpoint;
    private Vertx vertx;
    private WebClientOptions options;
    private WebClient webClient;

    @Override
    public TypesafeGraphQLClientBuilder configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public TypesafeGraphQLClientBuilder vertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    public TypesafeGraphQLClientBuilder client(WebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    public TypesafeGraphQLClientBuilder options(WebClientOptions options) {
        this.options = options;
        return this;
    }

    private Vertx vertx() {
        if (vertx == null) {
            Context vertxContext = Vertx.currentContext();
            if (vertxContext != null && vertxContext.owner() != null) {
                vertx = vertxContext.owner();
            } else {
                // create a new vertx instance if there is none
                vertx = Vertx.vertx();
            }
        }
        return vertx;
    }

    @Override
    public TypesafeGraphQLClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    @Override
    public <T> T build(Class<T> apiClass) {
        if (configKey == null) {
            configKey = configKey(apiClass);
        }

        GraphQLClientsConfiguration configs = GraphQLClientsConfiguration.getInstance();
        GraphQLClientConfiguration persistentConfig = configs
                .getClient(configKey);
        if (persistentConfig == null) {
            // in case that we're running in a plain Java SE application, it is possible that the client configuration
            // hasn't been added yet, because there is no CDI extension or Jandex processor that scans for @GraphQLClientApi annotations
            // at startup => try adding a configuration entry dynamically for this client interface in particular.
            // Then try again.
            configs.addTypesafeClientApis(Collections.singletonList(apiClass));
            persistentConfig = configs
                    .getClient(configKey);
        }
        if (persistentConfig != null) {
            applyConfig(persistentConfig);
        }

        if (endpoint == null) {
            throw ErrorMessageProvider.get().urlMissingErrorForNamedClient(configKey);
        }

        VertxTypesafeGraphQLClientProxy graphQlClient = new VertxTypesafeGraphQLClientProxy(vertx(), persistentConfig, options,
                endpoint, webClient);
        return apiClass.cast(Proxy.newProxyInstance(getClassLoader(apiClass), new Class<?>[] { apiClass },
                (proxy, method, args) -> invoke(apiClass, graphQlClient, method, args)));
    }

    private Object invoke(Class<?> apiClass, VertxTypesafeGraphQLClientProxy graphQlClient, java.lang.reflect.Method method,
            Object... args) {
        MethodInvocation methodInvocation = MethodInvocation.of(method, args);
        if (methodInvocation.isDeclaredInCloseable()) {
            graphQlClient.close();
            return null; // void
        }
        return graphQlClient.invoke(apiClass, methodInvocation);
    }

    private ClassLoader getClassLoader(Class<?> apiClass) {
        if (System.getSecurityManager() == null)
            return apiClass.getClassLoader();
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) apiClass::getClassLoader);
    }

    /**
     * Applies values from known global configuration. This does NOT override values passed to this
     * builder by method calls.
     */
    private void applyConfig(GraphQLClientConfiguration configuration) {
        if (this.endpoint == null && configuration.getUrl() != null) {
            this.endpoint = URI.create(configuration.getUrl());
        }
    }

    private String configKey(Class<?> apiClass) {
        GraphQLClientApi annotation = apiClass.getAnnotation(GraphQLClientApi.class);
        if (annotation == null) {
            return apiClass.getName();
        }
        String keyFromAnnotation = annotation.configKey();
        return (keyFromAnnotation.isEmpty()) ? apiClass.getName() : keyFromAnnotation;
    }
}
