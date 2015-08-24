/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import MyPackage.MyPack.JobMy;

/**
 *
 * @author mahdi
 */
public class SwitchTraffic {
    public double in;
    public double out;
    public JobMy j;
    public int superVm;
    
    public SwitchTraffic() {
        this.in = this.out = 0.0;
    }
    
    public void addSwTr(SwitchTraffic st) {
        this.in += st.in;
        this.out += st.out;
    }

    public SwitchTraffic getCopy() {
        SwitchTraffic st = new SwitchTraffic();
        st.in = st.in;
        st.out = st.out;
        return st;
    }
    
}
