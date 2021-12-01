package scc.utils;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import scc.Env;
import scc.entities.Message;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;


public class Search {
    private static final String SEARCH_SERVICE_QUERY_KEY = Env.SEARCH_KEY;
    private static final String SEARCH_SERVICE_URL = Env.SEARCH_URL;
    private static final String SEARCH_SERVICE_INDEX_NAME = "cosmosdb-index";

    private final SearchClient searchClient;

    public Search() {
        this.searchClient = new SearchClientBuilder()
                .credential(new AzureKeyCredential(SEARCH_SERVICE_QUERY_KEY))
                .endpoint(SEARCH_SERVICE_URL).indexName(SEARCH_SERVICE_INDEX_NAME)
                .buildClient();
    }

    public Object[] searchMessages(String channel, String text) {
        try {

            SearchOptions options = new SearchOptions().setIncludeTotalCount(true).setFilter(String.format("channel eq '%s'", channel))
                    .setSelect("doc_id", "user", "text", "channel", "replyTo", "imageId").setSearchFields("text").setTop(15);

            SearchPagedIterable searchPagedIterable = this.searchClient.search(text, options, null);

            Log.d("Cognitive Search found results", String.valueOf(searchPagedIterable.getTotalCount()));

            List<Message> messages = new ArrayList<>(Math.toIntExact(searchPagedIterable.getTotalCount()));

            for (SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
                resultResponse.getValue().forEach(searchResult -> {
                    SearchDocument msgDoc = searchResult.getDocument(SearchDocument.class);
                    Message msg = new Message((String) msgDoc.get("doc_id"), (String) msgDoc.get("replyTo"), (String) msgDoc.get("channel"), (String) msgDoc.get("user"), (String) msgDoc.get("text"), (String) msgDoc.get("imageId"));
                    messages.add(msg);
                });
            }
            return messages.toArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}