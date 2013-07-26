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
package org.ut.biolab.medsavant.server.db.variants;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import com.healthmarketscience.sqlbuilder.SelectQuery;

/**
 * Helper class for VariantManager.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantManagerHelper implements Serializable {
    private static final long serialVersionUID = 5329416217138693373L;

    public int getNumFilteredVariants(String sessID, SelectQuery q) throws SQLException, RemoteException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());
        rs.next();

        return rs.getInt(1);
    }
}
