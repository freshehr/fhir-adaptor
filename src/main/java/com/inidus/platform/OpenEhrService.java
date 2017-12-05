package com.inidus.platform;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Connects to an openEHR backend and returns selected data
 */
@Service
public class OpenEhrService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected String sessionToken;
    // Ethercis
       private String cdrURL = "http://178.62.71.220:8080";
        private String cdrUsername = "guest";
        private String cdrPassword = "guest";

    // ThinkEhr oprn_hcbox domain
 //   private String cdrURL = "https://cdr.code4health.org";
 //   private String cdrUsername = "oprn_hcbox";
 //   private String cdrPassword = "XioTAJoO479";
 //   private String cdrBasicAuth = "Basic b3Bybl9oY2JveDpYaW9UQUpvTzQ3OQ==";

    public OpenEhrService() {
        log.trace("Constructor");
    }


    public String getSessionToken(String userName, String userPassword){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "";

        HttpEntity<String> postEntity = new HttpEntity<>(body,headers);

        String url = cdrURL+ "/rest/v1/session";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("username", userName)
                    .queryParam("password", userPassword);

        ResponseEntity<String> result = new RestTemplate().exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.POST,
                    postEntity,
                    String.class);

        JsonNode resultJson = null;
            try {
                resultJson = new ObjectMapper().readTree(result.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }

        log.trace("resultJSON: " + resultJson.toString());
            return resultJson.get("sessionId").asText();
        }

        public void deleteSessionToken(String sessionToken){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Ehr-Session", sessionToken);

            String body = "";
            HttpEntity<String> postEntity = new HttpEntity<>(body,headers);

            String url = cdrURL+ "/rest/v1/session";
            ResponseEntity<String> result = new RestTemplate().exchange(url, HttpMethod.DELETE, postEntity, String.class);

            JsonNode resultJson = null;
            try {
                resultJson = new ObjectMapper().readTree(result.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Should return "DELETE"
            String action = resultJson.asText("action");

        }

    public JsonNode getAllergyById(String id) throws IOException {
        return null;
    }


    public JsonNode getAllAllergies() throws IOException {


        sessionToken = getSessionToken(cdrUsername,cdrPassword);
        log.trace("Session token: "+ sessionToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

         headers.add("Ehr-Session", sessionToken);

        log.trace("After headers");
        // headers.add("Authorization", cdrBasicAuth);
        String aql = "select " +
                "e/ehr_id/value as ehrId, " +
                "e/ehr_status/subject/external_ref/id/value as subjectId, " +
                "e/ehr_status/subject/external_ref/namespace as subjectNamespace, " +
                "a/uid/value as compositionId, " +
                "b_a/uid/value as entryId, " +
                "b_a/data[at0001]/items[at0002]/value as Causative_agent, " +
                "b_a/data[at0001]/items[at0063]/value/defining_code/code_string as Status_code, " +
                "b_a/data[at0001]/items[at0101]/value/defining_code/code_string as Criticality_code, " +
                "b_a/data[at0001]/items[at0120]/value/defining_code/code_string as Category_code, " +
                "b_a/data[at0001]/items[at0117]/value/value as Onset_of_last_reaction, " +
                "b_a/data[at0001]/items[at0058]/value/defining_code/code_string as Reaction_mechanism_code, " +
                "b_a/data[at0001]/items[at0006]/value/value as Comment, " +
                "b_a/protocol[at0042]/items[at0062]/value/value as Adverse_reaction_risk_Last_updated, " +
                "b_a/data[at0001]/items[at0009]/items[at0010]/value as Specific_substance, " +
                "b_a/data[at0001]/items[at0009]/items[at0021]/value/defining_code/code_string as Certainty_code, " +
                "b_a/data[at0001]/items[at0009]/items[at0011]/value as Manifestation, " +
                "b_a/data[at0001]/items[at0009]/items[at0012]/value/value as Reaction_description, " +
                "b_a/data[at0001]/items[at0009]/items[at0027]/value/value as Onset_of_reaction, " +
                "b_a/data[at0001]/items[at0009]/items[at0089]/value/defining_code/code_string as Severity_code, " +
                "b_a/data[at0001]/items[at0009]/items[at0106]/value as Route_of_exposure, " +
                "b_a/data[at0001]/items[at0009]/items[at0032]/value/value as Adverse_reaction_risk_Comment " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
                "where a/name/value='Adverse reaction list'";

        String body = "{\"aql\" : \"" + aql + "\"}";


        HttpEntity<String> postEntity = new HttpEntity<>(body, headers);

        String url = cdrURL+ "/rest/v1/query";

        log.trace("url : " + url);
        log.trace("After postEntity : " + body);
        ResponseEntity<String> result = new RestTemplate().exchange(url, HttpMethod.POST, postEntity, String.class);

        log.trace("POST: " + result.toString());

        JsonNode resultJson = new ObjectMapper().readTree(result.getBody());

        deleteSessionToken(sessionToken);

        return resultJson.get("resultSet");


    }
}
