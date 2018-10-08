package com.apress.demo.controllers;
/*
 * Created by hakdogan on 28/11/2017
 */


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.apress.demo.dao.QueryDAO;
import com.apress.demo.entities.Document;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;


@RestController
public class RestRequestController {

    @Autowired
    private QueryDAO dao;

    /**
     *
     * @param document
     * @return
     */
    @PostMapping(value = "/api/create", consumes = "application/json; charset=utf-8")
    public String create(@RequestBody Document document) {
        return dao.createIndex(document);
    }

    @PostMapping(value = "/api/update", consumes = "application/json; charset=utf-8")
    public String update(@RequestBody Document document){
        return dao.updateDocument(document);
    }

    /**
     *
     * @param id
     */
    @GetMapping(value = "/api/delete")
    public void delete(String id){
        dao.deleteDocument(id);
    }

    /**
     *
     * @return
     */
    @GetMapping(value = "/api/all", produces = "application/json; charset=utf-8")
    public List<Document> getAllDocuments() {
        return dao.matchAllQuery();
    }

    /**
     *
     * @param query
     * @return
     */
    @GetMapping("/api/search")
    public List<Document> search(@RequestParam("query") String query) {
        return dao.wildcardQuery(query);
    }
    
    /**
    *
    * @param query
    * @return
    */
   @GetMapping("/api/searchStr")
   public List<JsonNode> searchStr(@RequestParam("query") String query) {
       return dao.wildcardQueryStr(query);
   }
    
   /**
   *
   * @param query
   * @return
   */
  @GetMapping("/api/searchSrcFilters")
  public List<JsonNode> searchSrcFilters(@RequestParam("indexs")String[] indexs,@RequestParam("query") String query,@RequestParam("includeFields")String[]includeFields,@RequestParam("excludeFields")String[]excludeFields, int from,int size ) {
      return dao.wildcardQueryFields(query, indexs,includeFields, excludeFields, from, size);
  }
    
    
}



