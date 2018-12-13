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
        AutoRegressionFilter.solve(del_add, del_mul, number_add, number_mul, n, last, add, mul, dependencies);
    }

    private static void solve(int del_add, int del_mul, int number_add, int number_mul, int n, int[] last, int[] add,
                              int[] mul, int[][] dependencies) {
        Store store = new Store();

        ArrayList<IntVar> list = new ArrayList<>();

        IntVar[] ts = new IntVar[n];
        for (int i = 0; i < ts.length; i++){
            ts[i] = new IntVar(store, 0,100);
            list.add(ts[i]);
        }

        IntVar[] rs = new IntVar[n];
        for (int i = 0; i < add.length; i++){
            rs[add[i]-1] = new IntVar(store,1, number_add); //SET-UP
            list.add(rs[add[i]-1]);

        }
        for (int i = 0; i < mul.length; i++){
            rs[mul[i]-1] = new IntVar(store,1, number_mul); //MINUS 1 FOR INDEXING
            list.add(rs[mul[i]-1]);
        }


        for (int i = 0; i < add.length; i++){
            int[] fromdep = dependencies[add[i]-1];
            for (int j = 0; j < fromdep.length; j++){
                store.impose(new XplusClteqZ(ts[add[i]-1], del_add, ts[fromdep[j]-1]));
            }
        }

        for (int i = 0; i < mul.length; i++){
            int[] fromdep = dependencies[mul[i]-1];
            for (int j = 0; j < fromdep.length; j++){
                store.impose(new XplusClteqZ(ts[mul[i]-1], del_mul, ts[fromdep[j]-1]));
            }
        }

        IntVar one = new IntVar(store,1,1);
        IntVar AddCost = new IntVar(store, del_add,del_add);
        IntVar MulCost = new IntVar(store, del_mul,del_mul);

        IntVar[][] AddMat = new IntVar[add.length][1];
        IntVar[][] MulMat = new IntVar[mul.length][1];

        for (int i = 0; i < add.length; i++){
            IntVar[] vec = {ts[add[i]-1], rs[add[i]-1], AddCost, one};
            AddMat[i] = vec;
        }
        for (int i = 0; i < mul.length; i++){
            IntVar[] vec = {ts[mul[i]-1], rs[mul[i]-1], MulCost, one};
            MulMat[i] = vec;
        }

        store.impose(new Diff2(AddMat));
        store.impose(new Diff2(MulMat));

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
