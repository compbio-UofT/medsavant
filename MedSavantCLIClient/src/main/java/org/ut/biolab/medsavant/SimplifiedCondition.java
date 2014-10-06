/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.ut.biolab.medsavant;

import com.google.gson.JsonParseException;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;

 public class SimplifiedCondition {
        private static final Log LOG = LogFactory.getLog(SimplifiedCondition.class);
        
        private int projectId;
        private int refId;
        private String type;
        private String method;
        private String[] args;

        public Condition getCondition(String sessionId, VariantManagerAdapter vm) throws JsonParseException {
            try {
                if (this.args.length < 1) {
                    throw new JsonParseException("No arguments given for SimplifiedCondition with type " + this.type
                            + " and method " + this.method);
                }
               
                TableSchema tableSchema = vm.getCustomTableSchema(sessionId, this.projectId, this.refId);

                DbColumn col = tableSchema.getDBColumn(this.args[0]);
                if (this.type.equals("BinaryCondition")) {
                    if (this.method.equals("lessThan")) {
                        return BinaryCondition.lessThan(col, this.args[1], Boolean.parseBoolean(this.args[2]));
                    } else if (this.method.equals("greaterThan")) {
                        return BinaryCondition.greaterThan(col, this.args[1], Boolean.parseBoolean(this.args[2]));
                    } else if (this.method.equals("equalTo")) {
                        return BinaryCondition.equalTo(col, this.args[1]);
                    } else if (this.method.equals("notEqualTo")) {
                        return BinaryCondition.notEqualTo(col, this.args[1]);
                    } else if (this.method.equals("like")) {
                        return BinaryCondition.like(col, this.args[1]);
                    } else if (this.method.equals("notLike")) {
                        return BinaryCondition.notLike(col, this.args[1]);
                    } else if (this.method.equals("iLike")) {
                        return BinaryCondition.iLike(col, this.args[1]);
                    } else if (this.method.equals("notiLike")) {
                        return BinaryCondition.notiLike(col, this.args[1]);
                    }
                    throw new JsonParseException("Unrecognized method " + this.method + " for simplified condition "
                            + this.type);
                } else if (this.type.equals("UnaryCondition")) {
                    if (this.method.equals("isNull")) {
                        return UnaryCondition.isNull(col);
                    } else if (this.method.equals("isNotNull")) {
                        return UnaryCondition.isNotNull(col);
                    } else if (this.method.equals("exists")) {
                        return UnaryCondition.exists(col);
                    } else if (this.method.equals("unique")) {
                        return UnaryCondition.unique(col);
                    }
                    throw new JsonParseException("Unrecognized method " + this.method + " for simplified condition "
                            + this.type);
                }
                throw new JsonParseException("Unrecognized simplified condition type " + this.type);
            } catch (ArrayIndexOutOfBoundsException ai) {
                throw new JsonParseException("Invalid arguments specified for SimplifiedCondition of type" + this.type
                        + ", method " + this.method + ", and args=" + this.args);
            } catch (SQLException ex) {
                throw new JsonParseException("Couldn't fetch variant table schema: " + ex);
            } catch (RemoteException re) {
                throw new JsonParseException("Couldn't fetch variant table schema: " + re);
            } catch (SessionExpiredException se) {
                throw new JsonParseException("Couldn't fetch variant table schema: " + se);
            }
        }
    } 