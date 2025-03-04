/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.microprofile.server;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * A JAX-RS application to be registered.
 * Can configure additional fields that are not part of JAX-RS configuration (all optional):
 * <ul>
 * <li>contextRoot - what is the base URI of this application (e.g. /myApp)</li>
 * <li>executorService - executor service to use when executing requests for this application
 * a single executor service can be shared between multiple applications</li>
 * </ul>
 */
public final class JaxRsApplication {
    private final String contextRoot;
    private final ResourceConfig config;
    private final ExecutorService executorService;
    private final String appClassName;
    private final String routingName;
    private final boolean routingNameRequired;

    /**
     * Create a new instance based on an JAX-RS Application class.
     *
     * @param application class of an application (JAX-RS) to wrap
     * @return a new instance based on the application on root context path with no executor service (will use server default)
     */
    public static JaxRsApplication create(Class<? extends Application> application) {
        return builder()
                .application(application)
                .build();
    }

    /**
     * Create an instance based on a JAX-RS application.
     *
     * @param application JAX-RS application instance
     * @return a new instance based on the application on root context path with no executor service (will use server default)
     */
    public static JaxRsApplication create(Application application) {
        return builder()
                .application(application)
                .build();
    }

    /**
     * A new fluent API builder to create a customized {@link JaxRsApplication}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    private JaxRsApplication(Builder builder) {
        this.contextRoot = builder.contextRoot;
        this.config = builder.config;
        this.executorService = builder.executorService;
        this.appClassName = builder.appClassName;
        this.routingName = builder.routingName;
        this.routingNameRequired = builder.routingNameRequired;
    }

    String contextRoot() {
        return contextRoot;
    }

    ResourceConfig resourceConfig() {
        return config;
    }

    Optional<ExecutorService> executorService() {
        return Optional.ofNullable(executorService);
    }

    String appClassName() {
        return appClassName;
    }

    String routingName() {
        return routingName;
    }

    boolean routingNameRequired() {
        return routingNameRequired;
    }

    /**
     * Fluent API builder to create {@link JaxRsApplication} instances.
     */
    public static class Builder {
        private static final String DEFAULT_CONTEXT_ROOT = "/";
        private String contextRoot;
        private ResourceConfig config;
        private ExecutorService executorService;
        private String appClassName;
        private String routingName;
        private boolean routingNameRequired;

        /**
         * Configure an explicit context root for this application.
         *
         * @param contextRoot context root to expose this application on, defaults to "/"
         * @return updated builder instance
         */
        public Builder contextRoot(String contextRoot) {
            this.contextRoot = contextRoot;
            return this;
        }

        /**
         * Set resource configuration that forms this application.
         * This will replace the current application (if any) with the specified config.
         *
         * @param config Jersey resource config to use
         * @return updated builder instance
         * @see #application(Application)
         * @see #application(Class)
         */
        public Builder config(ResourceConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Set the JAX-RS application that forms this instance.
         * This will replace the current application (if any) with the specified application.
         *
         * @param app JAX-RS application instance
         * @return updated builder instance
         * @see #application(Class)
         * @see #config(ResourceConfig)
         */
        public Builder application(Application app) {
            this.config = toConfig(app);

            Class<?> clazz = app.getClass();
            contextRoot(clazz);
            routingName(clazz);
            appClassName = clazz.getName();

            return this;
        }

        /**
         * Set the JAX-RS application class that forms this instance.
         * This will replace the current application (if any) with the specified application.
         *
         * @param appClass JAX-RS application class
         * @return updated builder instance
         * @see #application(Application)
         * @see #config(ResourceConfig)
         */
        public Builder application(Class<? extends Application> appClass) {
            config = ResourceConfig.forApplicationClass(appClass);

            contextRoot(appClass);
            routingName(appClass);
            appClassName = appClass.getName();

            return this;
        }

        /**
         * Configure an executor service to be used for this application.
         * Executor services can be shared between applications. If none is defined, server default will be used.
         *
         * @param executorService executor service to use with this application
         * @return updated builder instance
         */
        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        /**
         * Create a new instance based on this builder.
         *
         * @return application ready to be registered with {@link Server.Builder#addApplication(JaxRsApplication)}
         */
        public JaxRsApplication build() {
            if (null == contextRoot) {
                contextRoot = DEFAULT_CONTEXT_ROOT;
            }

            return new JaxRsApplication(this);
        }

        private static ResourceConfig toConfig(Application application) {
            if (application instanceof ResourceConfig) {
                return (ResourceConfig) application;
            }

            return ResourceConfig.forApplication(application);
        }

        private void contextRoot(Class<?> clazz) {
            if (null != contextRoot) {
                return;
            }
            ApplicationPath path = clazz.getAnnotation(ApplicationPath.class);
            if (null != path) {
                contextRoot = path.value();
            }
        }

        private void routingName(Class<?> clazz) {
            if (null != routingName) {
                return;
            }
            RoutingName rn = clazz.getAnnotation(RoutingName.class);
            if (null != rn) {
                this.routingName = rn.value();
                this.routingNameRequired = rn.required();
            }
        }
    }
}
