package com.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class ESMappingBiz {
	private Client client;
	private static String clusterName = "";
	private static String clusterIp = "";
	
	
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}

	public static void setClusterName(String name) {
		clusterName = name;
	}

	public static void setClusterIp(String ip) {
		clusterIp = ip;
	}
	
	/**
	 * 得到访问es的客户端,我们使用Transport Client
	 * @return
	 */
	public Client buildClient(){
		System.out.println(clusterName+"-------"+clusterIp);
		Map<String, String> settingMap = new HashMap<String, String>();
//		settingMap.put("node.client", "true");
		settingMap.put("cluster.name", clusterName);
//		settingMap.put("node.name", "es_node0");
		Settings settings = ImmutableSettings.settingsBuilder().put(settingMap).build();
		Client client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress(clusterIp, 9300));
        //.addTransportAddress(new InetSocketTransportAddress("192.168.1.135", 9300));
		return client;
	}
	
	/**
	 * 创建索引
	 * 预定义一个索引的mapping,使用mapping的好处是可以个性的设置某个字段等的属性
	 * @throws Exception 
	 */
	public void buildIndexSysDm() throws Exception{
		//在本例中主要得注意,ttl及timestamp如何用java ,这些字段的具体含义,请去到es官网查看
		CreateIndexRequestBuilder cib=client.admin().indices().prepareCreate("productindex1");
		XContentBuilder mapping = XContentFactory.jsonBuilder()
				.startObject()
					.startObject("we3r")//
						.startObject("_ttl")//有了这个设置,就等于在这个给索引的记录增加了失效时间,
											//ttl的使用地方如在分布式下,web系统用户登录状态的维护.
							.field("enabled",true)//默认的false的
							.field("default","5m")//默认的失效时间,d/h/m/s 即天/小时/分钟/秒
							.field("store","yes")
							.field("index","not_analyzed")
						.endObject()
						.startObject("_timestamp")//这个字段为时间戳字段.即你添加一条索引记录后,自动给该记录增加个时间字段(记录的创建时间),搜索中可以直接搜索该字段.
							.field("enabled",true)
							.field("store","no")
							.field("index","not_analyzed")
						.endObject()
						.startObject("properties")//properties下定义的title等等就是属于我们需要的自定义字段了,相当于数据库中的表字段 ,此处相当于创建数据库表
							.startObject("title").field("type", "string").field("store", "yes").endObject()    
							.startObject("description").field("type", "string").field("index", "not_analyzed").endObject()  
							.startObject("price").field("type", "double").endObject()  
							.startObject("onSale").field("type", "boolean").endObject()  
							.startObject("type").field("type", "integer").endObject()  
							.startObject("createDate").field("type", "date").field("format","YYYYMMddhhMMSS").endObject()
						.endObject()
					.endObject()
				.endObject();  
		cib.addMapping("prindextype", mapping);
		cib.execute().actionGet();
	}
	
	
	/**
	 * 该方法为增加索引记录
	 * @throws Exception
	 */
	public void es_add(JSONObject json) throws Exception{
		String index,type,id;
		JSONArray js_index = json.getJSONArray("index");
		JSONArray js_values = json.getJSONArray("values");
		Object[] p_index = js_index.toArray();
		Object[] values = js_values.toArray();
		
		//批量插入
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for(int i=0;i!=values.length;++i){
			index = (String) JSONObject.fromObject(p_index[i]).get("index");
			type = (String) JSONObject.fromObject(p_index[i]).get("type");
			id = (String) JSONObject.fromObject(p_index[i]).get("id");

			bulkRequest.add(client.prepareIndex(index,type,id).setSource(JSONObject.fromObject(values[i])));
		}
		BulkResponse bulkResponse =  bulkRequest.execute().actionGet();
		if(bulkResponse.hasFailures()){
			//TODO
			
        }
	}
	
	/**
	 * 该方法为删除索引记录
	 * @throws Exception
	 */
	public void es_delete(String index,String type,String id,boolean operationThreaded){
		System.out.println("删除");
		DeleteResponse responsedd = client.prepareDelete(index, type, id)
		        .setOperationThreaded(operationThreaded)
		        .execute()
		        .actionGet();
	}
	
	/**
	 * 该方法为根据主键读取索引记录
	 * @throws Exception
	 */
	public void es_get(String index,String type,String id){
		System.out.println("根据主键搜索得到值");
		GetResponse responsere = client.prepareGet(index,type,id)
		        .execute()
		        .actionGet();
		System.out.println("完成读取--"+responsere.getSourceAsString());
	}
	
	/**
	 * 该方法为根据具体键值查找索引记录
	 * @throws Exception
	 */
	public void es_search(String indexName,String type,int from,int size,String searchName,String value){
		System.out.println("搜索");
		SearchRequestBuilder builder= client.prepareSearch(indexName)  //搜索productindex,prepareSearch(String... indices)注意该方法的参数,可以搜索多个索引
                .setTypes(type)  
                .setSearchType(SearchType.DEFAULT);		
	
		//全词匹配精确查找
		QueryBuilder qb2 = QueryBuilders.boolQuery()
		          .must(QueryBuilders.matchPhraseQuery(searchName, value));
		
		builder.setQuery(qb2);
		if(from != -1 && size != -1){
			builder.setFrom(from).setSize(size);
		} 
		SearchResponse responsesearch = builder.execute().actionGet(); 
		try{
			String jsondata= responsesearch.getHits().getHits()[0].getSourceAsString();
			System.out.println("搜索出来的数据jsondata-- "+jsondata);
		}catch(Exception es){
			es.printStackTrace();
		}
	}

}