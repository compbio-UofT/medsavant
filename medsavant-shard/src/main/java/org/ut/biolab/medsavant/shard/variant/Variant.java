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

/**
 * Variant representation.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class Variant implements Serializable {

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

    public Variant() {
        super();
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aa == null) ? 0 : aa.hashCode());
        result = prime * result + ((ac == null) ? 0 : ac.hashCode());
        result = prime * result + ((af == null) ? 0 : af.hashCode());
        result = prime * result + ((alt == null) ? 0 : alt.hashCode());
        result = prime * result + ((an == null) ? 0 : an.hashCode());
        result = prime * result + ((bq == null) ? 0 : bq.hashCode());
        result = prime * result + ((chrom == null) ? 0 : chrom.hashCode());
        result = prime * result + ((cigar == null) ? 0 : cigar.hashCode());
        result = prime * result + ((custom_info == null) ? 0 : custom_info.hashCode());
        result = prime * result + ((db == null) ? 0 : db.hashCode());
        result = prime * result + ((dbsnp_id == null) ? 0 : dbsnp_id.hashCode());
        result = prime * result + ((dna_id == null) ? 0 : dna_id.hashCode());
        result = prime * result + ((dp == null) ? 0 : dp.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((file_id == null) ? 0 : file_id.hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        result = prime * result + ((gt == null) ? 0 : gt.hashCode());
        result = prime * result + ((h2 == null) ? 0 : h2.hashCode());
        result = prime * result + ((mq == null) ? 0 : mq.hashCode());
        result = prime * result + ((mq0 == null) ? 0 : mq0.hashCode());
        result = prime * result + ((ns == null) ? 0 : ns.hashCode());
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + ((qual == null) ? 0 : qual.hashCode());
        result = prime * result + ((ref == null) ? 0 : ref.hashCode());
        result = prime * result + ((sb == null) ? 0 : sb.hashCode());
        result = prime * result + ((somatic == null) ? 0 : somatic.hashCode());
        result = prime * result + ((upload_id == null) ? 0 : upload_id.hashCode());
        result = prime * result + ((validated == null) ? 0 : validated.hashCode());
        result = prime * result + ((variant_id == null) ? 0 : variant_id.hashCode());
        result = prime * result + ((variant_type == null) ? 0 : variant_type.hashCode());
        result = prime * result + ((zygosity == null) ? 0 : zygosity.hashCode());
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
        if (aa == null) {
            if (other.aa != null)
                return false;
        } else if (!aa.equals(other.aa))
            return false;
        if (ac == null) {
            if (other.ac != null)
                return false;
        } else if (!ac.equals(other.ac))
            return false;
        if (af == null) {
            if (other.af != null)
                return false;
        } else if (!af.equals(other.af))
            return false;
        if (alt == null) {
            if (other.alt != null)
                return false;
        } else if (!alt.equals(other.alt))
            return false;
        if (an == null) {
            if (other.an != null)
                return false;
        } else if (!an.equals(other.an))
            return false;
        if (bq == null) {
            if (other.bq != null)
                return false;
        } else if (!bq.equals(other.bq))
            return false;
        if (chrom == null) {
            if (other.chrom != null)
                return false;
        } else if (!chrom.equals(other.chrom))
            return false;
        if (cigar == null) {
            if (other.cigar != null)
                return false;
        } else if (!cigar.equals(other.cigar))
            return false;
        if (custom_info == null) {
            if (other.custom_info != null)
                return false;
        } else if (!custom_info.equals(other.custom_info))
            return false;
        if (db == null) {
            if (other.db != null)
                return false;
        } else if (!db.equals(other.db))
            return false;
        if (dbsnp_id == null) {
            if (other.dbsnp_id != null)
                return false;
        } else if (!dbsnp_id.equals(other.dbsnp_id))
            return false;
        if (dna_id == null) {
            if (other.dna_id != null)
                return false;
        } else if (!dna_id.equals(other.dna_id))
            return false;
        if (dp == null) {
            if (other.dp != null)
                return false;
        } else if (!dp.equals(other.dp))
            return false;
        if (end == null) {
            if (other.end != null)
                return false;
        } else if (!end.equals(other.end))
            return false;
        if (file_id == null) {
            if (other.file_id != null)
                return false;
        } else if (!file_id.equals(other.file_id))
            return false;
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        if (gt == null) {
            if (other.gt != null)
                return false;
        } else if (!gt.equals(other.gt))
            return false;
        if (h2 == null) {
            if (other.h2 != null)
                return false;
        } else if (!h2.equals(other.h2))
            return false;
        if (mq == null) {
            if (other.mq != null)
                return false;
        } else if (!mq.equals(other.mq))
            return false;
        if (mq0 == null) {
            if (other.mq0 != null)
                return false;
        } else if (!mq0.equals(other.mq0))
            return false;
        if (ns == null) {
            if (other.ns != null)
                return false;
        } else if (!ns.equals(other.ns))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (qual == null) {
            if (other.qual != null)
                return false;
        } else if (!qual.equals(other.qual))
            return false;
        if (ref == null) {
            if (other.ref != null)
                return false;
        } else if (!ref.equals(other.ref))
            return false;
        if (sb == null) {
            if (other.sb != null)
                return false;
        } else if (!sb.equals(other.sb))
            return false;
        if (somatic == null) {
            if (other.somatic != null)
                return false;
        } else if (!somatic.equals(other.somatic))
            return false;
        if (upload_id == null) {
            if (other.upload_id != null)
                return false;
        } else if (!upload_id.equals(other.upload_id))
            return false;
        if (validated == null) {
            if (other.validated != null)
                return false;
        } else if (!validated.equals(other.validated))
            return false;
        if (variant_id == null) {
            if (other.variant_id != null)
                return false;
        } else if (!variant_id.equals(other.variant_id))
            return false;
        if (variant_type == null) {
            if (other.variant_type != null)
                return false;
        } else if (!variant_type.equals(other.variant_type))
            return false;
        if (zygosity == null) {
            if (other.zygosity != null)
                return false;
        } else if (!zygosity.equals(other.zygosity))
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
