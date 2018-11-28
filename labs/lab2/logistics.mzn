include "globals.mzn";

% given parameters
int: graph_size;
int: start;
int: n_dests;
array[1..n_dests] of int: dest;
int: n_edges;
array[1..n_edges] of int: from;
array[1..n_edges] of int: to;
array[1..n_edges] of int: cost;

% keep track of edges used in paths in a bool array
array[1..n_edges] of var bool: edge_used;

constraint % start node has >=1 connected edges
  sum(edge in 1..n_edges where from[edge] = start \/ to[edge] = start)(edge_used[edge]) >= 1;

constraint forall(node in 1..n_dests) ( % destination nodes have >=1 connected edges
  sum(edge in 1..n_edges where to[edge] = dest[node] \/ from[edge] = dest[node])(edge_used[edge]) >= 1
);

constraint forall(node in 1..graph_size) ( % in between nodes have either 0 or >=2 connected edges
  if (node != start /\ forall(i in 1..n_dests)(dest[i] != node)) then
    sum(edge in 1..n_edges where to[edge] = node \/ from[edge] = node)(edge_used[edge]) == 0 
    \/
    sum(edge in 1..n_edges where to[edge] = node \/ from[edge] = node)(edge_used[edge]) >= 2
  endif
);

int: max_cost;
max_cost = sum(edge in 1..n_edges)(cost[edge]);

var 0..max_cost: net_cost;

constraint
    net_cost = sum(i in 1..n_edges where edge_used[i])(cost[i]);

solve minimize net_cost;

output[show(net_cost)];