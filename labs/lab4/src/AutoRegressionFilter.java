import org.jacop.core.*;
import org.jacop.constraints.*;
import org.jacop.search.*;

import java.util.ArrayList;

public class AutoRegressionFilter {

    public static void main(String[] args){
        int del_add = 1;
        int del_mul = 2;
        int number_add = 1;
        int number_mul = 1;
        int n = 28;
        int[] last = {27,28};
        int[] add = {9,10,11,12,13,14,19,20,25,26,27,28};
        int[] mul = {1,2,3,4,5,6,7,8,15,16,17,18,21,22,23,24};
        int[][] dependencies = {{9}, {9}, {10}, {10}, {11}, {11}, {12}, {12}, {27}, {28}, {13}, {14}, {16,17}, {15,18}, {19}, {19}, {20}, {20}, {22,23}, {21,24}, {25}, {25},
                {26},
                {26},
                {27},
                {28},
                {},
                {},
        };
        AutoRegressionFilter autoRegressionFilter = new AutoRegressionFilter();
        autoRegressionFilter.solve(del_add, del_mul, number_add, number_mul, n, last, add, mul, dependencies);
    }

    private void addConstraintToList(IntVar[] rs, int[] addOrMulSet, int number, ArrayList<IntVar> list, Store store) {
        for (int i = 0; i < addOrMulSet.length; i++){
            rs[addOrMulSet[i]-1] = new IntVar(store,1, number); //SET-UP
            list.add(rs[addOrMulSet[i]-1]);
        }
    }

    private void lessThanOrEqualToConstraint(IntVar[] ts, int[][] dependencies, int[] addOrMulSet, int del, Store store) {
        for (int i = 0; i < addOrMulSet.length; i++){
            int[] fromdep = dependencies[addOrMulSet[i]-1];
            for (int j = 0; j < fromdep.length; j++){
                store.impose(new XplusClteqZ(ts[addOrMulSet[i]-1], del, ts[fromdep[j]-1]));
            }
        }
    }

    private void diff2Constraint(IntVar[] ts, IntVar[] rs, int[] addOrMulSet, int del, Store store){
        IntVar one = new IntVar(store,1,1);
        IntVar cost = new IntVar(store, del, del);
        IntVar[][] mat = new IntVar[addOrMulSet.length][1];
        for (int i = 0; i < addOrMulSet.length; i++){
            IntVar[] vec = {ts[addOrMulSet[i]-1], rs[addOrMulSet[i]-1], cost, one};
            mat[i] = vec;
        }
        store.impose(new Diff2(mat));
    }

    private void solve(int del_add, int del_mul, int number_add, int number_mul, int n, int[] last, int[] add,
                              int[] mul, int[][] dependencies) {
        Store store = new Store();

        ArrayList<IntVar> list = new ArrayList<>();

        IntVar[] ts = new IntVar[n];
        for (int i = 0; i < ts.length; i++){
            ts[i] = new IntVar(store, 0,100);
            list.add(ts[i]);
        }

        IntVar[] rs = new IntVar[n];
        addConstraintToList(rs, add, number_add, list, store);
        addConstraintToList(rs, mul, number_mul, list, store);

        lessThanOrEqualToConstraint(ts, dependencies, add, del_add, store);
        lessThanOrEqualToConstraint(ts, dependencies, mul, del_mul, store);

        diff2Constraint(ts, rs, add, del_add, store);
        diff2Constraint(ts, rs, mul, del_mul, store);

        IntVar[] ends = new IntVar[last.length];
        for (int i = 0; i < ends.length; i++){
            ends[i] = new IntVar(store, 0,100);
            list.add(ends[i]);
            store.impose(new XplusCeqZ(ts[last[i]-1],1,ends[i]));
        }

        IntVar completionTime = new IntVar(store,0,100);
        store.impose(new Max(ends, completionTime));

        IntVar[] varArray = new IntVar[list.size()];
        for (int i = 0; i < varArray.length; i++){
            varArray[i] = list.get(i);
        }
        Search<IntVar> search = new DepthFirstSearch<>();
        SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store, varArray, new IndomainMin<>());
        search.labeling(store, select, completionTime); // ans as last parameter - problem 1, max as last parameter - problem 2
    }

}
