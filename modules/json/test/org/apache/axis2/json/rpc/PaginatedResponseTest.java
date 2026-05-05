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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link PaginatedResponse} and {@link PaginationRequest}.
 */
public class PaginatedResponseTest {

    // ═══════════════════════════════════════════════════════════════════════
    // PaginatedResponse
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testOf_firstPage_hasMore() {
        List<String> items = Arrays.asList("a", "b", "c");
        PaginatedResponse<String> resp = PaginatedResponse.of(items, 0, 3, 10);

        assertEquals(3, resp.getData().size());
        assertEquals(0, resp.getPagination().getOffset());
        assertEquals(3, resp.getPagination().getLimit());
        assertEquals(10, resp.getPagination().getTotalCount());
        assertTrue("offset 0 + limit 3 < total 10 → hasMore", resp.getPagination().isHasMore());
    }

    @Test
    public void testOf_lastPage_noMore() {
        List<String> items = Arrays.asList("h", "i", "j");
        PaginatedResponse<String> resp = PaginatedResponse.of(items, 7, 3, 10);

        assertEquals(3, resp.getData().size());
        assertEquals(7, resp.getPagination().getOffset());
        assertFalse("offset 7 + limit 3 = 10 = total → no more",
                resp.getPagination().isHasMore());
    }

    @Test
    public void testOf_partialLastPage_noMore() {
        // Only 2 items on last page (limit was 3 but only 2 remain)
        List<String> items = Arrays.asList("i", "j");
        PaginatedResponse<String> resp = PaginatedResponse.of(items, 8, 3, 10);

        assertEquals(2, resp.getData().size());
        // offset 8 + limit 3 = 11 > total 10 → no more
        assertFalse(resp.getPagination().isHasMore());
    }

    @Test
    public void testOf_exactlyOnePage() {
        List<String> items = Arrays.asList("a", "b", "c");
        PaginatedResponse<String> resp = PaginatedResponse.of(items, 0, 3, 3);

        assertEquals(3, resp.getPagination().getTotalCount());
        assertFalse("single page → no more", resp.getPagination().isHasMore());
    }

    @Test
    public void testOf_emptyResult() {
        PaginatedResponse<String> resp = PaginatedResponse.of(
                Collections.emptyList(), 0, 50, 0);

        assertTrue(resp.getData().isEmpty());
        assertEquals(0, resp.getPagination().getTotalCount());
        assertFalse(resp.getPagination().isHasMore());
    }

    @Test
    public void testOf_nullDataTreatedAsEmpty() {
        PaginatedResponse<String> resp = PaginatedResponse.of(null, 0, 50, 0);

        assertNotNull(resp.getData());
        assertTrue(resp.getData().isEmpty());
    }

    @Test
    public void testUnpaginated() {
        List<String> items = Arrays.asList("x", "y", "z");
        PaginatedResponse<String> resp = PaginatedResponse.unpaginated(items);

        assertEquals(3, resp.getData().size());
        assertEquals(0, resp.getPagination().getOffset());
        assertEquals(3, resp.getPagination().getLimit());
        assertEquals(3, resp.getPagination().getTotalCount());
        assertFalse("unpaginated → no more", resp.getPagination().isHasMore());
    }

