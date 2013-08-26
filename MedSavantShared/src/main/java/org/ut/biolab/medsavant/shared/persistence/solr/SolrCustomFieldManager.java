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
package org.ut.biolab.medsavant.shared.persistence.solr;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.persistence.CustomFieldManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implement adding custom fields for Solr.
 */
public class SolrCustomFieldManager implements CustomFieldManager {

    public HttpClient httpClient = new DefaultHttpClient();

    @Override
    public void addCustomField(CustomField customField) throws URISyntaxException, IOException {

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").
                setHost("localhost").
                setPort(8983).
                setPath("/solr/medsavant/schema/fields");
        URI uri = uriBuilder.build();
        HttpPost request = new HttpPost(uri);

        HttpEntity entity = new StringEntity(getJSONForCustomField(customField));
        request.setEntity(entity);
        request.addHeader("Content-Type", "application/json");

        httpClient.execute(request);
    }

    @Override
    public void addCustomFields(List<CustomField> customFieldList) throws URISyntaxException, IOException {
        for (CustomField customField : customFieldList) {
            addCustomField(customField);
        }
    }

    private String getJSONForCustomField(CustomField customField) {

        String name = customField.getColumnName();
        String type = customField.getColumnType().toString();

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode node = nodeFactory.objectNode();

        node.put(ClientUtils.escapeQueryChars("name"), ClientUtils.escapeQueryChars(name));
        node.put(ClientUtils.escapeQueryChars("type"), convertSQLTypeToSolrType(type));
        node.put(ClientUtils.escapeQueryChars("indexed"), ClientUtils.escapeQueryChars("true"));
        node.put(ClientUtils.escapeQueryChars("stored"), ClientUtils.escapeQueryChars("true"));

        return node.toString();
    }

    private String convertSQLTypeToSolrType(String type) {
        String solrType = type;
        if (TYPE_MAPPINGS.containsKey(type)) {
            solrType = TYPE_MAPPINGS.get(type);
        }
        return solrType;
    }

    private static final Map<String, String> TYPE_MAPPINGS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("varchar", "string");
        put("tinyint", "boolean");
        put("text", "string");  //for now
        put("blob", "binary");
    }}
    );
}
