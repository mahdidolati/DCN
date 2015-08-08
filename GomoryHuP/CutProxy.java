/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GomoryHuP;

import Sedgewick.FordFulkerson;
import java.util.List;

/**
 *
 * @author mahdi
 */
public class CutProxy {
    public FordFulkerson ff;
    public List<RowProxy> c;
    int s;
    int t;
    private List<Integer> sCut;
    private double val;
    //chosen for k-cut
    private boolean chosen;
    
    public CutProxy(FordFulkerson ff, List<RowProxy> c, int s, int t, List<Integer> sCut, double v) {
        this.ff = ff;
        this.c = c;
        this.t = t;
        this.s = s;
        this.sCut = sCut;
        this.val = v;
        this.chosen = false;
    }
    
    public void print() {
        //System.out.println("" + s + ", " +  t + ", " + this.val + ", " + this.sCut);
    }
    
    public double getVal() {
        return this.val;
    }
    
    public void setChosen() {
        this.chosen = true;
    }
    
    public boolean getChosen() {
        return this.chosen;
    }
    
    public List<Integer> getSCut() {
        return this.sCut;
    }
}
