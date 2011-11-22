/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.fishersexact;

import java.math.MathContext;

/**
 *
 * @author Andrew
 */
public class Main {
    
    public static void main(String args[]){
        
        System.out.println(FishersTest.fishersExact(1500, 500, 500, 1500).round(MathContext.DECIMAL32));
        
    }
    
}
