/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Licensed to the Apache Software Foundation (ASF) under one
 ~ or more contributor license agreements.  See the NOTICE file
 ~ distributed with this work for additional information
 ~ regarding copyright ownership.  The ASF licenses this file
 ~ to you under the Apache License, Version 2.0 (the
 ~ "License"); you may not use this file except in compliance
 ~ with the License.  You may obtain a copy of the License at
 ~
 ~   http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package org.apache.sling.graphql.core.servlet;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.graphql.core.cache.SimpleGraphQLCacheProvider;
import org.apache.sling.graphql.core.engine.SlingDataFetcherSelector;
import org.apache.sling.graphql.core.scalars.SlingScalarsProvider;
import org.apache.sling.graphql.core.schema.RankedSchemaProviders;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class GraphQLServletTest {

    @Rule
    public SlingContext context = new SlingContext();

    @Test
    public void testDisabledSuffix() throws IOException {
        RankedSchemaProviders rankedSchemaProviders = mock(RankedSchemaProviders.class);
        context.registerService(rankedSchemaProviders);
        SlingDataFetcherSelector slingDataFetcherSelector = mock(SlingDataFetcherSelector.class);
        context.registerService(slingDataFetcherSelector);
        SlingScalarsProvider slingScalarsProvider = mock(SlingScalarsProvider.class);
        context.registerService(slingScalarsProvider);
        context.registerInjectActivateService(new SimpleGraphQLCacheProvider());
        context.registerInjectActivateService(new GraphQLServlet(), ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES, "a/b/c",
                "persistedQueries.suffix", "");
        GraphQLServlet servlet = (GraphQLServlet) context.getService(Servlet.class);
        assertNotNull(servlet);

        context.build().resource("/content/graphql", ResourceResolver.PROPERTY_RESOURCE_TYPE, "a/b/c").commit();
        Resource resource = context.resourceResolver().resolve("/content/graphql");

        MockSlingHttpServletResponse response = context.response();
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.bundleContext());


        request.setResource(resource);
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo) request.getRequestPathInfo();
        requestPathInfo.setExtension("gql");
        requestPathInfo.setResourcePath(resource.getPath());
        requestPathInfo.setSuffix("/persisted");
        request.setPathInfo("/content/graphql/persisted/hash");
        servlet.doGet(request, response);
        assertEquals(400, response.getStatus());
        assertEquals("Persisted queries are disabled.", response.getStatusMessage());
    }


}
