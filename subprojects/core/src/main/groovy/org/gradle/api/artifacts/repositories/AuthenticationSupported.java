/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.artifacts.repositories;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.credentials.Credentials;

/**
 * An artifact repository which supports username/password authentication.
 */
public interface AuthenticationSupported {

    /**
     * Returns the username and password credentials used to authenticate to this repository.
     * If no credentials have been assigned to this repository, an empty set of username and password credentials is assigned to this repository and returned.
     *
     * @return The credentials.
     *
     * @throws ClassCastException when the credentials assigned to this repository are not of type {@link PasswordCredentials}.
     */
    PasswordCredentials getCredentials();

    /**
     * Returns the credentials of the specified type used to authenticate with this repository.
     * If no credentials have been assigned to this repository, an empty set of credentials of the specified type is assigned to this repository and returned.
     *
     * @param clazz type of the credential
     * @return The credentials
     *
     * @throws ClassCastException when the credentials assigned to this repository are not assignable to the specified type.
     */
    @Incubating
    public <T extends Credentials> T getCredentials(Class<T> clazz);

    /**
     * Configures the username and password credentials for this repository using the supplied action.
     * If no credentials have been assigned to this repository, an empty set of username and password credentials is assigned to this repository and passed
     * to the action.
     *
     * <pre autoTested=''>
     *     repositories {
     *         maven {
     *             url "${url}"
     *             credentials {
     *                 username = 'joe'
     *                 password = 'secret'
     *             }
     *         }
     *     }
     * </pre>
     *
     * @throws ClassCastException when the credentials assigned to this repository are not of type {@link PasswordCredentials}.
     */
    void credentials(Action<? super PasswordCredentials> action);

    /**
     * Configures the credentials for this repository using the supplied action.
     * <p>
     * If no credentials have been assigned to this repository, an empty set of credentials of the specified type will be assigned to this repository and given to the configuration action.
     * If credentials have already been specified for this repository, they will be passed to the given configuration action.
     * <pre autoTested=''>
     * repositories {
     *   maven {
     *     url "${url}"
     *     credentials(AwsCredentials) {
     *       accessKey "myAccessKey"
     *       secretKey "mySecret"
     *     }
     *   }
     * }
     * </pre>
     * <p>
     * The following credential types are currently supported for the {@code credentialsType} argument:
     * <ul>
     * <li>{@link org.gradle.api.artifacts.repositories.PasswordCredentials}</li>
     * <li>{@link org.gradle.api.credentials.AwsCredentials}</li>
     * </ul>
     *
     * @throws IllegalArgumentException if {@code credentialsType} is not of a supported type
     * @throws ClassCastException if {@code credentialsType} is of a different type to the credentials previously specified for this repository
     */
    @Incubating
    <T extends Credentials> void credentials(Class<T> credentialsType, Action<? super T> action);
}
