/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author mahdi
 */

public class Output {
    public static String EDGE = "edge";
    public static String AGG = "agg";
    public static String CORE = "core";
    public static String SERVER = "server";
    public static String NUMBER = "number";
    public static String VALUE = "value";
    public static String I_SERVER = "img_server";
    public static String DC = "dc";
    public static String NETWORK = "network";
    public static String NET_MIN = "net_min";
    public static String ALL_TRAFFIC = "all_traffic";
    
    Map<String, Map<String, Double>> vals;
    Map<String, Map<String, Integer>> statNum;
            
    public Output() {
        vals = new HashMap<>();
        statNum = new HashMap<>();
    }
    
    public void setVal(String device, String stat, double val) {
        Map<String, Double> tm = new HashMap<>();
        tm.put(stat, val);
        vals.put(device, tm);
        Map<String, Integer> tmi = new HashMap<>();
        tmi.put(stat, 1);
        statNum.put(device, tmi);
    }
    
    public void addVal(String device, String stat, double val) {
        this.addVal(device, stat, val, 1);
    }

    public void addVal(String device, String stat, double val, int num) {
        if(vals.containsKey(device) && vals.get(device).containsKey(stat)) {
            int newNum = num+statNum.get(device).get(stat);
            double newVal = 
                    1.0/(newNum)*val + 
                    (newNum-1.0)/(newNum*1.0)*vals.get(device).get(stat);
            statNum.get(device).put(stat, newNum);
            vals.get(device).put(stat, newVal);
        } else if(vals.containsKey(device)) {
            vals.get(device).put(stat, val);
            statNum.get(device).put(stat, num);
        } else {
            Map<String, Double> tm = new HashMap<>();
            tm.put(stat, val);
            vals.put(device, tm);
            Map<String, Integer> tmi = new HashMap<>();
            tmi.put(stat, num);
            statNum.put(device, tmi);
        }
    }
    
    public void addOutput(Output o) {
        for(String device : o.vals.keySet()) {
            for(String stat : o.vals.get(device).keySet()) {
//                LogUtil.LOGGER.log(Level.INFO, "Adding Out: {0}", o.vals.get(s).get(t));
                this.addVal(device, stat, o.vals.get(device).get(stat), o.statNum.get(device).get(stat));
            }
        }
    }
    
    public String getCaption() {
        String ret = "";
        for(String device : this.vals.keySet()) {
            for(String stat : this.vals.get(device).keySet()) {
                ret = ret + device + "-" + stat + ", ";
            }
        }
        return ret;
    }
    
    public String getValString() {
        double netusage = 0.0;
        double srvusage = 0.0;
        for(String device : this.vals.keySet()) {
            for(String stat : this.vals.get(device).keySet()) {
                if(stat.equals(Output.NUMBER) == false)
                    if(device.equals(Output.SERVER))
                        srvusage += this.vals.get(device).get(stat);
                    else
                        netusage += this.vals.get(device).get(stat);
            }
        }
        return Double.toString(srvusage) + ", " + Double.toString(netusage)+", ";
    }
    
    @Override
    public String toString() {
        String ret = "";
        for(String device : this.vals.keySet()) {
            for(String stat : this.vals.get(device).keySet()) {
//                LogUtil.LOGGER.log(Level.INFO, "device, stat: {0} {1}", new String[]{device, stat});
                ret = ret + this.vals.get(device).get(stat) + ", ";
            }
        }
        return ret;
    }

    public Map<String, Map<String, Double>> getVals() {
        return vals;
    }

    public void setVals(Map<String, Map<String, Double>> vals) {
        this.vals = vals;
    }

    public double getDcConsumption() {
        return this.getNetworkConsumption()+this.getStat(Output.SERVER, Output.VALUE);
    }
    
    public double getStat(String d, String s) {
        if(this.vals.containsKey(d))
            if(this.vals.get(d).containsKey(s))
                return this.vals.get(d).get(s);
            else
                return Double.NaN;
        else {
            if(d.equals(Output.DC))
                return this.getDcConsumption();
            if(d.equals(Output.NETWORK))
                if(s.equals(Output.VALUE))
                    return this.getNetworkConsumption();
                else if(s.equals(Output.NUMBER))
                    return this.getNetworkNumber();
        }
        return Double.NaN;
    }
    
    public double getNetworkConsumption() {
        return this.getStat(Output.EDGE, Output.VALUE) +
                this.getStat(Output.AGG, Output.VALUE) +
                this.getStat(Output.CORE, Output.VALUE);
    }

    public double getNetworkNumber() {
        return this.getStat(Output.EDGE, Output.NUMBER) +
                this.getStat(Output.AGG, Output.NUMBER) +
                this.getStat(Output.CORE, Output.NUMBER);
    }
    
}
