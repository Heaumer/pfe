/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan.choco.search;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.constraint.pack.SatisfyDemandingSliceHeights;
import gnu.trove.TLongIntHashMap;

/**
 * A heuristic to try to assign the virtual machines to migrate
 * or to resume to its current (or previous) location.
 * If it is not possible, it consider the current residual capacity of the nodes
 * to choose the one to test.
 *
 * @author Fabien Hermenier
 */
public class StayFirstSelector2 implements ValSelector<IntDomainVar> {
    public enum Option {
        wfMem, wfCPU, bfMem, bfCPU
    }


    private Option opt;

    /**
     * The previous location of the running VMs.
     */
    private TLongIntHashMap oldLocation = new TLongIntHashMap();

    private SatisfyDemandingSliceHeights pack;

    private ReconfigurationProblem rp;

    /**
     * Build a selector for a specific solver.
     *
     * @param s    the solver
     * @param pack the pack constraint
     * @param o    the option to customize the heuristic
     */
    public StayFirstSelector2(ReconfigurationProblem s, TLongIntHashMap oldLocation, SatisfyDemandingSliceHeights pack, Option o) {
        this.opt = o;
        this.pack = pack;
        rp = s;

        this.oldLocation = oldLocation;
    }

    /**
     * Get the bin with the maximum remaining space.
     *
     * @param place the hoster variable of the slice to place.
     * @param dim   0 for the CPU dimension, 1 for the memory dimension
     * @return {@code -1} if no host is available, otherwise the index of the node.
     */
    private int worstFit(IntDomainVar place, int dim) {
        DisposableIntIterator ite = place.getDomain().getIterator();
        int maxIdx = -1;
        int maxVal = -1;

        try {
            while (ite.hasNext()) {
                int bIdx = ite.next();
                int bVal = dim == 0 ? pack.getRemainingCPU(bIdx) : pack.getRemainingMemory(bIdx);
                //Plan.logger.debug("Node N" + bIdx + " free=" + bVal);
                if (bVal > maxVal) {
                    maxVal = bVal;
                    maxIdx = bIdx;
                }
            }
        } finally {
            ite.dispose();
        }
        //Plan.logger.debug("Choose N" + maxIdx);
        return maxIdx;
    }

    /**
     * Get the bin with the minimum remaining space.
     *
     * @param place the hoster variable of the slice to place.
     * @param dim   0 for the CPU dimension, 1 for the memory dimension
     * @return {@code -1} if no host is available, otherwise the index of the node.
     */
    private int bestFit(IntDomainVar place, int dim) {
        int minIdx = -1;
        int minVal = Integer.MAX_VALUE;

        for (int bIdx = place.getInf(); bIdx <= place.getSup(); bIdx = place.getDomain().getNextValue(bIdx)) {
            int bVal = dim == 0 ? pack.getRemainingCPU(bIdx) : pack.getRemainingMemory(bIdx);
            if (bVal < minVal) {
                minVal = bVal;
                minIdx = bIdx;
            }
        }
        return minIdx;
    }


    @Override
    public int getBestVal(IntDomainVar var) {
        int val = this.oldLocation.get(var.getIndex());
        //Check wether the VM can stay on the same node.
        if (this.oldLocation.containsKey(var.getIndex()) && var.canBeInstantiatedTo(val)) {
            System.err.println(var + " stay on " + rp.getNode(val).getName());
            return val;
        }
        int to;
        switch (opt) {
            case wfCPU:
                to = worstFit(var, 0);
                break;
            case wfMem:
                to = worstFit(var, 1);
                break;
            case bfCPU:
                to = bestFit(var, 0);
                break;
            case bfMem:
                to = bestFit(var, 1);
                break;
            default:
                to = var.getInf();
        }
        System.err.println(var + " move to " + rp.getNode(to).getName());
        return to;
    }
}
