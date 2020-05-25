package com.ray.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.List;

/**
 * description:
 * Created by Ray on 2020-05-25
 */
public class DemoSolr3 {
    public static void main(String[] args) throws IOException, SolrServerException {
        HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8080/solr").build();
        SolrQuery query = new SolrQuery("*:*");
        QueryResponse response = client.query("hotel", query, SolrRequest.METHOD.GET);
        SolrDocumentList results = response.getResults();
        long numFound = results.getNumFound();
        System.out.println("numFound：" + numFound);
     /*   for (SolrDocument result : results) {
            System.out.println(result.get("id")+"--"+result.get("hotelName")+"--"+result.get("minPrice")+"--"+result.get("maxPrice"));
        }*/

        List<ItripHotelVO> beans = response.getBeans(ItripHotelVO.class);
        for (ItripHotelVO bean : beans) {
            System.out.println(bean.getId()+"--"+bean.getHotelName());
        }
    }
}
