package com.quadtree;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NonHierarchicalDistanceBasedAlgorithm<T extends ClusterItem> {
    private static final int DEFAULT_MAX_DISTANCE_AT_ZOOM = 100; // essentially 100 dp.

    private int mMaxDistance = DEFAULT_MAX_DISTANCE_AT_ZOOM;

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final Collection<QuadItem<T>> mQuadList = new LinkedHashSet<>();

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final PointQuadTree<QuadItem<T>> mQuadTree = new PointQuadTree<QuadItem<T>>(
            new Bounds(.0, 1.0, .0, 1.0), 0);

    /**
     * Adds an item to the algorithm
     *
     * @param item the item to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    public boolean addItem(T item) {
        boolean result;
        final QuadItem<T> quadItem = new QuadItem<>(item);
        synchronized (mQuadTree) {
            result = mQuadList.add(quadItem);
            if (result) {
                mQuadTree.add(quadItem);
            }
        }
        return result;
    }

    /**
     * Adds a collection of items to the algorithm
     *
     * @param items the items to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    public boolean addItems(Collection<T> items) {
        boolean result = false;
        for (T item : items) {
            boolean individualResult = addItem(item);
            if (individualResult) {
                result = true;
            }
        }
        return result;
    }

    public void clearItems() {
        synchronized (mQuadTree) {
            mQuadList.clear();
            mQuadTree.clear();
        }
    }

    /**
     * Removes an item from the algorithm
     *
     * @param item the item to be removed
     * @return true if this algorithm contained the specified element (or equivalently, if this
     * algorithm changed as a result of the call).
     */
    public boolean removeItem(T item) {
        boolean result;
        // QuadItem delegates hashcode() and equals() to its item so,
        //   removing any QuadItem to that item will remove the item
        final QuadItem<T> quadItem = new QuadItem<>(item);
        synchronized (mQuadTree) {
            result = mQuadList.remove(quadItem);
            if (result) {
                mQuadTree.remove(quadItem);
            }
        }
        return result;
    }

    /**
     * Removes a collection of items from the algorithm
     *
     * @param items the items to be removed
     * @return true if this algorithm contents changed as a result of the call
     */
    public boolean removeItems(Collection<T> items) {
        boolean result = false;
        synchronized (mQuadTree) {
            for (T item : items) {
                // QuadItem delegates hashcode() and equals() to its item so,
                //   removing any QuadItem to that item will remove the item
                final QuadItem<T> quadItem = new QuadItem<>(item);
                boolean individualResult = mQuadList.remove(quadItem);
                if (individualResult) {
                    mQuadTree.remove(quadItem);
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Updates the provided item in the algorithm
     *
     * @param item the item to be updated
     * @return true if the item existed in the algorithm and was updated, or false if the item did
     * not exist in the algorithm and the algorithm contents remain unchanged.
     */
    public boolean updateItem(T item) {
        // TODO - Can this be optimized to update the item in-place if the location hasn't changed?
        boolean result;
        synchronized (mQuadTree) {
            result = removeItem(item);
            if (result) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                result = addItem(item);
            }
        }
        return result;
    }

    public Set<? extends Cluster<T>> getClusters(float zoom) {
        final int discreteZoom = (int) zoom;

        final double zoomSpecificSpan = mMaxDistance / Math.pow(2, discreteZoom) / 256;

        final Set<QuadItem<T>> visitedCandidates = new HashSet<>();
        final HashSet<Cluster<T>> results = new HashSet<>();
        final Map<QuadItem<T>, Double> distanceToCluster = new HashMap<>();
        final Map<QuadItem<T>, StaticCluster<T>> itemToCluster = new HashMap<>();

        synchronized (mQuadTree) {
            for (QuadItem<T> candidate : mQuadList) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue;
                }

                Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
                Collection<QuadItem<T>> searchBoundsItems = mQuadTree.search(searchBounds);
                if (searchBoundsItems.size() == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate);
                    visitedCandidates.add(candidate);
                    distanceToCluster.put(candidate, 0d);
                    continue;
                }
                StaticCluster staticCluster = new StaticCluster(candidate.getClusterItem().getPosition());
                results.add(staticCluster);

                for (QuadItem<T> clusterItem : searchBoundsItems) {
                    double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());

                    Double existingDistance = distanceToCluster.get(clusterItem);
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue;
                        }
                        // Move item to the closer cluster.
                        itemToCluster.get(clusterItem).remove(clusterItem.getClusterItem());
                    }
                    distanceToCluster.put(clusterItem, distance);
                    staticCluster.add(clusterItem.getClusterItem());
                    itemToCluster.put(clusterItem, staticCluster);
                }
                visitedCandidates.addAll(searchBoundsItems);
            }
        }
        return results;
    }


    public Collection<T> getItems() {
        final Set<T> items = new LinkedHashSet<>();
        synchronized (mQuadTree) {
            for (QuadItem<T> quadItem : mQuadList) {
                items.add(quadItem.getClusterItem());
            }
        }
        return items;
    }

    private double distanceSquared(Point a, Point b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private Bounds createBoundsFromSpan(Point p, double span) {
        // TODO: Use a span that takes into account the visual size of the marker, not just its
        // LatLng.
        double halfSpan = span / 2;
        return new Bounds(
                p.getX() - halfSpan, p.getX() + halfSpan,
                p.getY() - halfSpan, p.getY() + halfSpan);
    }


}
