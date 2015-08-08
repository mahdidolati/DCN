/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GomoryHuP;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mahdi
 */
public class RowProxy {
    public int num;
    public List<Integer> all;
    public List<Double> row;
    //is it already been a source or a sink or not
    public boolean chosen;
    
    public RowProxy() {
        this.row = new ArrayList<>();
        this.all = new ArrayList<>();
        this.chosen = false;
    }
    
    public RowProxy(int n, List<Double> l, List<Integer> a) {
        this.num = n;
        this.row = new ArrayList<>();
        this.all = new ArrayList<>();
        this.all.add(n);
        this.chosen = false;
        if(l != null)
            for(int i=0; i<l.size(); ++i) {
                this.row.add(l.get(i));
            }
        if(a != null)
            for(int i=0; i<a.size(); ++i) {
                this.all.add(a.get(i));
            }
    }
    
    public void addToAll(int n) {
        this.all.add(n);
    }
    
    public void addToAll(List<Integer> l) {
        this.all.addAll(l);
    }
    
    public RowProxy getCopy() {
        RowProxy r = new RowProxy();
        r.num = this.num;
        r.chosen = this.chosen;
        if(this.row != null)
            for(int i=0; i<this.row.size(); ++i) {
                r.row.add(this.row.get(i));
            }
        if(this.all != null)
            for(int i=0; i<this.all.size(); ++i) {
                r.all.add(this.all.get(i));
            }
        return r;
    }
}
