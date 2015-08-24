/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MyPackage.MyPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class Server {
    public Map<JobMy, Set<Integer>> hosted;
    double cap;
    public double curLoad;
    boolean canExceed;
    public double maxCap;
    public boolean deleted;
    public boolean hasSpace;
    public Server(double cap) {
        this();
        this.cap = cap;
    }
    
    public Server() {
        this.hosted = new HashMap<>();
        this.maxCap = 80.0;
        deleted = false;
        hasSpace = true;
    }

    ReturnTrCpu candidateFrom(JobMy j) {
        if(this.curLoad >= cap) {
            return new ReturnTrCpu();
        }
        Set<Integer> curHosting = new HashSet<>();
        if(this.hosted.containsKey(j))
            curHosting = this.hosted.get(j);
        
        ReturnTrCpu tt = j.getBestFor(curHosting, cap-curLoad, maxCap-curLoad, canExceed);
//        System.out.println("(0|0): " + tt.index);
        return tt;
    }

    public void addCandidate(ReturnTrCpu bestCandid) {
        if(this.hosted.containsKey(bestCandid.j) == false) {
            this.hosted.put(bestCandid.j, new HashSet<>());
        }
        this.hosted.get(bestCandid.j).add(bestCandid.index);
        curLoad += bestCandid.cpu;
    }

    public SwitchTraffic getInOut() {
        SwitchTraffic st = new SwitchTraffic();
        st.in = 0;
        st.out = 0;
        for(JobMy j : this.hosted.keySet()) {
            SwitchTraffic tst = j.getInOut(this.hosted.get(j));
            st.in += tst.in;
            st.out += tst.out;
        }
        return st;
    }
    
    public SwitchTraffic getInOutSuper() {
        SwitchTraffic st = new SwitchTraffic();
        st.in = 0;
        st.out = 0;
        for(JobMy j : this.hosted.keySet()) {
            SwitchTraffic tst = j.getInOutSuper(this.hosted.get(j));
            st.in += tst.in;
            st.out += tst.out;
        }
        return st;
    }
    
    public void merge(Server srv) {
        for(JobMy j : srv.hosted.keySet()) {
            if(this.hosted.containsKey(j)) {
                this.hosted.get(j).addAll(srv.hosted.get(j));
            }else{
                this.hosted.put(j, srv.hosted.get(j));
            }
//            System.out.println("Server merge: " + this.hosted.get(j));
        }  
    } 

    public boolean isConnected(Server server) {
        for(JobMy j : server.hosted.keySet()) {
            if(this.hosted.containsKey(j))
                return true;
        }
        return false;
    }

    public double getBetween(Server server) {
        double ret = 0.0;
        for(JobMy j : this.hosted.keySet()) {
            if(server.hosted.containsKey(j)) {
                for(int v1 : this.hosted.get(j)) {
                    for(int v2 : server.hosted.get(j)) {
                        ret += j.getSuperTrafficMatrix().get(v2).get(v1);
                        ret += j.getSuperTrafficMatrix().get(v1).get(v2);
                    }
                }
            }
        }
        return ret;
    }
    
    public double getBetweenCpu(Server server) {
        double ret = 0.0;
        if(server == this)
            return ret;
        for(JobMy j : this.hosted.keySet()) {
            if(server.hosted.containsKey(j)) {
                for(int v1 : this.hosted.get(j)) {
                    for(int v2 : server.hosted.get(j)) {
                        ret += j.getTrafficMatrix().get(v2).get(v1);
                        ret += j.getTrafficMatrix().get(v1).get(v2);
                    }
                }
            }
        }
        return ret;
    }
    
    public double getBetween(List<Server> servers) {
        double all = 0.0;
        for(Server server : servers) {
            if(server.deleted)
                continue;
            all += this.getBetweenCpu(server);
        }
        return all;
    }

    public Server getCopy() {
        Server s = new Server();
        for(JobMy j : this.hosted.keySet()) {
            Set<Integer> ll = new HashSet<>();
            for(int i : this.hosted.get(j)) {
                ll.add(i);
            }
            s.hosted.put(j, ll);
            s.curLoad = this.curLoad;
            s.maxCap = this.maxCap;
            s.canExceed = this.canExceed;
            s.cap = this.cap;
            s.hasSpace = this.hasSpace;
            s.deleted = this.deleted;
        }
        return s;
    }
    
}
