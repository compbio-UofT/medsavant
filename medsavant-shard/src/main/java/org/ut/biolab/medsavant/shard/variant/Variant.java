/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.shard.variant;

import java.io.Serializable;

public class Variant implements Serializable {

    /**
     * Column representing ID in mapping.
     */
    public static final String id = "variant_id";
    private static final long serialVersionUID = 1L;
    private Integer upload_id;
    private Integer file_id;
    private Integer variant_id;
    private String dna_id;
    private String chrom;
    private Integer position;
    private String dbsnp_id;
    private String ref;
    private String alt;
    private Float qual;
    private String filter;
    private String variant_type;
    private String zygosity;
    private String gt;
    private String custom_info;
    private String aa;
    private Integer ac;
    private Float af;
    private Integer an;
    private Float bq;
    private String cigar;
    private Boolean db;
    private Integer dp;
    private Integer end;
    private Boolean h2;
    private Float mq;
    private Integer mq0;
    private Integer ns;
    private Float sb;
    private Boolean somatic;
    private Boolean validated;

    public Integer getUpload_id() {
        return upload_id;
    }

    public void setUpload_id(Integer upload_id) {
        this.upload_id = upload_id;
    }

    public Integer getFile_id() {
        return file_id;
    }

    public void setFile_id(Integer file_id) {
        this.file_id = file_id;
    }

    public Integer getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(Integer variant_id) {
        this.variant_id = variant_id;
    }

    public String getDna_id() {
        return dna_id;
    }

    public void setDna_id(String dna_id) {
        this.dna_id = dna_id;
    }

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getDbsnp_id() {
        return dbsnp_id;
    }

    public void setDbsnp_id(String dbsnp_id) {
        this.dbsnp_id = dbsnp_id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public Float getQual() {
        return qual;
    }

    public void setQual(Float qual) {
        this.qual = qual;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getVariant_type() {
        return variant_type;
    }

    public void setVariant_type(String variant_type) {
        this.variant_type = variant_type;
    }

    public String getZygosity() {
        return zygosity;
    }

    public void setZygosity(String zygosity) {
        this.zygosity = zygosity;
    }

    public String getGt() {
        return gt;
    }

    public void setGt(String gt) {
        this.gt = gt;
    }

    public String getCustom_info() {
        return custom_info;
    }

    public void setCustom_info(String custom_info) {
        this.custom_info = custom_info;
    }

    public String getAa() {
        return aa;
    }

    public void setAa(String aa) {
        this.aa = aa;
    }

    public Integer getAc() {
        return ac;
    }

    public void setAc(Integer ac) {
        this.ac = ac;
    }

    public Float getAf() {
        return af;
    }

    public void setAf(Float af) {
        this.af = af;
    }

    public Integer getAn() {
        return an;
    }

    public void setAn(Integer an) {
        this.an = an;
    }

    public Float getBq() {
        return bq;
    }

    public void setBq(Float bq) {
        this.bq = bq;
    }

    public String getCigar() {
        return cigar;
    }

    public void setCigar(String cigar) {
        this.cigar = cigar;
    }

    public Boolean getDb() {
        return db;
    }

    public void setDb(Boolean db) {
        this.db = db;
    }

    public Integer getDp() {
        return dp;
    }

    public void setDp(Integer dp) {
        this.dp = dp;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Boolean getH2() {
        return h2;
    }

    public void setH2(Boolean h2) {
        this.h2 = h2;
    }

    public Float getMq() {
        return mq;
    }

    public void setMq(Float mq) {
        this.mq = mq;
    }

    public Integer getMq0() {
        return mq0;
    }

    public void setMq0(Integer mq0) {
        this.mq0 = mq0;
    }

    public Integer getNs() {
        return ns;
    }

    public void setNs(Integer ns) {
        this.ns = ns;
    }

    public Float getSb() {
        return sb;
    }

    public void setSb(Float sb) {
        this.sb = sb;
    }

    public Boolean getSomatic() {
        return somatic;
    }

    public void setSomatic(Boolean somatic) {
        this.somatic = somatic;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((variant_id == null) ? 0 : variant_id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Variant other = (Variant) obj;
        if (variant_id == null) {
            if (other.variant_id != null)
                return false;
        } else if (!variant_id.equals(other.variant_id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Variant [upload_id=" + upload_id + ", file_id=" + file_id + ", variant_id=" + variant_id + ", dna_id=" + dna_id + ", chrom=" + chrom + ", position=" + position
                + ", dbsnp_id=" + dbsnp_id + ", ref=" + ref + ", alt=" + alt + ", qual=" + qual + ", filter=" + filter + ", variant_type=" + variant_type + ", zygosity="
                + zygosity + ", gt=" + gt + ", custom_info=" + custom_info + ", aa=" + aa + ", ac=" + ac + ", af=" + af + ", an=" + an + ", bq=" + bq + ", cigar=" + cigar
                + ", db=" + db + ", dp=" + dp + ", end=" + end + ", h2=" + h2 + ", mq=" + mq + ", mq0=" + mq0 + ", ns=" + ns + ", sb=" + sb + ", somatic=" + somatic
                + ", validated=" + validated + "]";
    }

}
