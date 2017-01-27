package org.dbpedia.spotlight.approach;


import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface Model {

    @RequestLine("POST /annotate")
    @Headers({"Content-Type: application/x-www-form-urlencoded", "Accept: application/json"})
    String annotate(@Param("text") String text, @Param("types") String dbpediaTypes, @Param("confidence") Double confidence);
}