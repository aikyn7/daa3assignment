# Assignment 3 Report — Graph Algorithms

**Student:** Aikyn Zhumagaliuly · **Group:** SE-2516

## 1. Design and implementation
The project uses one adjacency-list `Graph` class for directed and undirected weighted graphs. The input format is `n m directed|undirected`, followed by `m` lines `u v w`. Self-loops and parallel edges are retained; isolated vertices are represented by empty adjacency lists. DSU combines union by size and path compression. Kruskal sorts the edge list and uses DSU; Prim uses a priority queue and starts a new component whenever an unvisited vertex remains, thereby producing a minimum spanning forest for disconnected input. Kosaraju uses explicit stacks rather than recursive DFS. Kahn reports a cycle if it cannot output all vertices. DAG shortest paths initialize every zero-indegree vertex to distance zero, then relax edges in topological order; parent pointers reconstruct a path.

## 2. Complexity table

| Operation / algorithm | Time | Extra space | Justification |
|---|---:|---:|---|
| DSU `find` / `union` | Amortized `O(α(V))` per operation | `O(V)` total DSU storage | Union by size bounds tree height and path compression flattens future paths; the inverse Ackermann factor is effectively constant. |
| Kruskal | `O(E log E + E α(V))` | `O(V + E)` | Sorting dominates; each edge performs a constant number of DSU operations. |
| Prim (priority queue) | `O((V+E) log E)` | `O(V+E)` | Each adjacency arc can add a candidate to the heap; each heap operation is logarithmic. |
| Kosaraju | `O(V+E)` | `O(V+E)` | Two iterative DFS passes and a reversed adjacency list visit each vertex and arc a constant number of times. |
| Kahn | `O(V+E)` | `O(V)` | In-degrees are computed once; every vertex is queued once and each outgoing arc is processed once. |
| DAG shortest paths | `O(V+E)` | `O(V)` beyond the graph | One topological ordering and one relaxation pass suffice; distance and parent arrays are linear. |
| Tarjan SCC (bonus) | `O(V+E)` | `O(V)` | Each vertex is indexed once and each arc is considered once using low-link values and an active stack. |

## 3. Correctness arguments

### Cut property and minimum spanning trees
For any cut that separates vertices into two sets, a lightest edge crossing that cut is safe for a minimum spanning tree: if an MST does not contain that edge, adding it creates a cycle. That cycle must contain another edge crossing the same cut. Replacing that other edge with the lightest crossing edge cannot increase total weight, so an MST containing the selected edge exists. Kruskal repeatedly selects the lightest edge that joins two different DSU components, so it never creates a cycle and each selected edge is safe across a cut between components. Prim grows a tree from its current vertex set by choosing a minimum-weight edge crossing from the tree to an unvisited vertex. The cut property makes that choice safe at every step. Repeating until no reachable vertex remains, then starting another component, gives a minimum spanning forest for disconnected graphs. Thus both algorithms must agree on total forest weight even if equal-weight ties lead to different edges.

### Induction on topological order for DAG shortest paths
A topological ordering places every predecessor of a vertex before that vertex. Initially, every source (zero-indegree vertex) has distance zero, representing a virtual super-source connected to all sources by zero-weight edges. These source distances are correct because no original edge enters a source. Assume all distances are correct when a vertex `u` is processed. Every path to a later vertex `v` has a final edge from some predecessor, and all such predecessors are processed before `v`. Relaxing each outgoing edge from each processed vertex therefore considers every possible final edge and keeps the minimum candidate distance. By induction, when each vertex is reached, its distance is final. Negative edge weights are safe because a DAG has no directed cycles, so no path can loop to reduce its weight indefinitely; in a general graph, a reachable negative cycle can make shortest-path distances undefined.

## 4. Counter interpretation
The counters are collected inside the algorithms rather than calculated from asymptotic formulas. For Kosaraju, `edge_scans` should be `2E` on a directed graph because the first DFS scans the original adjacency arcs and the second scans the reversed graph. Kahn and DAG relaxation each scan `E` arcs on a DAG. These identities are useful sanity checks. For Kruskal, the sort comparison count depends on the sorting implementation and input ordering, while the DSU work is proportional to the number of edge decisions. For cross-size comparison, divide `edge_scans` by `V+E` for Kosaraju and by `E` for Kahn / DAG relaxation; the ratios remain bounded near constants. The CSV records median time from five runs, but the operation counts are the stronger evidence because timing varies with CPU, JVM, garbage collection, and background load.

## 5. Which algorithm when?

| Situation | Choice | Reason |
|---|---|---|
| Sparse undirected graph, edge list available | Kruskal | Sorting edges is practical, and DSU cheaply rejects cycle-forming edges. |
| Dense undirected graph stored as adjacency lists | Prim is a useful candidate | It grows from vertices and uses a priority queue; compare actual heap overhead and counters for the workload. |
| Need an MST but graph is disconnected | Kruskal or component-aware Prim | Both return a minimum spanning forest and component count. |
| Need strongly connected components | Kosaraju or Tarjan | Both are linear; Kosaraju is easy to reason about, while Tarjan avoids building a reversed graph. |
| Need dependency order and graph is expected to be a DAG | Kahn | Linear time and explicit cycle detection. |
| Need shortest paths in a DAG, possibly with negative edges | Topological relaxation | Linear time; no Dijkstra nonnegative-weight restriction is needed. |
| Negative cycles may exist in a general graph | Bellman–Ford for detection / shortest paths | DAG relaxation is not applicable when cycles may exist. |

## 6. Plots and slope check
The `results/plots/` directory contains three plots generated from measured rows in the included CSV. The largest dense configuration has not been measured yet (its CSV rows use `time_ms=-1`), so the plots omit those five rows. Regenerate the plots after running the full benchmark using the completed CSV. On log-log axes, a traversal with work proportional to `V+E` has slope close to 1 when density is fixed as a constant multiple of `V`. Kruskal has a slightly larger effective slope due to the `E log E` sorting term. The scans-per-edge curves should be flat: approximately 2 for Kosaraju, 1 for Kahn, and 1 for DAG relaxation.

## 7. Timing paragraph
The benchmark records the median of five runs to reduce the impact of individual pauses. Absolute times are not portable: CPU model, JDK implementation, heap size, garbage collection, thermal throttling, and background tasks can change them. The report therefore emphasizes operation counts and ratios over claims based on one laptop's timing. Run the supplied benchmark on the submission machine before the defense and replace conceptual plots with plots calculated from that run if empirical plots are required by the instructor.

## 8. Limitations to verify before submission
Run `mvn clean test` and the full benchmark locally. Five rows for the largest dense configuration are explicitly marked unmeasured and must be regenerated. The reference table in the assignment is a strict validation target; if generated results differ, inspect the generator's exact random-number consumption and the required counter definitions before submission. Git branch/tag history is repository-specific and must be created in the student's own Git repository.
