/**
 * Created by soohyunkim on 2018-11-08.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacop.core.*;
import org.jacop.constraints.*;
import org.jacop.search.*;

public class Photo {
    public static void main(String[] args) {

         // Example Input 1
         int n = 9;
         int n_prefs = 17;
         int[][] prefs = {{1,3}, {1,5}, {1,8},
             {2,5}, {2,9}, {3,4}, {3,5}, {4,1},
             {4,5}, {5,6}, {5,1}, {6,1}, {6,9},
             {7,3}, {7,8}, {8,9}, {8,7}};

         /*
         // Example Input 2
         int n = 11;
         int n_prefs = 20;
         int[][] prefs = {{1,3}, {1,5}, {2,5},
             {2,8}, {2,9}, {3,4}, {3,5}, {4,1},
             {4,5}, {4,6}, {5,1}, {6,1}, {6,9},
             {7,3}, {7,5}, {8,9}, {8,7}, {8,10},
             {9, 11}, {10, 11}};

         // Example Input 3
         int n = 15;
         int n_prefs = 20;
         int[][] prefs = {{1,3}, {1,5}, {2,5},
                 {2,8}, {2,9}, {3,4}, {3,5}, {4,1},
                 {4,15}, {4,13}, {5,1}, {6,10}, {6,9},
                 {7,3}, {7,5}, {8,9}, {8,7}, {8,14},
                 {9, 13}, {10, 11}};
         */

         new Photo().solve(n, n_prefs, prefs);
    }

    private static void solve(int n, int n_prefs, int[][] prefs) {

        Store store = new Store();

        IntVar[] persons = new IntVar[n];

        for (int i = 0; i < n; i++) {
            persons[i] = new IntVar(store, "p" + (i+1), 1, n);
        }

        store.impose(new Alldiff(persons));

        IntVar[] distances = new IntVar[n_prefs];
        for (int i = 0; i < n_prefs; i++) {
            int firstPerson = prefs[i][0];
            int secondPerson = prefs[i][1];
            IntVar from = persons[firstPerson - 1];
            IntVar to = persons[secondPerson - 1];

            // store distance constraint (distance can only be between 1 to n-1)
            distances[i] = new IntVar(store, "distance between " + firstPerson + " and " + secondPerson, 1, n - 1);
            Constraint distanceConstraint = new Distance(from, to, distances[i]);
            store.impose(distanceConstraint);
        }


        IntVar[] isFulfilled = new IntVar[n_prefs];
        for (int i = 0; i < n_prefs; i++) {
            isFulfilled[i] = new IntVar(store, "r" + (i+1), 0, 1);
            Constraint reifiedConstraint = new Reified(new XeqC(distances[i], 1), isFulfilled[i]);
            store.impose(reifiedConstraint);
        }

        // get cost
        IntVar cost = new IntVar(store, 0, n_prefs);
        Constraint sumConstraint = new SumInt(isFulfilled, "==", cost);
        store.impose(sumConstraint);

        // Original problem: find a placement that satisfies maximum number of preferences
        IntVar negCost = new IntVar(store, -n_prefs, 0);
        store.impose(new XplusYeqC(cost, negCost, 0));

        // Modified problem: find a placement that also minimizes the maximal distance between two persons from the preference list
        // additional constraint added
        IntVar cost2 = new IntVar(store, 1, n - 1);
        store.impose(new Max(distances, cost2));

        // Use DFS to find the solution
        SelectChoicePoint<IntVar> select = new SimpleSelect<>(persons, new SmallestDomain<>(),
                new IndomainMin<IntVar>());
        Search<IntVar> label = new DepthFirstSearch<>();
        System.out.println(">> STARTING SEARCH");
        boolean result = label.labeling(store, select, negCost);
        // boolean result = label.labeling(store, select, cost2);

        if (result) {
            System.out.println(">> YES, THERE IS A SOLUTION");
            System.out.println("Solution: " + java.util.Arrays.asList(persons));
        } else {
            System.out.println(">> NO SOLUTION");
        }
    }
}
