package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");  // 自带的方法
    }

    // MD5加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static  String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if(map != null){
            for(String key: map.keySet()){
                json.put(key, map.get(key));
            }
        }
        // json对象转化为字符串
        return json.toJSONString();
    }
    //如果没有业务返回数据，重载方法(方便调用)
    public static  String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }

    public static  String getJSONString(int code){
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("mood","sad");
        map.put("name","katniss");
        System.out.println(getJSONString(0, "ok", map));
    }


}
