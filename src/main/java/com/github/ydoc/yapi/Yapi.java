package com.github.ydoc.yapi;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @author yujian
 * @description 导入的json实体
 * @create 2021-04-23 15:43
 **/
@Data
public class Yapi {
    private Long      index;
    private String    name;
    private String    desc;
    private Long      add_time;
    private Long      up_time;
    private List<Api> list;
    @Data
    public static class Api{
        private Query query_path;
        private Long edit_uid = 0L;
        private String status;
        private String type;
        private Boolean req_body_is_json_schema;
        private Boolean res_body_is_json_schema;
        private Boolean api_opened;
        private Long index;
        private List<String> tag = Collections.emptyList();
        private Long _id;
        private String method;
        private Long catid;
        private String title;
        private String path;
        private Long project_id;
        private List<String> req_params = Collections.emptyList();;
        private String res_body_type;
        private Long uid;
        private Long      add_time;
        private Long      up_time;
        private List<ReqQuery> req_query = Collections.emptyList();;
        private List<Yapi.Header> req_headers ;
        private List<String> req_body_form = Collections.emptyList();;
        private Long __v;
        private String desc = "";
        private String markdown = "";
        private String req_body_other;
        private String req_body_type = "json";
        private String res_body;



    }
    @Data
    public static class Query{
        private String path;
        private List<String> params;
    }
    @Data
    public static class ReqQuery{
        private String required;
        private String _id;
        private String name;
        private String example;
        private String desc;
    }
    @Data
    public static class Header{
        private String required = "1";
        private String _id = IdAuto.get();
        private String name = "Content-Type";
        private String value = "application/json";
    }
}
