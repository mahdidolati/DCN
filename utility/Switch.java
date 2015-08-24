/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import MyPackage.MyPack.Server;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mahdi
 */
public class Switch {
    List<SwitchTraffic> sts;
    List<Server> servers;
    Map<Server,SwitchTraffic> srvToSt;
    public Switch() {
        sts = new ArrayList<>();
        servers = new ArrayList<>();
        srvToSt = new HashMap<>();
    }
    public void add(SwitchTraffic st) {
        this.sts.add(st);
    }
    public void add(SwitchTraffic st, Server srv, double maxLoad) {
        if(FatTree.getK()/2 - sts.size() > 0) {
            this.sts.add(st);
            this.servers.add(srv);
            this.srvToSt.put(srv, st);
        }else{
            for(Server server : this.servers) {
                if(server.curLoad+srv.curLoad <= maxLoad) {
                    server.merge(srv);
                    server.curLoad += srv.curLoad;
                    this.srvToSt.get(server).in += st.in;
                    this.srvToSt.get(server).out += st.out;
                    break;
                }
            }
        }
    }
    public boolean hasDedicatePlace() {
        return this.hasFree();
    }
    public List<SwitchTraffic> getSwitchTraffic() {
        return this.sts;
    }
    
    public boolean hasFree() {
        return (FatTree.getK()/2 - sts.size() > 0);
    }
    
    public boolean hasFree(double load, double maxLoad) {
        if(FatTree.getK()/2 - sts.size() > 0) {
            return true;
        }
        for(Server server : this.servers) {
            if(server.curLoad+load <= maxLoad) 
                return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        String ret = "";
        double allIn = 0.0;
        double allOut = 0.0;
        for(SwitchTraffic st : this.sts) {
            ret = ret + st.in + ", " + st.out + ", ";
            allIn += st.in;
            allOut +=st.out;
        }
        ret = "" + sts.size() + ", " + allIn + ", " + allOut + ", " + ret;
        return ret;
    }

    public List<SwitchTraffic> getSts() {
        return sts;
    }

    public void setSts(List<SwitchTraffic> sts) {
        this.sts = sts;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }
    
    public void addServer(Server srv) {
        if(this.servers.contains(srv) == false)
            this.servers.add(srv);
    }

    public Switch getCopy() {
        Switch nsw = new Switch();
        for(SwitchTraffic st : this.getSts()) {
            nsw.sts.add(st.getCopy());
            for(Server srv : this.servers) {
                nsw.servers.add(srv.getCopy());
            }
        }
        return nsw;
    }

    public Map<Server, SwitchTraffic> getSrvToSt() {
        return srvToSt;
    }

    public void setSrvToSt(Map<Server, SwitchTraffic> srvToSt) {
        this.srvToSt = srvToSt;
    }

    
    
}
