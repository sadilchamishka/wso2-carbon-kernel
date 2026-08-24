/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.jdbc;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

/**
 * Guards the claim update batch order, which is what keeps concurrent updates of one user from
 * deadlocking now that the pre-update lock is gone. A regression here is silent: nothing throws and
 * nothing logs, requests just start failing under concurrency.
 */
public class ClaimUpdatePredicateSQLTest {

    @Test
    public void testTheBatchIsOrderedIndependentlyOfTheCallersMap() {

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("http://wso2.org/claims/mobile", "1");
        properties.put("http://wso2.org/claims/emailaddress", "2");
        properties.put("http://wso2.org/claims/country", "3");

        Map<String, String> reversed = new LinkedHashMap<>();
        properties.entrySet().stream().sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .forEach(e -> reversed.put(e.getKey(), e.getValue()));

        assertEquals(claimOrder(properties), claimOrder(reversed),
                "Two callers passing the same claims in different orders must produce the same batch order, "
                        + "otherwise they can take the same row locks in opposite orders and deadlock.");
        assertEquals(claimOrder(properties), Arrays.asList("http://wso2.org/claims/country",
                "http://wso2.org/claims/emailaddress", "http://wso2.org/claims/mobile"));
    }

    private List<String> claimOrder(Map<String, String> properties) {

        return UniqueIDJDBCUserStoreManager.orderClaimsForBatch(properties).stream()
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
