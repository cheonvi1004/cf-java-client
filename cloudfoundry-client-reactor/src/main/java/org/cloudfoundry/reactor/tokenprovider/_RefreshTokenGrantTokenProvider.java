/*
 * Copyright 2013-2016 the original author or authors.
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

package org.cloudfoundry.reactor.tokenprovider;

import org.cloudfoundry.reactor.TokenProvider;
import org.immutables.value.Value;
import reactor.ipc.netty.http.client.HttpClientRequest.Form;

/**
 * The OAuth Refresh Token Grant implementation of {@link TokenProvider}
 */
@Value.Immutable
abstract class _RefreshTokenGrantTokenProvider extends AbstractUaaTokenProvider {

    @Override
    protected void accessTokenPayload(Form form) {
        form
            .attr("grant_type", "refresh_token")
            .attr("client_id", getClientId())
            .attr("client_secret", getClientSecret())
            .attr("refresh_token", getToken());
    }

    /**
     * The refresh token
     */
    abstract String getToken();

}