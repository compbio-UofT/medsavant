/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.fishersexact;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class FishersTest {
    
    
    /**
     * | A B |
     * | C D |
     * @return Result of 2-Tail Fisher's Exact Test
     */
    public static BigDecimal fishersExact(int a, int b, int c, int d){
        
        resetFactMap();
        
        
        // 1. Compute cutoff
        
        BigDecimal baseBig = new BigDecimal(factBig(a).multiply(factBig(b)).multiply(factBig(c)).multiply(factBig(d)));
        BigDecimal constantBig = (new BigDecimal(factBig(a+b).multiply(factBig(c+d).multiply(factBig(a+c).multiply(factBig(b+d)))))).divide(new BigDecimal(factBig(a+b+c+d)), 100, BigDecimal.ROUND_DOWN);
        
        // 2a. Variations base (minimizing a)
                
        int a1 = 0, b1 = a+b, c1 = a+c, d1 = d-a;
        if(d1 < 0){
            b1 += d1;
            c1 += d1;
            d1 = 0;
            a1 = (a+b)-b1;
        }
        
        // 2b. Variations base (minimizing b)
        
        int a2 = a+b, b2 = 0, c2 = c-b, d2 = b+d;
        if(c2 < 0){
            a2 += c2;
            d2 += c2;
            c2 = 0;
            b2 = (a+b)-a2;
        }    
        
        // 3. Compute variations
        
        BigDecimal p_value = new BigDecimal("0");
        int iterations = 0; //TODO: remove these, just for benchmarking
        int iterations1 = Math.min(b1, c1) + 1;
        
        // 3a. Compute variations (increasing a1 -> a)

        while(a1 < a){
            iterations++;
            BigDecimal base1Big = new BigDecimal(factBig(a1).multiply(factBig(b1)).multiply(factBig(c1)).multiply(factBig(d1)));
            if(base1Big.compareTo(baseBig) >= 0){  // equivalent to p_value <= p_cutoff                
                p_value = p_value.add(constantBig.divide(base1Big, 1000, BigDecimal.ROUND_HALF_UP));
            } else {               
                break;
            }  
            a1++;
            b1--;
            c1--;
            d1++;
        }
        
        // 3b. Compute variations (decreasing a2 -> a+1)
        
        while(a2 > a){
            iterations++;
            BigDecimal base1Big = new BigDecimal(factBig(a2).multiply(factBig(b2)).multiply(factBig(c2)).multiply(factBig(d2)));
            if(base1Big.compareTo(baseBig) >= 0){  // equivalent to p_value <= p_cutoff                
                p_value = p_value.add(constantBig.divide(base1Big, 1000, BigDecimal.ROUND_HALF_UP));
            } else {              
                break;
            }  
            a2--;
            b2++;
            c2++;
            d2--;
        }

        // 3c. Add value for actual a,b,c,d
        
        p_value = p_value.add(constantBig.divide(new BigDecimal(factBig(a).multiply(factBig(b)).multiply(factBig(c)).multiply(factBig(d))), 1000, BigDecimal.ROUND_HALF_UP)); 
        
        
        
        System.out.println("old iterations = " + iterations1);
        System.out.println("new iterations = " + iterations);
                
        
        //TODO: clear factMap to free memory
        //resetFactMap();
        return p_value;
    }
    
    private static double fact(int x){
        double result = 1;
        while(x > 1) result *= x--;
        return result;
    }
    
    private static void resetFactMap(){
        factMap = new HashMap<Integer, BigInteger>();
        factMap.put(0, BigInteger.ONE);
        highestMappedValue = 0;
    }
    
    private static Map<Integer, BigInteger> factMap;
    private static int highestMappedValue = 0;
    
    private static BigInteger factBig(int x){
        
        if(highestMappedValue >= x){
            return factMap.get(x);
        } 
        
        
        int i = highestMappedValue;
        while(i < x){
            i++;
            factMap.put(i, factMap.get(i-1).multiply(new BigInteger(Integer.toString(i))));
        }
        highestMappedValue = x;
        return factMap.get(x);         
    }
    
    /*private static BigInteger factBig(int x){
        BigInteger result = new BigInteger("1");
        while(x > 1) result = result.multiply(new BigInteger(Integer.toString(x--)));
        return result;
    }*/
    
}
