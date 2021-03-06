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
package org.apache.sling.graphql.core.cache;

import org.apache.sling.graphql.api.cache.GraphQLCacheProvider;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SimpleGraphQLCacheProviderTest {

    @Rule
    public OsgiContext context = new OsgiContext();

    @Test
    public void getHash() {
        context.registerInjectActivateService(new SimpleGraphQLCacheProvider());
        SimpleGraphQLCacheProvider provider = (SimpleGraphQLCacheProvider) context.getService(GraphQLCacheProvider.class);
        assertNotNull(provider);
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", provider.getHash("hello world"));
    }

    @Test
    public void testMemoryLimits() {
        context.registerInjectActivateService(new SimpleGraphQLCacheProvider(), "capacity", 0, "maxSize", 40);
        SimpleGraphQLCacheProvider provider = (SimpleGraphQLCacheProvider) context.getService(GraphQLCacheProvider.class);
        assertNotNull(provider);

        String aHash = provider.cacheQuery("a", "a/b/c", null);
        assertEquals("a", provider.getQuery(aHash, "a/b/c", null));

        String bHash = provider.cacheQuery("b", "a/b/c", null);
        assertEquals("b", provider.getQuery(bHash, "a/b/c", null));

        // a should be evicted due to the size constraints
        assertNull(provider.getQuery(aHash, "a/b/c", null));

        // but b should still be there
        assertEquals("b", provider.getQuery(bHash, "a/b/c", null));
    }

    @Test
    public void testCapacityLimits() {
        context.registerInjectActivateService(new SimpleGraphQLCacheProvider(), "capacity", 3, "maxSize", 0);
        SimpleGraphQLCacheProvider provider = (SimpleGraphQLCacheProvider) context.getService(GraphQLCacheProvider.class);
        assertNotNull(provider);

        String aHash = provider.cacheQuery("a", "a/b/c", null);
        assertEquals("a", provider.getQuery(aHash, "a/b/c", null));

        String bHash = provider.cacheQuery("b", "a/b/c", null);
        assertEquals("b", provider.getQuery(bHash, "a/b/c", null));

        String cHash = provider.cacheQuery("c", "a/b/c", null);
        assertEquals("c", provider.getQuery(cHash, "a/b/c", null));

        String dHash = provider.cacheQuery("d", "a/b/c", null);
        assertEquals("d", provider.getQuery(dHash, "a/b/c", null));

        // a should be evicted due to the size constraints
        assertNull(provider.getQuery(aHash, "a/b/c", null));

        // but b, c, d should still be there
        assertEquals("b", provider.getQuery(bHash, "a/b/c", null));
        assertEquals("c", provider.getQuery(cHash, "a/b/c", null));
        assertEquals("d", provider.getQuery(dHash, "a/b/c", null));
    }

    @Test
    public void testCapacityHasPriorityOverMemory() {
        context.registerInjectActivateService(new SimpleGraphQLCacheProvider(), "capacity", 2, "maxSize", 40);
        SimpleGraphQLCacheProvider provider = (SimpleGraphQLCacheProvider) context.getService(GraphQLCacheProvider.class);
        assertNotNull(provider);

        String aHash = provider.cacheQuery("a", "a/b/c", null);
        assertEquals("a", provider.getQuery(aHash, "a/b/c", null));

        String bHash = provider.cacheQuery("b", "a/b/c", null);
        assertEquals("b", provider.getQuery(bHash, "a/b/c", null));

        String cHash = provider.cacheQuery("c", "a/b/c", null);
        assertEquals("c", provider.getQuery(cHash, "a/b/c", null));

        // a should be evicted due to the size constraints
        assertNull(provider.getQuery(aHash, "a/b/c", null));

        // but b, c should still be there
        assertEquals("b", provider.getQuery(bHash, "a/b/c", null));
        assertEquals("c", provider.getQuery(cHash, "a/b/c", null));
    }

}
