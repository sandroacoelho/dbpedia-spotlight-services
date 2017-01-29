package org.dbpedia.spotlight.rest;

import feign.Feign;
import feign.form.FormEncoder;
import lombok.RequiredArgsConstructor;
import org.dbpedia.spotlight.approach.Model;
import org.dbpedia.spotlight.common.AnnotationUnit;
import org.dbpedia.spotlight.common.SemanticMediaType;
import org.dbpedia.spotlight.formats.JSON;
import org.dbpedia.spotlight.formats.NIFWrapper;
import org.dbpedia.spotlight.services.SpotlightConfiguration;
import org.dbpedia.spotlight.services.SpotlightLanguageDetector;
import org.dbpedia.spotlight.services.TextExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.core.MediaType;
import java.util.Optional;

import static org.dbpedia.spotlight.common.Constants.EMPTY;
import static org.dbpedia.spotlight.formats.JSON.to;

@Controller(value = "/annotate")
@RequiredArgsConstructor
public class AnnotateRest implements AnnotateResource {

    private final SpotlightLanguageDetector languageDetector;

    private final TextExtractor textExtractor;

    private final SpotlightConfiguration configuration;

    private String serviceRequest(Optional<String> text,
                                          Optional<String> inUrl,
                                          Optional<Double> confidence,
                                          Optional<String> dbpediaTypes,
                                          String outputFormat) {

        if (inUrl.isPresent()) {
            text = Optional.of(textExtractor.extract(inUrl.get()));
        }

        String language = languageDetector.language(text.get());

        Model model = Feign.builder().encoder(new FormEncoder()).target(Model.class,
                String.format(configuration.URL, configuration.getSpotlightURL(), language));


        if (MediaType.TEXT_HTML.equalsIgnoreCase(outputFormat)) {
            return model.html(text.get(), dbpediaTypes.orElse(EMPTY),
                    confidence.orElse(configuration.DEFAULT_CONFIDENCE));


        }

        return model.annotate(text.get(), dbpediaTypes.orElse(EMPTY),
                    confidence.orElse(configuration.DEFAULT_CONFIDENCE));

    }

    private String getSemanticFormats(Optional<String> text,
                                      Optional<String> inUrl,
                                      Optional<Double> confidence,
                                      Optional<String> dbpediaTypes,
                                      String outputFormat) {
        NIFWrapper nif;

        if (inUrl.isPresent()) {
            nif = new NIFWrapper(configuration, inUrl.get());
        } else {
            nif = new NIFWrapper(configuration);
        }

        AnnotationUnit annotationUnit = to(serviceRequest(text, inUrl, confidence, dbpediaTypes, outputFormat));
        nif.entity(annotationUnit);

        return nif.getNIF(outputFormat);
    }


    @Override
    public ResponseEntity<String> html(@RequestParam("text") Optional<String> text,
                               @RequestParam("url") Optional<String> inUrl,
                               @RequestParam("confidence") Optional<Double> confidence,
                               @RequestParam("support") Optional<Integer> support,
                               @RequestParam("types") Optional<String> dbpediaTypes,
                               @RequestParam("sparql") Optional<String> sparqlQuery,
                               @RequestParam("policy") Optional<String> policy,
                               @RequestParam("coreferenceResolution") Optional<Boolean> coreferenceResolution,
                               @RequestParam("spotter") Optional<String> spotter,
                               @RequestParam("disambiguator") Optional<String> disambiguatorName) {

        String result = serviceRequest(text, inUrl, confidence, dbpediaTypes, MediaType.TEXT_HTML);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    @Override
    @ResponseBody
    public AnnotationUnit json(@RequestParam("text") Optional<String> text,
                   @RequestParam("url") Optional<String> inUrl,
                   @RequestParam("confidence") Optional<Double> confidence,
                   @RequestParam("support") Optional<Integer> support,
                   @RequestParam("types") Optional<String> dbpediaTypes,
                   @RequestParam("sparql") Optional<String> sparqlQuery,
                   @RequestParam("policy") Optional<String> policy,
                   @RequestParam("coreferenceResolution") Optional<Boolean> coreferenceResolution,
                   @RequestParam("spotter") Optional<String> spotter,
                   @RequestParam("disambiguator") Optional<String> disambiguatorName) {

        return  to(serviceRequest(text, inUrl, confidence, dbpediaTypes, MediaType.APPLICATION_JSON));


    }

    @Override
    @ResponseBody
    public ResponseEntity<String> nif(@RequestParam("text") Optional<String> text,
                                      @RequestParam("url") Optional<String> inUrl,
                                      @RequestParam("confidence") Optional<Double> confidence,
                                      @RequestParam("support") Optional<Integer> support,
                                      @RequestParam("types") Optional<String> dbpediaTypes,
                                      @RequestParam("sparql") Optional<String> sparqlQuery,
                                      @RequestParam("policy") Optional<String> policy,
                                      @RequestParam("coreferenceResolution") Optional<Boolean> coreferenceResolution,
                                      @RequestParam("spotter") Optional<String> spotter,
                                      @RequestParam("disambiguator") Optional<String> disambiguatorName) {

        String result =  getSemanticFormats(text, inUrl, confidence, dbpediaTypes, SemanticMediaType.TEXT_TURTLE);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<String> triples(@RequestParam("text") Optional<String> text,
                             @RequestParam("url") Optional<String> inUrl,
                             @RequestParam("confidence") Optional<Double> confidence,
                             @RequestParam("support") Optional<Integer> support,
                             @RequestParam("types") Optional<String> dbpediaTypes,
                             @RequestParam("sparql") Optional<String> sparqlQuery,
                             @RequestParam("policy") Optional<String> policy,
                             @RequestParam("coreferenceResolution") Optional<Boolean> coreferenceResolution,
                             @RequestParam("spotter") Optional<String> spotter,
                             @RequestParam("disambiguator") Optional<String> disambiguatorName) {


        String result = getSemanticFormats(text, inUrl, confidence, dbpediaTypes, SemanticMediaType.APPLICATION_N_TRIPLES);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String>  jsonld(@RequestParam("text") Optional<String> text,
                            @RequestParam("url") Optional<String> inUrl,
                            @RequestParam("confidence") Optional<Double> confidence,
                            @RequestParam("support") Optional<Integer> support,
                            @RequestParam("types") Optional<String> dbpediaTypes,
                            @RequestParam("sparql") Optional<String> sparqlQuery,
                            @RequestParam("policy") Optional<String> policy,
                            @RequestParam("coreferenceResolution") Optional<Boolean> coreferenceResolution,
                            @RequestParam("spotter") Optional<String> spotter,
                            @RequestParam("disambiguator") Optional<String> disambiguatorName) {

        String result = getSemanticFormats(text, inUrl, confidence, dbpediaTypes, SemanticMediaType.APPLICATION_LD_JSON);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

}
