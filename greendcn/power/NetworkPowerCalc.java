/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn.power;

/**
 *
 * @author mahdi
 */
public class NetworkPowerCalc {
    
    private DevicePowerUsageModel e,a,c,l;
    
    public NetworkPowerCalc(DevicePowerUsageModel edgePow, DevicePowerUsageModel aggPow, 
            DevicePowerUsageModel corePow, DevicePowerUsageModel linkPow) {
        this.e = edgePow;
        this.a = aggPow;
        this.c = corePow;
        this.l = linkPow;
    }
    
    public double getConsumptionProbabilityBased(int n, double totalWork, double eCpu, 
            double eBw, double pr, double islandSize, int k) {
        double serverCap = totalWork/n;
        double vmsOnServer = serverCap/eCpu;
        double hostEdgeLink = vmsOnServer*(islandSize-vmsOnServer)*pr*eBw;
        double edgeOut = ((k/2*vmsOnServer)%(islandSize))*(islandSize-((k/2*vmsOnServer)%(islandSize)))*pr*eBw;
        double podOut = ((k*k/4*vmsOnServer)%(islandSize))*(islandSize-((k*k/4*vmsOnServer)%(islandSize)))*pr*eBw;
        
        double edgeFabric = k/2*hostEdgeLink+edgeOut;
        double aggFabric = edgeOut+2*podOut/k;
        double coreFabric = 4*n/(k*k)*4*podOut/(k*k);
        
        double fabricsUsage = 2*n/k*e.getConsumption(edgeFabric)
                + 2*n/k*a.getConsumption(aggFabric)
                + (k*k/4)*c.getConsumption(coreFabric);
        
        double linksUsage = n*l.getConsumption(hostEdgeLink)
                + n*l.getConsumption(2*edgeOut/k) 
                + l.getConsumption(4*podOut/(k*k));
        
        return fabricsUsage+linksUsage;
    }
    
    public double getConsumption(int n, double totalWork, double serverCap, double hTOe,double eTOa, double aTOc, int k) {
        double edgeX = 2*n/k*e.getConsumption((k/2)*(hTOe+eTOa));
        double aggX = 2*n/k*a.getConsumption((k/2)*(eTOa+aTOc));
        double coreX = k*c.getConsumption(k*aTOc);
        //
        double hToEdL = n*l.getConsumption(hTOe);
        double eToAgL = n*l.getConsumption(eTOa);
        double aToCoL = 4*n/(k*k)*k*l.getConsumption(aTOc);
        //
        double link = hToEdL+eToAgL+aToCoL;
        double proc = edgeX + aggX + coreX;
        return (link+proc);
//        return 0.0;
    }
    
}
