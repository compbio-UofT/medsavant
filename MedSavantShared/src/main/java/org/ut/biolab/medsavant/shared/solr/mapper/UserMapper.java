package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.User;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handle the mapping of User objects.
 */
public class UserMapper implements ResultMapper<User> {

    @Override
    public List<User> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableUser> searcheableUserList = binder.getBeans(SearcheableUser.class, solrDocumentList);
        return toModelList(searcheableUserList);
    }

    @Override
    public List<User> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<User> toModelList(List<SearcheableUser> searcheableUserList) {
        List<User> userList = new ArrayList<User>();
        for (SearcheableUser searcheableUser : searcheableUserList) {
            userList.add(searcheableUser.getUser());
        }
        return userList;
    }
}
