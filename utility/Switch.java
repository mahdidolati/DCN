/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mahdi
 */
public class Switch {
    List<SwitchTraffic> sts;
    public Switch() {
        sts = new ArrayList<>();
    }
    public void add(SwitchTraffic st) {
        this.sts.add(st);
    }
    public List<SwitchTraffic> getSwitchTraffic() {
        return this.sts;
    }
    
    public boolean hasFree() {
        return (FatTree.getK()/2 - sts.size() > 0);
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
    
}
