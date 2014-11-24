package com.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.util.ESMappingBiz;
import com.util.TimeHelper;


@Controller
public class LoginController {

    @RequestMapping(value="/login")
    public ModelAndView login(HttpServletRequest request,HttpServletResponse response){
    	/*模拟json数据___START*/
    	JSONObject json = new JSONObject();
    	
    	Map<String, Object> map_i1 = new HashMap<String, Object>();
    	Map<String, Object> map_i2 = new HashMap<String, Object>();
    	Map<String, Object> map_v1 = new HashMap<String, Object>();
    	Map<String, Object> map_v2 = new HashMap<String, Object>();
    	map_i1.put("index", "productindex1");
    	map_i1.put("type", "prindextype");
    	map_i1.put("id", "jk8231");
    	
    	map_i2.put("index", "productindex1");
    	map_i2.put("type", "prindextype");
    	map_i2.put("id", "jk8234");
    	
    	map_v1.put("title", "abcd1");
    	map_v1.put("description", "苏州新科兰德科技有限公司");
    	map_v1.put("price", 232);
    	map_v1.put("onSale",true);
    	map_v1.put("type",2);
    	map_v1.put("createDate",TimeHelper.getCurrentTime());
    	map_v1.put("dfsfs","哈哈");
    	
    	map_v2.put("title", "abcd2");
    	map_v2.put("description", "中国人2");
    	map_v2.put("price", 232);
    	map_v2.put("onSale",true);
    	map_v2.put("type",22);
    	map_v2.put("createDate",TimeHelper.getCurrentTime());
    	
    	List<Map> list_v = new ArrayList<Map>();
    	List<Map> list_i = new ArrayList<Map>();
    	list_v.add(map_v1);
    	list_v.add(map_v2);
    	list_i.add(map_i1);
    	list_i.add(map_i2);
    	
    	
    	JSONArray j_values = JSONArray.fromObject(list_v);
    	JSONArray j_index = JSONArray.fromObject(list_i);
    	
    	json.put("index", j_index);
    	json.put("values", j_values);
    	
    	System.out.println("\n最终构造的JSON数据格式：");
        System.out.println(json);
    	/*模拟json数据___END*/
        
    	ESMappingBiz esm=new ESMappingBiz();
		esm.setClient(esm.buildClient());
		try {
			//插入json数据
			esm.es_add(json);
			
			//搜索数据。from:0,size:50; 如果不需要from和size，都设为-1.
			esm.es_search("productindex1","prindextype",0,50,"description", "苏州新科兰德科技有限公司");
			
			//根据主键搜
			esm.es_get("productindex1", "prindextype", "jk8234");
			
			//根据index，type，id删除数据
	//		esm.es_delete("productindex", "prindextype", "jk8234",false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			esm.getClient().close();
		}
        ModelAndView mv = new ModelAndView("/index");
        return mv;
    }
}