/**
 * SplitDFS1.java
 * This file is part of JaCoP.
 * <p>
 * JaCoP is a Java Constraint Programming solver.
 * <p>
 * Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * Notwithstanding any other provision of this License, the copyright
 * owners of this work supplement the terms of this License with terms
 * prohibiting misrepresentation of the origin of this work and requiring
 * that modified versions of this work be marked in reasonable ways as
 * different from the original version. This supplement of the license
 * terms is in accordance with Section 7 of GNU Affero General Public
 * License version 3.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.jacop.constraints.*;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

public class SplitDFS {

    boolean trace = false;
    Store store;
    IntVar[] variablesToReport;
    int depth = 0;
    public int costValue = IntDomain.MaxInt;
    public IntVar costVariable = null;

    public int nrOfNodes, nrOfWrongDecisions =0;
    int splitId;
    int varSelect;

    public SplitDFS(Store s, int id, int varSel) {
        store = s;
        splitId = id;
        varSelect = varSel;
    }

    public boolean label(IntVar[] vars) {
        ChoicePoint choice = null;
        boolean consistent;

        if (costVariable != null) {
            try {
                if (costVariable.min() <= costValue - 1)
                    costVariable.domain.in(store.level, costVariable, costVariable.min(), costValue - 1);
                else
                    return false;
            } catch (FailException f) {
                return false;
            }
        }
        consistent = store.consistency();
        if (trace) { // DEBUG
            System.out.println("After .consistency() ");
            for (int j = 0; j < variablesToReport.length; j++) {
                System.out.print(variablesToReport[j] + " ");
            }
            System.out.println();
        }

        if (!consistent) {
            // Failed leaf of the search tree
            nrOfWrongDecisions++;
            return false;
        } else { // consistent
            nrOfNodes++;
            if (vars.length == 0) {
                if (costVariable != null)
                    costValue = costVariable.min();

                reportSolution();
                return costVariable == null; // true is satisfiability search and false if minimization
            }
            choice = new ChoicePoint(vars, varSelect);
            levelUp();

            IntVar choiceVar = choice.getIntVar();
            if (choiceVar.min() == choiceVar.max()){
//                if (trace) System.out.println("min == max! for " + choiceVar);
                consistent = label(choice.getSearchVariables());
                levelDown();
                return consistent;
            } else {
//                if (trace) System.out.println("Imposing constraint - var " +choice.var + " <= " + (choice.var.min() + choice.var.max())/2 );
                if (splitId == 1 ) store.impose(choice.getLteqConstraint()); else if (splitId == 2) store.impose(choice.getGteqConstraint());

                consistent = label(vars);
                if (consistent){
                    levelDown();
                    return true;
                } else {
                    restoreLevel();
//                    if (trace) System.out.println("Imposing constraint - var " +choice.var + " > " + (choice.var.min() + choice.var.max())/2 );
                    if (splitId == 1 ) store.impose(new Not(choice.getLteqConstraint())); else if (splitId == 2) store.impose( new Not(choice.getGteqConstraint()));
                    consistent = label(vars);

                    levelDown();
                    return consistent;
                }
            }
        }
    }

    void levelDown() {
        store.removeLevel(depth);
        store.setLevel(--depth);
    }

    void levelUp() {
        store.setLevel(++depth);
    }

    void restoreLevel() {
        store.removeLevel(depth);
        store.setLevel(store.level);
    }
    public void printStatistics(){
        System.out.println("=== SPLITDFS type " + splitId + " ===");
        if (varSelect == 1){
            System.out.println("Variable Select Method: Input Order");
        } else if (varSelect == 2){
            System.out.println("Variable Select Method: First Fail");
        } else if (varSelect == 3){
            System.out.println("Variable Select Method: Max Regret");
        } else if (varSelect == 4){
            System.out.println("Variable Select Method: Smallest");
        }

        System.out.println("Number of nodes: "+ nrOfNodes);
        System.out.println("Number of wrong decisions: " + nrOfWrongDecisions + "\n");
    }

    public void reportSolution() {
        if (costVariable != null)
            System.out.println("Cost is " + costVariable);

        for (int i = 0; i < variablesToReport.length; i++)
            System.out.print(variablesToReport[i] + " ");
        System.out.println("\n---------------");
    }

    public void setVariablesToReport(IntVar[] v) {
        variablesToReport = v;
    }

    public void setCostVariable(IntVar v) {
        costVariable = v;
    }
}
