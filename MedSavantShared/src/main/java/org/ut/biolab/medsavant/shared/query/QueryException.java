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
package org.ut.biolab.medsavant.shared.query;

/**
 *  Provide exception handling for query exceptions
 */
public class QueryException extends Exception {


    private Query query;

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Query query, Throwable throwable) {
        super(message, throwable);
        this.query = query;
    }

    public String getMesage() {
        return String.format("%s\n%s", super.getMessage(), query.toString());
    }


}
