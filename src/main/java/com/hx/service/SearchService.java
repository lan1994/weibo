package com.hx.service;

import com.hx.model.Question;
import com.hx.model.User;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class SearchService {
    private static final String SOLR_URL = "http://127.0.0.1:8983/solr/";
    private HttpSolrClient weiboClient = new HttpSolrClient.Builder(SOLR_URL+"weibo").build();
    private HttpSolrClient userClient = new HttpSolrClient.Builder(SOLR_URL+"user").build();
    private static final String QUESTION_TITLE_FIELD = "question_title";
    private static final String QUESTION_CONTENT_FIELD = "question_content";
    private static final String USER_NAME_FIELD = "user_name";
    private static final String USER_HEAD_URL_FIELD = "user_head_url";
    public List<Question> searchQuestion(String keyword, int offset, int count,
                                         String hlPre, String hlPos) throws Exception {
        List<Question> questionList = new ArrayList<>();
        SolrQuery query = new SolrQuery(keyword);
        query.setRows(count);
        query.setStart(offset);
        query.setHighlight(true);
        query.setHighlightSimplePre(hlPre);
        query.setHighlightSimplePost(hlPos);
        query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);
        QueryResponse response = weiboClient.query(query);
        for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
            Question q = new Question();
            q.setId(Integer.parseInt(entry.getKey()));
            if (entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
                List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
                if (contentList.size() > 0) {
                    q.setContent(contentList.get(0));
                }
            }
            if (entry.getValue().containsKey(QUESTION_TITLE_FIELD)) {
                List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
                if (titleList.size() > 0) {
                    q.setTitle(titleList.get(0));
                }
            }
            questionList.add(q);
        }
        return questionList;
    }

    public boolean indexQuestion(int qid, String title, String content) throws Exception {
        SolrInputDocument doc =  new SolrInputDocument();
        doc.setField("id", qid);
        doc.setField(QUESTION_TITLE_FIELD, title);
        doc.setField(QUESTION_CONTENT_FIELD, content);
        UpdateResponse response = weiboClient.add(doc, 1000);
        return response != null && response.getStatus() == 0;
    }

    public boolean indexUser(int uid, String userName, String headUrl) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", uid);
        doc.setField(USER_NAME_FIELD, userName);
        doc.setField(USER_HEAD_URL_FIELD, headUrl);
        UpdateResponse response = userClient.add(doc, 1000);
        return response != null && response.getStatus() == 0;
    }

    public List<User> searchUser(String keyWord, int offset, int count,
                                 String hlPre, String hlPos) throws Exception {
        List<User> userList = new ArrayList<>();
        SolrQuery query = new SolrQuery();
        query.setQuery(keyWord);
        query.setStart(offset);
        query.setRows(count);
        query.setHighlight(true);
        query.setHighlightSimplePre(hlPre);
        query.setHighlightSimplePost(hlPos);
        query.set("hl.fl", USER_NAME_FIELD + "," + USER_HEAD_URL_FIELD );
        QueryResponse response = userClient.query(query);
        SolrDocumentList docList = response.getResults();
        for(int i=0;i<docList.size();i++){
            SolrDocument doc = docList.get(i);
            List<String> userNameList = (List) doc.get(USER_NAME_FIELD);
            List<String> userHeadUrlList = (List) doc.get(USER_HEAD_URL_FIELD);
            String id = (String)doc.get("id");
            User u = new User();
            u.setId(Integer.parseInt(id));
            u.setName(userNameList.get(0));
            u.setHeadUrl(userHeadUrlList.get(0));
            userList.add(u);
        }
        return userList;
    }
}
