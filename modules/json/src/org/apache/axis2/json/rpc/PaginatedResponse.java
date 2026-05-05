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

package org.apache.axis2.json.rpc;

import java.util.Collections;
import java.util.List;

/**
 * Generic paginated response wrapper for Axis2 JSON-RPC services.
 *
 * <p>Provides offset/limit pagination metadata alongside the result data.
 * Designed for services backed by SQL databases where the DAO layer already
 * supports {@code setFirstResult(offset)} and {@code setMaxResults(limit)}
 * (standard JPA/Hibernate pattern).
 *
 * <h3>Wire format</h3>
 * The JSON formatter serializes this as:
 * <pre>{@code
 * {
 *   "response": {
 *     "data": [ ... ],
 *     "pagination": {
 *       "offset": 0,
 *       "limit": 50,
 *       "totalCount": 1247,
 *       "hasMore": true
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h3>Why offset/limit instead of cursor</h3>
 * <ul>
 *   <li>Existing DAO layers use integer offsets ({@code query.setFirstResult()}).
 *       Cursor pagination requires a stable sort key and stateful server-side
 *       tokens — added complexity with no benefit when the underlying query is
 *       already offset-based.</li>
 *   <li>Frontend grids (SmartClient, AG Grid, React Table) natively speak
 *       offset/limit via {@code startRow}/{@code endRow} or
 *       {@code page}/{@code pageSize}. Cursor tokens require client-side
 *       adaptation.</li>
 *   <li>The {@code totalCount} field enables "Showing 1–50 of 1,247" UI patterns
 *       and page-count calculations. Cursor APIs typically omit total counts
 *       because they are expensive for the cursor model — but they are cheap
 *       when you already run a {@code SELECT COUNT(*)} in the DAO.</li>
 * </ul>
 *
 * <h3>Usage in a service method</h3>
 * <pre>{@code
 * public PaginatedResponse<AssetBO> findAssets(AssetQuery query) {
 *     List<AssetBO> items = dao.findList(query.getOffset(), query.getLimit());
 *     long total = dao.count(query);
 *     return PaginatedResponse.of(items, query.getOffset(), query.getLimit(), total);
 * }
 * }</pre>
 *
 * <h3>Virtual scrolling / infinite scroll</h3>
 * Frontends that use virtual scrolling (loading the next chunk as the user
 * scrolls) can use {@code hasMore} to decide whether to fetch the next page:
 * <pre>{@code
 * // React / TypeScript
 * const nextOffset = pagination.offset + pagination.limit;
 * if (pagination.hasMore) {
 *     fetchPage(nextOffset, pagination.limit);
 * }
 * }</pre>
 *
 * @param <T> the element type in the data list
 */
public class PaginatedResponse<T> {

    /** The items for this page.  Never null after construction via factory methods. */
    private List<T> data;

    /** Pagination metadata: offset, limit, totalCount, hasMore. */
    private Pagination pagination;

    /** Default constructor for JSON deserialization (Gson, Moshi, Jackson). */
    public PaginatedResponse() {
    }

    public PaginatedResponse(List<T> data, Pagination pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    /**
     * Build a paginated response from query results.
     *
     * @param data       the result items for this page
     * @param offset     zero-based offset of the first item in this page
     * @param limit      maximum items requested (page size)
     * @param totalCount total number of items matching the query (across all pages)
     * @return a fully populated response ready for serialization
     */
    public static <T> PaginatedResponse<T> of(List<T> data, int offset, int limit,
                                                long totalCount) {
        return new PaginatedResponse<>(
                data != null ? data : Collections.emptyList(),
                new Pagination(offset, limit, totalCount));
    }

    /**
     * Build an unpaginated response (all results in one page, no more pages).
     * Useful when the full result set is small enough to return at once, or
     * when migrating an existing service that returned a bare list — wrap it
     * in PaginatedResponse.unpaginated() to add pagination metadata without
     * changing the client contract (hasMore=false, totalCount=data.size()).
     */
    public static <T> PaginatedResponse<T> unpaginated(List<T> data) {
        List<T> items = data != null ? data : Collections.emptyList();
        return new PaginatedResponse<>(items,
                new Pagination(0, items.size(), items.size()));
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    // ── Pagination metadata ──────────────────────────────────────────────────

    /**
     * Pagination metadata returned alongside the data.
     *
     * <p>All fields are echoed from the request (offset, limit) or computed
     * from the query (totalCount, hasMore). This gives the client everything
     * needed to render paging controls or implement infinite scroll without
     * any client-side bookkeeping.
     */
    public static class Pagination {
        /** Zero-based offset of the first item in this page. */
        private int offset;

        /** Maximum items requested (page size). */
        private int limit;

        /** Total items matching the query across all pages. */
        private long totalCount;

        /**
         * True if there are more items beyond this page.
         * Computed as {@code offset + data.size() < totalCount}.
         * Frontends use this to enable/disable "Load More" or "Next Page".
         */
        private boolean hasMore;

        public Pagination() {
        }

        public Pagination(int offset, int limit, long totalCount) {
            this.offset = offset;
            this.limit = limit;
            this.totalCount = totalCount;
            // hasMore is true when there are items beyond the current page.
            // We use offset + limit (not offset + actual data size) because
            // the caller may have received fewer items than limit on the last
            // page, and we want hasMore=false in that case too. But the
            // definitive check is offset + limit < totalCount — if the offset
            // plus a full page is still less than total, there's more.
            this.hasMore = (long) offset + limit < totalCount;
        }

        public int getOffset() { return offset; }
        public void setOffset(int offset) { this.offset = offset; }

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }

        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

        public boolean isHasMore() { return hasMore; }
        public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
    }
}
