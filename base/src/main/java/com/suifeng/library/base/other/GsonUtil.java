package com.suifeng.library.base.other;

import com.google.gson.Gson;

/** 
 * Gson工具类
 */
public class GsonUtil {
	/** 
     * 对象转换成json字符串 
     * @param obj  
     * @return  
     */  
    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);  
    }  
  

    /** 
     * json字符串转成对象 
     * @param str   
     * @param type  
     * @return  
     */  
    public static <T> T fromJson(String str, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(str, type);  
    }  
}
