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
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import com.healthmarketscience.sqlbuilder.SelectQuery;

/**
 * Sharded version of the helper class for VariantManager.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedVariantManagerHelper implements Serializable {

    private static final long serialVersionUID = 6242924081283721254L;

    public Integer getNumFilteredVariants(String sessID, SelectQuery q) {
        // TODO: deal with table-project mapping, hibernate configuration has to
        // be provided dynamically
        Session s = ShardedConnectionController.openSession();

        Criteria c = s.createCriteria(Variant.class).setProjection(Projections.count(Variant.id));
        c.add(Restrictions.sqlRestriction(q.getWhereClause().toString()));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();

        ShardedConnectionController.closeSession(s);
        return (res == null ? 0 : res);
    }

    public List<Object[]> getVariants(String sessID, SelectQuery q, int start, int limit, String[] orderByCols) {
        Session session = ShardedConnectionController.openSession();

        Criteria c = session.createCriteria(Variant.class);
        c.add(Restrictions.sqlRestriction(q.getWhereClause().toString()));
        List<Variant> variantList = c.list();

        ShardedConnectionController.closeSession(session);

        return null;
    }
}
