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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.ut.biolab.medsavant.shard.common.EntityStyle;

import com.healthmarketscience.sqlbuilder.SelectQuery;

/**
 * Sharded version of the helper class for VariantManager.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedVariantManagerHelper implements Serializable {

    private static final long serialVersionUID = 6242924081283721254L;
    private static final String[] INT_FIELDS = new String[] { "upload_id", "file_id", "variant_id", "position", "ac", "an", "dp", "end", "mq0", "ns" };
    private static final String[] FLOAT_FIELDS = new String[] { "qual", "af", "bq", "mq", "sb" };
    private static final String[] BOOLEAN_FIELDS = new String[] { "db", "h2", "somatic", "validated" };
    private static final String[] STRING_FIELDS = new String[] { "dna_id", "chrom", "dbsnp_id", "ref", "alt", "filter", "variant_type", "zygosity", "gt", "custom_info", "aa",
            "cigar" };

    private boolean equalsAny(String pattern, String[] match) {
        for (String s : match) {
            if (pattern.equals(s)) {
                return true;
            }
        }

        return false;
    }

    private Object castToItsType(String att, String value) {
        Object res = null;
        if (equalsAny(att, INT_FIELDS)) {
            res = Integer.valueOf(value);
        } else if (equalsAny(att, BOOLEAN_FIELDS)) {
            res = Boolean.valueOf(value);
        } else if (equalsAny(att, FLOAT_FIELDS)) {
            res = Float.valueOf(value);
        } else {
            res = value;
        }

        return res;
    }

    private Object[] getAttributeValues(Variant v) {
        // use reflection to get the list
        String atts = ReflectionToStringBuilder.toString(v, EntityStyle.getInstance());

        // parse
        atts = atts.substring(1, atts.length() - 1);
        String[] pairs = atts.split(",");
        Object[] os = new Object[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            String[] pieces = pairs[i].split("=", 2);
            os[i] = castToItsType(pieces[0], pieces[1]);
        }

        return os;
    }

    public Integer getNumFilteredVariants(String sessID, SelectQuery q) {
        // TODO: deal with table-project mapping, hibernate configuration has to
        // be provided dynamically
        Session s = ShardedConnectionController.openSession();

        Criteria c = s.createCriteria(Variant.class).setProjection(Projections.count(VariantMapping.getIdColumn()));
        c.add(Restrictions.sqlRestriction(q.getWhereClause().toString()));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();

        ShardedConnectionController.closeSession(s);
        return (res == null ? 0 : res);
    }

    public List<Object[]> getVariants(String sessID, SelectQuery q, int start, int limit, String[] orderByCols) {
        Session session = ShardedConnectionController.openSession();
        Criteria c = session.createCriteria(Variant.class).setFetchSize(limit).setMaxResults(limit).setFirstResult(start);
        c.add(Restrictions.sqlRestriction(q.getWhereClause().toString()));
        List<Variant> variantList = c.list();

        // convert to rows the editor expects
        int numberColumns = VariantMapping.getColumnCount();
        List<Object[]> res = new ArrayList<Object[]>();
        Iterator<Variant> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            res.add(getAttributeValues((Variant) iterator.next()));
        }

        ShardedConnectionController.closeSession(session);

        return res;
    }
}
