include "globals.mzn";
include "1.dzn";

% Given input parameters
int: n;
int: n_commercial;
int: n_residential;
array[0..n] of int: point_distribution;

% n by n matrix where 0 is commercial and 1 is residential
array[1..n, 1..n] of var 0..1: matrix;

% Maximum points possible (solution)
var int: max_points;

% Array of row and columns of commercial lots
array[1..n] of var int: residential_column;
array[1..n] of var int: residential_row;

% Array of points for rows and columns
array[1..n] of var int: point_column;
array[1..n] of var int: point_row;

% Constraint - Symmetry breaker
constraint lex2(matrix);

% Constraint - # of residential lots in a row and in a column should equal n_residential
constraint sum(i in 1..n)(residential_row[i])= n_residential;
constraint sum(i in 1..n)(residential_column[i])= n_residential;

% Constraint - Gather residential_row and residential_column from the grid
constraint forall(i in 1..n)(residential_column[i]= sum(j in 1..n)(matrix[i, j]));
constraint forall(i in 1..n)(residential_row[i]= sum(j in 1..n)(matrix[j, i]));

% Constraint - Gather points for residential_row and residential_column
constraint forall(i in 1..n)(point_row[i]= point_distribution[residential_row[i]]);
constraint forall(i in 1..n)(point_column[i]= point_distribution[residential_column[i]]);

% Constraint - Get max_points from summing up all the points_row and points_column from the grid
constraint max_points = sum(i in 1..n)(point_row[i]+point_column[i]);
solve maximize max_points;

% Solution
output ["Lot matrix:\n", show2d(matrix), "\nMax points: ", show(max_points)];