    @Test
    public void testUnpaginated_null() {
        PaginatedResponse<String> resp = PaginatedResponse.unpaginated(null);
        assertNotNull(resp.getData());
        assertTrue(resp.getData().isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PaginationRequest
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testRequest_defaults() {
        PaginationRequest req = new PaginationRequest();
        assertEquals(0, req.getOffset());
        assertEquals(50, req.getLimit());
    }

    @Test
    public void testRequest_negativeOffsetClampedToZero() {
        PaginationRequest req = new PaginationRequest();
        req.setOffset(-5);
        assertEquals(0, req.getOffset());
    }

    @Test
    public void testRequest_zeroLimitUsesDefault() {
        PaginationRequest req = new PaginationRequest();
        req.setLimit(0);
        assertEquals(PaginationRequest.DEFAULT_LIMIT, req.getLimit());
    }

    @Test
    public void testRequest_negativeLimitUsesDefault() {
        PaginationRequest req = new PaginationRequest();
        req.setLimit(-10);
        assertEquals(PaginationRequest.DEFAULT_LIMIT, req.getLimit());
    }

    @Test
    public void testRequest_exceedsMaxLimitIsCapped() {
        PaginationRequest req = new PaginationRequest();
        req.setLimit(5000);
        assertEquals(PaginationRequest.DEFAULT_MAX_LIMIT, req.getLimit());
    }

    @Test
    public void testRequest_customMaxLimit() {
        PaginationRequest req = new PaginationRequest();
        req.setMaxLimit(100);
        req.setLimit(200);
        assertEquals(100, req.getLimit());
    }

    @Test
    public void testRequest_validLimitPassesThrough() {
        PaginationRequest req = new PaginationRequest();
        req.setLimit(25);
        assertEquals(25, req.getLimit());
    }

    @Test
    public void testRequest_constructorWithValues() {
        PaginationRequest req = new PaginationRequest(100, 25);
        assertEquals(100, req.getOffset());
        assertEquals(25, req.getLimit());
    }

    @Test
    public void testRequest_constructorNegativeOffset() {
        PaginationRequest req = new PaginationRequest(-10, 25);
        assertEquals(0, req.getOffset());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Integration: request → response round-trip
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testRoundTrip_simulateServiceCall() {
        // Simulate: client requests page 3 (offset=100, limit=50) of 247 total items
        PaginationRequest req = new PaginationRequest(100, 50);

        // Service returns 50 items from this page
        List<String> pageData = Collections.nCopies(50, "item");
        PaginatedResponse<String> resp = PaginatedResponse.of(
                pageData, req.getOffset(), req.getLimit(), 247);

        assertEquals(50, resp.getData().size());
        assertEquals(100, resp.getPagination().getOffset());
        assertEquals(50, resp.getPagination().getLimit());
        assertEquals(247, resp.getPagination().getTotalCount());
        assertTrue("100 + 50 = 150 < 247 → hasMore", resp.getPagination().isHasMore());

        // Client calculates: next page starts at offset 150
        int nextOffset = resp.getPagination().getOffset() + resp.getPagination().getLimit();
        assertEquals(150, nextOffset);
    }

    @Test
    public void testRoundTrip_lastPage() {
        // Client requests the last partial page
        PaginationRequest req = new PaginationRequest(200, 50);

        // Service returns only 47 items (247 total - 200 offset = 47 remaining)
        List<String> pageData = Collections.nCopies(47, "item");
        PaginatedResponse<String> resp = PaginatedResponse.of(
                pageData, req.getOffset(), req.getLimit(), 247);

        assertEquals(47, resp.getData().size());
        assertFalse("200 + 50 = 250 > 247 → no more", resp.getPagination().isHasMore());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Enterprise DAO scenarios (sanitized production patterns)
    //
    // These tests mirror the common patterns found in Hibernate-backed
    // enterprise apps: large entity lists, soft-delete filtering, virtual
    // scrolling with large page sizes, and count-alongside-data queries.
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testEnterprise_largeEntityList_firstPage() {
        // Scenario: user opens a grid showing all portfolio holdings.
        // DAO runs: query.setFirstResult(0), query.setMaxResults(2000)
        // Count query returns 8,543 active (non-deleted) holdings.
        PaginationRequest req = new PaginationRequest(0, 2000);

        List<String> holdings = Collections.nCopies(2000, "holding");
        PaginatedResponse<String> resp = PaginatedResponse.of(
                holdings, req.getOffset(), req.getLimit(), 8543);

        assertEquals(2000, resp.getData().size());
        assertEquals(0, resp.getPagination().getOffset());
        assertEquals(2000, resp.getPagination().getLimit());
        assertEquals(8543, resp.getPagination().getTotalCount());
        assertTrue("8543 holdings, showing first 2000 → hasMore", resp.getPagination().isHasMore());

        // Frontend grid calculates total pages for page controls
        long totalPages = (long) Math.ceil(
                (double) resp.getPagination().getTotalCount() / resp.getPagination().getLimit());
        assertEquals(5, totalPages); // ceil(8543/2000) = 5 pages
    }

    @Test
    public void testEnterprise_largeEntityList_lastPartialPage() {
        // Scenario: grid scrolls to the last page of 8,543 holdings.
        // Pages 1-4 had 2000 each (offsets 0, 2000, 4000, 6000).
        // Page 5: offset=8000, limit=2000, but only 543 remain.
        PaginationRequest req = new PaginationRequest(8000, 2000);

        List<String> lastPageHoldings = Collections.nCopies(543, "holding");
        PaginatedResponse<String> resp = PaginatedResponse.of(
                lastPageHoldings, req.getOffset(), req.getLimit(), 8543);

        assertEquals(543, resp.getData().size());
        assertFalse("last page → no more", resp.getPagination().isHasMore());
    }

    @Test
    public void testEnterprise_virtualScroll_startRowEndRow() {
        // Scenario: SmartClient-style grid sends startRow=300, endRow=350.
        // The service translates: offset = startRow, limit = endRow - startRow.
        int startRow = 300;
        int endRow = 350;
        int offset = startRow;
        int limit = endRow - startRow;

        PaginationRequest req = new PaginationRequest(offset, limit);
        assertEquals(300, req.getOffset());
        assertEquals(50, req.getLimit());

        // DAO returns 50 items from a total of 1,247
        List<String> chunk = Collections.nCopies(50, "row");
        PaginatedResponse<String> resp = PaginatedResponse.of(
                chunk, req.getOffset(), req.getLimit(), 1247);

        assertTrue("more rows available for virtual scroll", resp.getPagination().isHasMore());

        // Grid calculates next fetch: startRow=350, endRow=400
        int nextStartRow = resp.getPagination().getOffset() + resp.getPagination().getLimit();
        assertEquals(350, nextStartRow);
    }

    @Test
    public void testEnterprise_softDeleteFiltering() {
        // Scenario: database has 10,000 records but 1,457 are soft-deleted.
        // The DAO applies a "deleted = 0" filter before pagination.
        // Count query: SELECT COUNT(*) WHERE deleted = 0 → 8,543
        // Data query: SELECT ... WHERE deleted = 0 OFFSET 0 LIMIT 50
        //
        // totalCount reflects the FILTERED set (8,543), not the raw table (10,000).
        // This is the correct behavior — clients see only active records.
        PaginationRequest req = new PaginationRequest(0, 50);

        List<String> activeRecords = Collections.nCopies(50, "active");
        long filteredTotal = 8543; // excludes soft-deleted
        PaginatedResponse<String> resp = PaginatedResponse.of(
                activeRecords, req.getOffset(), req.getLimit(), filteredTotal);

        assertEquals(8543, resp.getPagination().getTotalCount());
        assertTrue(resp.getPagination().isHasMore());
    }

    @Test
    public void testEnterprise_maxLimitEnforced() {
        // Scenario: a client sends limit=999999 trying to dump the entire table.
        // PaginationRequest caps it at DEFAULT_MAX_LIMIT (2000).
        PaginationRequest req = new PaginationRequest();
        req.setLimit(999999);
        assertEquals("server caps at 2000", 2000, req.getLimit());
    }

    @Test
    public void testEnterprise_serviceCanLowerMaxLimit() {
        // Scenario: a specific operation handles expensive entities (e.g. with
        // large text fields). The service lowers the max to 100 to bound memory.
        PaginationRequest req = new PaginationRequest();
        req.setMaxLimit(100);
        req.setLimit(500);
        assertEquals("service-specific cap at 100", 100, req.getLimit());
    }

    @Test
    public void testEnterprise_unpaginated_smallLookupTable() {
        // Scenario: a lookup/reference table (e.g. list of 15 departments).
        // No pagination needed — return the full set with hasMore=false.
        List<String> departments = Arrays.asList(
                "Equity Long/Short", "Fixed Income", "Macro", "Quant",
                "Event Driven", "Multi-Strategy", "Credit", "Real Assets",
                "Private Equity", "Venture", "Commodities", "FX",
                "Emerging Markets", "Infrastructure", "Insurance-Linked");

        PaginatedResponse<String> resp = PaginatedResponse.unpaginated(departments);

        assertEquals(15, resp.getData().size());
        assertEquals(15, resp.getPagination().getTotalCount());
        assertEquals(0, resp.getPagination().getOffset());
        assertFalse("small table, all returned → no more", resp.getPagination().isHasMore());
    }

    @Test
    public void testEnterprise_emptyFilterResult() {
        // Scenario: user searches for "XYZNOTFOUND" — no matching records.
        // The service returns an empty page with totalCount=0.
        PaginationRequest req = new PaginationRequest(0, 50);

        PaginatedResponse<String> resp = PaginatedResponse.of(
                Collections.emptyList(), req.getOffset(), req.getLimit(), 0);

        assertTrue(resp.getData().isEmpty());
        assertEquals(0, resp.getPagination().getTotalCount());
        assertFalse(resp.getPagination().isHasMore());
    }

    @Test
    public void testEnterprise_pageCalculation_forReactUI() {
        // Scenario: React frontend needs to render page controls.
        // API returns pagination metadata; frontend calculates display values.
        PaginatedResponse<String> resp = PaginatedResponse.of(
                Collections.nCopies(50, "item"), 150, 50, 1247);

        // Frontend calculations:
        int offset = resp.getPagination().getOffset();
        int limit = resp.getPagination().getLimit();
        long total = resp.getPagination().getTotalCount();

        int currentPage = (offset / limit) + 1;       // 1-based page number
        int totalPages = (int) Math.ceil((double) total / limit);
        int showingFrom = offset + 1;                  // 1-based display
        int showingTo = offset + resp.getData().size(); // actual items on this page

        assertEquals("page 4 of 25", 4, currentPage);
        assertEquals(25, totalPages);
        assertEquals("Showing 151", 151, showingFrom);
        assertEquals("to 200", 200, showingTo);
    }
}
