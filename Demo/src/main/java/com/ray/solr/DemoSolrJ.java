package com.ray.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.SolrHttpClientBuilder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;

/**
 * description:
 * Created by Ray on 2020-05-25
 */
public class DemoSolrJ {
    public static void main(String[] args) throws IOException, SolrServerException {
        //创建solr的http请求客户端对象
        HttpSolrClient client =
                new HttpSolrClient.Builder("http://localhost:8080/solr").build();
        //创建查询参数
        SolrQuery query = new SolrQuery("*:*");
        //执行查询获得响应结果
        QueryResponse response = client.query("core1", query, SolrRequest.METHOD.GET);
        //从响应中获取数据
        SolrDocumentList results = response.getResults();
        for (SolrDocument doc : results) {
            System.out.println(doc.get("id")+"--"+doc.get("hotelName")+"--"+doc.get("address"));
        }
    }
}
