/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mahdi
 */
public class OutUtil {
    public static String EDGE = "edge";
    public static String AGG = "agg";
    public static String CORE = "core";
    public static String SERVER = "server";
    public static String NUMBER = "number";
    public static String VALUE = "value";
    Map<List<String>, Double> vals;
    Map<List<String>, Double> num;
    public OutUtil() {
        vals = new HashMap<>();
        num = new HashMap<>();
    }
    private List<String> getNewList() {
        return new LinkedList<String>() {
            @Override
            public int hashCode() {
                int h = 0;
                for(String str : this) {
                    h += str.hashCode();
                }
                return h;
            }
            @Override
            public boolean equals(Object o) {
                if(o instanceof LinkedList == false)
                    return false;
                if(((List)o).size() != this.size())
                    return false;
                return ((List)o).containsAll(this);
            }
        };
    }
    public void addVal(List<String> key, double val) {
        if(vals.containsKey(key)) {
            
        }
//        if(vals.containsKey(device) && vals.get(device).containsKey(stat)) {
//            statNum.get(device).put(stat, 1+statNum.get(device).get(stat));
//            vals.get(device).put(stat, (vals.get(device).get(stat)+val)/statNum.get(device).get(stat));
//        } else if(vals.containsKey(device)) {
//            vals.get(device).put(stat, val);
//            statNum.get(device).put(stat, 1);
//        } else {
//            Map<String, Double> tm = new HashMap<>();
//            tm.put(stat, val);
//            vals.put(device, tm);
//            Map<String, Integer> tmi = new HashMap<>();
//            tmi.put(stat, 1);
//            statNum.put(device, tmi);
//        }
    }
}
