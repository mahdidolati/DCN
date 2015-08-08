/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package KCutSaran;

import GomoryHuP.CutProxy;
import Sedgewick.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mahdi
 */
public class KCut {
    
    public static List<List<Integer>> getKCut(int k, List<List<Double>> tr) {
        List<List<Double>> trt = new ArrayList<>();
        for(List<Double> l : tr) {
            List<Double> t = new ArrayList<>();
            for(double d : l) {
                t.add(d);
            }
            trt.add(t);
        }
        List<CutProxy> cuts = GomoryHuP.GomoryHu.getGoHu(trt);
        trt = KCut.getKCuts(k, cuts, trt);
        return KCut.getComponents(trt);
    }
    
    private static List<List<Integer>> balance(List<List<Integer>> cuts, int size) {
        for(List<Integer> l : cuts) {
            if(l.size() > size) {
                
            }
        }
        return cuts;
    }
    
    public static List<List<Double>> getKCuts(int k, List<CutProxy> cuts, List<List<Double>> tr) {
        int compNum = KCut.getComponentNum(tr);
        while(compNum < k && KCut.isAvail(cuts)) {
            CutProxy minCut = KCut.getMinAvail(cuts);
            List<Integer> cut = minCut.getSCut();
            KCut.applyCut(cut, tr);
            compNum = KCut.getComponentNum(tr);
            while(compNum > k) {
                for(int i=0; i<tr.size(); i++) {
                    for(int j=0; j<tr.size(); j++) {
                        if(i > j)
                            continue;
                        if((cut.contains(i) && !cut.contains(j)) || (cut.contains(j) && !cut.contains(i))) {
                            tr.get(i).set(j, tr.get(i).get(j)*-1);
                            tr.get(j).set(i, tr.get(j).get(i)*-1);
                        }
                    }
                }
                compNum = KCut.getComponentNum(tr);
            }
        }
        return tr;
    }
    
    public static void applyCut(List<Integer> cut, List<List<Double>> tr) {
        for(int i=0; i<tr.size(); i++) {
            for(int j=0; j<tr.size(); j++) {
                if(i > j)
                    continue;
                if(tr.get(i).get(j) < 0)
                    continue;
                if(tr.get(i).get(j) < 0)
                    continue;
                if((cut.contains(i) && !cut.contains(j)) || (cut.contains(j) && !cut.contains(i))) {
                    tr.get(i).set(j, tr.get(i).get(j)*-1);
                    tr.get(j).set(i, tr.get(j).get(i)*-1);
                }
            }
        }
    }
    
    public static int getComponentNum(List<List<Double>> tr) {
        Queue<Integer> q = new Queue<>();
        List<Integer> seen = new ArrayList<>();
        int comp = 0;
        for(int i=0; i<tr.size(); ++i) {
            if(seen.contains(i) == false) {
                seen.add(i);
                q.enqueue(i);
                comp++;
                while(q.isEmpty() == false) {
                    int parent = q.dequeue();
                    for(int j=0; j<tr.size(); ++j) {
                        if(tr.get(parent).get(j) > 0 || tr.get(j).get(parent) > 0) {
                            if(seen.contains(j) == false) {
                                q.enqueue(j);
                                seen.add(j);
                            }
                        }
                    }
                }
            }
        }
        return comp;
    }
    
    public static List<List<Integer>> getComponents(List<List<Double>> tr) {
        Queue<Integer> q = new Queue<>();
        List<Integer> seen = new ArrayList<>();
        List<List<Integer>> comps = new ArrayList<>();
        for(int i=0; i<tr.size(); ++i) {
            if(seen.contains(i) == false) {
                List<Integer> curComp = new ArrayList<>();
                seen.add(i);
                curComp.add(i);
                q.enqueue(i);
                while(q.isEmpty() == false) {
                    int parent = q.dequeue();
                    for(int j=0; j<tr.size(); ++j) {
                        if(tr.get(parent).get(j) > 0 || tr.get(j).get(parent) > 0) {
                            if(seen.contains(j) == false) {
                                q.enqueue(j);
                                seen.add(j);
                                curComp.add(j);
                            }
                        }
                    }
                }
                comps.add(curComp);
            }
        }
        return comps;
    }
    
    public static CutProxy getMinAvail(List<CutProxy> cuts) {
        CutProxy c = null;
        double best = Double.MAX_VALUE;
        for(CutProxy cut : cuts) {
            if(cut.getChosen()==false && cut.getVal() < best) {
                best = cut.getVal();
                c = cut;
            }
        }
        if(c != null)
            c.setChosen();
        return c;
    }
    
    public static boolean isAvail(List<CutProxy> cuts) {
        for(CutProxy cut : cuts) {
            if(cut.getChosen()==false) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
        List<List<Double>> tr = new ArrayList<>();
        int size = 22;
        for(int i=0; i<size; ++i) {
            List<Double> t = new ArrayList<>();
            for(int j=0; j<size; ++j) {
                double d = Math.random()*10;
                if(i == j)
                    d = 0.0;
                int m = (int)d*1000;
                t.add(m/1000.0);
                //System.out.print(" "+m/1000.0);
            }
            //System.out.println("");
            tr.add(t);
        }
        
        //System.out.println("component number before rum: " + KCut.getComponentNum(tr));
        
        //System.out.println("----------Components----------");
        for(List<Integer> l : KCut.getKCut(15, tr)) {
            for(int i : l) {
                //System.out.print(" " + i);
            }
            //System.out.println("");
        }
        
    }
    
}
