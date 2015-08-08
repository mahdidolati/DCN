/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GomoryHuP;

import Sedgewick.FlowNetwork;
import Sedgewick.FordFulkerson;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author mahdi
 */

class ST {
    int s;
    int t;

    ST(int s, int t) {
        this.s = s;
        this.t = t;
    }
}

public class GomoryHu {
    
    private static List<RowProxy> newTr(List<RowProxy> tr, List<Integer> cut) {
        cut.sort(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                if(o1.intValue() < o2.intValue())
                    return 1;
                if(o1.intValue() > o2.intValue())
                    return -1;
                return 0;
            }
        });
        int min = Integer.MAX_VALUE;
        for(Integer m : cut) {
            if(m < min)
                min = m;
        }
        for(Integer j : cut) {
            if(j != min) {
                tr.get(min).addToAll(tr.get(j).all);
            }
        }        
        for(int i=0; i<tr.size(); ++i) {
            double val = 0.0;
            for(Integer j : cut) {
                val += tr.get(i).row.get(j);
            }
            tr.get(i).row.set(min, val);
            val = 0.0;
            for(Integer j : cut) {
                val += tr.get(j).row.get(i);
            }
            tr.get(min).row.set(i, val);
        }
        for(Integer j : cut) {
            if(j != min) {
                tr.remove(j.intValue());
            }
        }
        for(int i=0; i<tr.size(); ++i) {
            for(Integer j : cut) {
                if(j != min)
                    tr.get(i).row.remove(j.intValue());
            }
        }
        tr.get(min).row.set(min, 0.0);
        tr.get(min).chosen = true;
        return tr;
    }
    
    private static List<RowProxy> newTrCopy(List<RowProxy> tr, List<Integer> cut) {
        List<RowProxy> t = new ArrayList<>();
        for(int i=0; i<tr.size(); ++i) {
            t.add(tr.get(i).getCopy());
        }
        //what is this line?? 
//        return GomoryHu.newTr(t, cut);
        return t;
    }
    
    private static List<RowProxy> getRowProxied(List<List<Double>> tr) {
        List<RowProxy> t = new ArrayList<>();
        int size = tr.size();
        int i, j;
        for(i=0; i<size; ++i) {
            List<Double> ll = new ArrayList<>();
            for(j=0; j<size; ++j) {
                ll.add(tr.get(i).get(j));
            }
            RowProxy rp = new RowProxy(i, ll, null);
            t.add(rp);
        }
        return t;
    }
    
    private static ST getST(List<RowProxy> cs) {
        int s = -1;
        int t = -1;
        for(int i=0; i<cs.size(); ++i) {
            if(cs.get(i).chosen==false && s == -1) {
                s = i;
            }else if(cs.get(i).chosen==false && t == -1) {
                t = i;
            }
        }
        if(s == -1 || t == -1)
            return null;
        else
            return new ST(s, t);
    }
    
    private static CutProxy getCP(List<RowProxy> tr, int s, int t, List<Integer> l, double v) {
        int source;
        int destination;
        List<Integer> sCut = new ArrayList<>();
        source = tr.get(s).num;
        destination = tr.get(t).num;
        for(int i : l) {
            sCut.addAll(tr.get(i).all);
        }
        return new CutProxy(null, tr, source, destination, sCut, v);
    }
    
    private static List<CutProxy> getGoHuAux(List<RowProxy> tr, int s, int t) {
        List<CutProxy> cp = new ArrayList<>();
        FlowNetwork G = new FlowNetwork(tr);
        FordFulkerson maxflow = new FordFulkerson(G, s, t);
        cp.add(GomoryHu.getCP(tr, s, t, maxflow.getSCut(), maxflow.value()));
        
        ST st = null;
        List<RowProxy> cs = GomoryHu.newTrCopy(tr, maxflow.getSCut());
        st = GomoryHu.getST(cs);
        if(st != null) {
            cs.get(st.s).chosen = true;
            cs.get(st.t).chosen = true;
            List<CutProxy> sret = GomoryHu.getGoHuAux(cs, st.s, st.t);
            cp.addAll(sret);
        }

        st = null;
        List<RowProxy> ct = GomoryHu.newTrCopy(tr, maxflow.getDCut());
        st = GomoryHu.getST(ct);
        if(st != null) {
            ct.get(st.s).chosen = true;
            ct.get(st.t).chosen = true;
            List<CutProxy> tret = GomoryHu.getGoHuAux(ct, st.s, st.t);
            cp.addAll(tret);
        }
        
        return cp;
    }
    
    public static List<CutProxy> getGoHu(List<List<Double>> tr) {
        if(tr.size() <= 1)
            return new ArrayList<>();
        List<RowProxy> rowed = GomoryHu.getRowProxied(tr);
        int s = 0;
        int t = tr.size() - 1;
        rowed.get(s).chosen = true;
        rowed.get(t).chosen = true;
        return GomoryHu.getGoHuAux(rowed, s, t);
    }
    
    public static FordFulkerson getMinCut(List<List<Double>> tr, int s, int t) {
        List<RowProxy> rowed = GomoryHu.getRowProxied(tr);
        FlowNetwork G = new FlowNetwork(rowed);
        FordFulkerson maxflow = new FordFulkerson(G, s, t);
        return maxflow;
    }
    
}
