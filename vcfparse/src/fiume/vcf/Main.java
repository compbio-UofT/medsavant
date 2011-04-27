/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fiume.vcf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            VariantSet variants = VCFParser.parseVariants(new File("C:\\calls.vcf"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
