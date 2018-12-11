import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XgteqC;
import org.jacop.constraints.XlteqC;
import org.jacop.core.IntVar;

import java.util.ArrayList;
import java.util.Arrays;

public class ChoicePoint {

    IntVar var;
    IntVar[] searchVariables;
    int value;
    int varSelectMethod;

    public ChoicePoint(IntVar[] v, int varSelect) {
        varSelectMethod = varSelect;
        var = selectVariable(v);
        value = selectValue(var);
    }

    public IntVar[] getSearchVariables() {
        return searchVariables;
    }

    IntVar selectVariable(IntVar[] v) {
        if (v.length != 0) {
            int min = Integer.MAX_VALUE;
            int minIndex = 0;

            int maxDiff = 0;
            int maxDiffIndex = 0;

            int smallest = Integer.MAX_VALUE;
            int smallestIndex = 0;

            searchVariables = new IntVar[v.length - 1];
            for (int i = 0; i < v.length - 1; i++) {
                if (v[i].getSize() < min){ // for first fail
                    min = v[i].getSize();
                    minIndex = i;
                }
                if (v.length > 1){ // for max regret
                    if (v[i].dom().nextValue(v[i].min()) - v[i].dom().min() > maxDiff){
                        maxDiff = v[i].dom().nextValue(v[i].min()) - v[i].dom().min();
                        maxDiffIndex = i;
                    }
                }
                if (v[i].min() < smallest){
                    smallest = v[i].min();
                    smallestIndex = i;
                }
            }
            //variable selection
            if (varSelectMethod == 1) { //input order
                for (int i = 0; i < v.length - 1; i++) {
                    searchVariables[i] = v[i + 1];
                }
                return v[0];
            } else if (varSelectMethod == 2) { //first fail
                ArrayList<IntVar> list = new ArrayList<>();
                for (int i = 0; i < v.length; i++) {
                   list.add(v[i]);
                }
                list.remove(minIndex);
                list.toArray(searchVariables);
                return v[minIndex];
            } else if (varSelectMethod == 3) { //max regret
                ArrayList<IntVar> list = new ArrayList<>();
                for (int i = 0; i < v.length; i++) {
                    list.add(v[i]);
                }
                list.remove(maxDiffIndex);
                list.toArray(searchVariables);
                return v[maxDiffIndex];
            } else if (varSelectMethod == 4) { //smallest
                ArrayList<IntVar> list = new ArrayList<>();
                for (int i = 0; i < v.length; i++) {
                    list.add(v[i]);
                }
                list.remove(smallestIndex);
                list.toArray(searchVariables);
                return v[smallestIndex];
            } else {
                System.out.println("ERROR AT VARIABLE SELECT");
                return null; // error
            }

        } else {
            System.err.println("Zero length list of variables for labeling");
            return null;
        }
    }

    int selectValue(IntVar v) {
        return v.min();
    }

    IntVar getIntVar(){return var;}

    public PrimitiveConstraint getConstraint() {
        return new XeqC(var, value);
    }

    public PrimitiveConstraint getLteqConstraint() { return new XlteqC(var, Math.round((var.max() + var.min())/2));
    }
    public PrimitiveConstraint getGteqConstraint() {
        if ((var.max() + var.min()) % 2 == 0) {
            return new XgteqC(var, ((var.max() + var.min())/2));//dont add 1
        } else {
            return new XgteqC(var, ((var.max() + var.min()) / 2)+1);
        }
    }
}