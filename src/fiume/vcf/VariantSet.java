/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fiume.vcf;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class VariantSet {

    private final ArrayList<VCFProperty> properties;
    protected final ArrayList<VariantRecord> records;
    private VCFHeader header;

    public VariantSet() {
        properties = new ArrayList<VCFProperty>();
        records = new ArrayList<VariantRecord>();
    }

    void addProperty(String string, Object o) {
//      System.out.println("Adding property " + string + " with value " + o);
        properties.add(new VCFProperty(string,o));
    }

    void setHeader(VCFHeader h) {
        this.header = h;
    }

    void addRecords(List<VariantRecord> rs) {
        records.addAll(rs);
    }

    public List<VariantRecord> getRecords() {
        return records;
    }

    public VCFHeader getHeader() {
        return header;
    }


    

}
