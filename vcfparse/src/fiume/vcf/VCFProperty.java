/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fiume.vcf;

/**
 *
 * @author mfiume
 */
public class VCFProperty {
    private final String key;
    private final Object value;

    public VCFProperty(String key, Object value) {
        this.key = key;
        this.value = value;
    }
}
