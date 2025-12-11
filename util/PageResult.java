package util;

import java.util.List;

/**
 * Immutable pagination container returned by repository/service calls.
 */
public final class PageResult<T> {
    private final List<T> items;
    private final int pageIndex;
    private final int pageSize;
    private final long totalItems;

    public PageResult(List<T> items, int pageIndex, int pageSize, long totalItems) {
        if (pageIndex < 0) {
            throw new IllegalArgumentException("pageIndex must be non-negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be positive");
        }
        this.items = List.copyOf(items);
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalItems = Math.max(totalItems, 0);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        if (totalItems == 0) {
            return 1;
        }
        return (int) Math.ceil(totalItems / (double) pageSize);
    }

    public boolean hasNext() {
        return pageIndex + 1 < getTotalPages();
    }

    public boolean hasPrevious() {
        return pageIndex > 0;
    }
}
