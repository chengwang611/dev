package com.apress.demo.dao;
/*
 * Created by hakdogan on 01/12/2017
 */

import com.apress.demo.config.ConfigProps;
import com.apress.demo.entities.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class QueryDAO {

    private final RestHighLevelClient client;
    private final SearchSourceBuilder sourceBuilder;
    private final ConfigProps props;
    private final Gson gson;

    @Autowired
    public QueryDAO(RestHighLevelClient client, SearchSourceBuilder sourceBuilder,
                    ConfigProps props, Gson gson){
        this.client = client;
        this.sourceBuilder = sourceBuilder;
        this.props = props;
        this.gson = gson;
    }

    /**
     *
     * @param document
     * @return
     */
    public String createIndex(Document document){

        try {
            IndexRequest request = new IndexRequest(props.getIndex().getName(), props.getIndex().getType());
            request.source(gson.toJson(document), XContentType.JSON);
            IndexResponse response = client.index(request);
            return response.getId();
        } catch (Exception ex) {
//            log.error("The exception was thrown in createIndex method. {} ", ex);
        }

        return null;
    }

    /**
     *
     * @param document
     * @return
     */
    public String updateDocument(Document document){

        try {
            UpdateRequest request = new UpdateRequest(props.getIndex().getName(),
                    props.getIndex().getType(), document.getId())
                    .doc(gson.toJson(document), XContentType.JSON);

            UpdateResponse response = client.update(request);
            return response.getId();
        } catch (Exception ex){
            //log.error("The exception was thrown in updateDocument method. {} ", ex);
        }

        return null;
    }

    /**
     *
     * @return
     */
    public List<Document> matchAllQuery() {

        List<Document> result = new ArrayList<>();

        try {
            flush();
            result = getDocuments(QueryBuilders.matchAllQuery());
        } catch (Exception ex){
           // log.error("The exception was thrown in matchAllQuery method. {} ", ex);
        }

        return result;
    }

    /**
     *
     * @param query
     * @return
     */
    public List<Document> wildcardQuery(String query){

        List<Document> result = new ArrayList<>();

        try {
            result = getDocuments(QueryBuilders.queryStringQuery("*" + query.toLowerCase() + "*"));
        } catch (Exception ex){
            //log.error("The exception was thrown in wildcardQuery method. {} ", ex);
        }

        return result;
    }
    
    /**
    *
    * @param query
    * @return
    */
   public List<JsonNode> wildcardQueryStr(String query){

       List<JsonNode> result = new ArrayList<>();

       try {
           result = getDocumentsStr(QueryBuilders.queryStringQuery("*" + query.toLowerCase() + "*"));
       } catch (Exception ex){
           //log.error("The exception was thrown in wildcardQuery method. {} ", ex);
       }

       return result;
   }

   
   /**
   *
   * @param query
   * @return
   */
  public List<JsonNode> wildcardQueryFields(String query,String[] indexs,String[] includeFields,String[] excludeFields,int from, int size){

      List<JsonNode> result = new ArrayList<>();

      try {
    	  
    	  
    	  
          result = getDocumentsStr(QueryBuilders.queryStringQuery("*" + query.toLowerCase() + "*"),indexs,includeFields,excludeFields,from,size);
      } catch (Exception ex){
          //log.error("The exception was thrown in wildcardQuery method. {} ", ex);
      }

      return result;
  }
   
   
   
   

    /**
     *
     * @param id
     * @throws IOException
     */
    public void deleteDocument(String id){
        try {
            DeleteRequest deleteRequest = new DeleteRequest(props.getIndex().getName(), props.getIndex().getType(), id);
            client.delete(deleteRequest);
        } catch (Exception ex){
            //log.error("The exception was thrown in deleteDocument method. {} ", ex);
        }
    }

    /**
     *
     * @return
     */
    private SearchRequest getSearchRequest(){
        //SearchRequest searchRequest = new SearchRequest(props.getIndex().getName());
    	String[] indexs=props.getIndex().getName().split(",");
        SearchRequest searchRequest = new SearchRequest(indexs);
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }
    
    /**
    *
    * @return
    */
   private SearchRequest getSearchRequest(String[] indexs){
       //SearchRequest searchRequest = new SearchRequest(props.getIndex().getName());
   	
       SearchRequest searchRequest = new SearchRequest(indexs);
       searchRequest.source(sourceBuilder);
       return searchRequest;
   }
    

    /**
     *
     * @param builder
     * @return
     * @throws IOException
     */
    private List<Document> getDocuments(AbstractQueryBuilder builder) throws IOException {
        List<Document> result = new ArrayList<>();

        sourceBuilder.query(builder);
        SearchRequest searchRequest = getSearchRequest();

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Document doc = gson.fromJson(hit.getSourceAsString(), Document.class);
            doc.setId(hit.getId());
            result.add(doc);
        }

        return result;
    }
    /**
    *
    * @param builder
    * @return
    * @throws IOException
    */
	private List<JsonNode> getDocumentsStr(AbstractQueryBuilder builder)
			throws IOException {
		List<JsonNode> result = new ArrayList<>();

		sourceBuilder.query(builder);
		SearchRequest searchRequest = getSearchRequest();

		SearchResponse searchResponse = client.search(searchRequest);
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(hit.getSourceAsString());

			result.add(actualObj);

		}

		return result;
	}
	
    /**
    *
    * @param builder
    * @return
    * @throws IOException
    */
	private List<JsonNode> getDocumentsStr(AbstractQueryBuilder builder,String[] indexs,String[] includeFields ,String[] excludeFields,int from,int size)
			throws IOException {
		List<JsonNode> result = new ArrayList<>();

		sourceBuilder.query(builder);
		sourceBuilder.fetchSource(includeFields, excludeFields);
		sourceBuilder.from(from);
		sourceBuilder.size(size);
		
		SearchRequest searchRequest = getSearchRequest(indexs);

		SearchResponse searchResponse = client.search(searchRequest);
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(hit.getSourceAsString());

			result.add(actualObj);

		}

		return result;
	}
	
	
	

    public void flush() throws IOException {
        String endPoint = String.join("/", props.getIndex().getName(), "_flush");
        client.getLowLevelClient().performRequest("POST", endPoint);
    }
    
    
	public static SearchResponse doTemplate() {

		String[] indices = { "ga_day_index-0907-2" };

		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("192.168.0.171", 9200, "http")));

		SearchTemplateRequest request = new SearchTemplateRequest();
		request.setRequest(new SearchRequest("ga_day_index-0907-2"));

		request.setScriptType(ScriptType.INLINE);
		request.setScript("{\n" + "    \"query\": {\n" + "        \"match\" : {\n"
				+ "            \"device.browser\" : {\n" + "                \"query\" : \"*GoogleAnalysicst*\",\n"
				+ "                \"fuzziness\": \"AUTO\"\n" + "            }\n" + "        }\n" + "    }\n" + "}");

		SearchResponse response = null;
		try {
			response = client.search(request.getRequest());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}
    
}
