/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.fishersexact;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Andrew
 */
public class FishersTest {
    
    // | A B |
    // | C D |
    public static BigDecimal fishersExact(int a, int b, int c, int d){

        // 1. Compute cutoff
        
        BigDecimal baseBig = new BigDecimal(factBig(a).multiply(factBig(b)).multiply(factBig(c)).multiply(factBig(d)));
        BigDecimal constantBig = (new BigDecimal(factBig(a+b).multiply(factBig(c+d).multiply(factBig(a+c).multiply(factBig(b+d)))))).divide(new BigDecimal(factBig(a+b+c+d)), 100, BigDecimal.ROUND_DOWN);
        //BigDecimal p_cutoffBig = constantBig.divide(baseBig, 100, BigDecimal.ROUND_DOWN);

        // 2. Variations base
                
        int a1 = 0, b1 = a+b, c1 = a+c, d1 = d-a;
        if(d1 < 0){
            b1 += d1;
            c1 += d1;
            d1 = 0;
            a1 = (a+b)-b1;
        }
        
        // 3. Compute variations
        
        BigDecimal p_value = new BigDecimal("0");
        int iterations = Math.min(b1, c1) + 1;
        for(int i = 0; i < iterations; i++){      
            BigDecimal base1Big = new BigDecimal(factBig(a1).multiply(factBig(b1)).multiply(factBig(c1)).multiply(factBig(d1)));
            if(base1Big.compareTo(baseBig) >= 0){  // equivalent to p_value <= p_cutoff                
                p_value = p_value.add(constantBig.divide(base1Big, 100, BigDecimal.ROUND_DOWN));
            }     
            a1++;
            b1--;
            c1--;
            d1++;
        }
        
        return p_value;
    }
    
    private static double fact(int x){
        double result = 1;
        while(x > 1) result *= x--;
        return result;
    }
    
    private static BigInteger factBig(int x){
        BigInteger result = new BigInteger("1");
        while(x > 1) result = result.multiply(new BigInteger(Integer.toString(x--)));
        return result;
    }
    
}
