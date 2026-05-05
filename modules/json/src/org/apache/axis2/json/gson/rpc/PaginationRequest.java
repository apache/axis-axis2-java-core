/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.json.gson.rpc;

/**
 * Pagination parameters that a client sends with a list query.
 *
 * <p>Services can embed these fields directly in their request POJO or
 * accept a {@code PaginationRequest} as a separate parameter. The values
 * map directly to Hibernate/JPA's {@code setFirstResult(offset)} and
 * {@code setMaxResults(limit)}.
 *
 * <h3>Defaults</h3>
 * <ul>
 *   <li>{@code offset = 0} — start from the beginning</li>
 *   <li>{@code limit = 50} — return at most 50 items</li>
 * </ul>
 *
 * <h3>Safety</h3>
 * The {@link #getLimit()} getter clamps the value to {@link #MAX_LIMIT}
 * (default 2000) to prevent clients from requesting unbounded result sets
 * that could exhaust server memory. Services can override this by calling
 * {@link #setMaxLimit(int)} before reading the limit.
 *
 * <h3>Wire format</h3>
 * When embedded in a request POJO:
 * <pre>{@code
 * {
 *   "searchTerm": "AAPL",
 *   "offset": 100,
 *   "limit": 50
 * }
 * }</pre>
 */
public class PaginationRequest {

    /** Default page size when not specified by the client. */
    public static final int DEFAULT_LIMIT = 50;

    /**
     * Maximum page size to prevent unbounded queries.  Set to 2000 to match
     * common enterprise grid configurations where large virtual-scroll pages
     * are used to minimize round trips while staying within reasonable memory.
     */
    public static final int DEFAULT_MAX_LIMIT = 2000;

    /** Zero-based offset — maps to JPA/Hibernate {@code query.setFirstResult(offset)}. */
    private int offset;

    /** Page size — maps to JPA/Hibernate {@code query.setMaxResults(limit)}. */
    private int limit;

    /**
     * Server-enforced ceiling on limit.  Prevents clients from requesting
     * unbounded result sets (e.g. limit=999999) that could exhaust heap.
     * Services can lower this per-operation via {@link #setMaxLimit(int)}.
     */
    private int maxLimit = DEFAULT_MAX_LIMIT;

    public PaginationRequest() {
        this.offset = 0;
        this.limit = DEFAULT_LIMIT;
    }

    public PaginationRequest(int offset, int limit) {
        this.offset = Math.max(0, offset);
        this.limit = limit;
    }

    /** Zero-based offset. Negative values are clamped to 0. */
    public int getOffset() {
        return Math.max(0, offset);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Page size. Clamped to [1, maxLimit].
     * If the client sends 0 or negative, the default (50) is used.
     * If the client sends more than maxLimit, it is capped.
     */
    public int getLimit() {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, maxLimit);
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Override the maximum allowed limit for this request.
     * Call this before {@link #getLimit()} to change the cap.
     */
    public void setMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
    }

    public int getMaxLimit() {
        return maxLimit;
    }
}
