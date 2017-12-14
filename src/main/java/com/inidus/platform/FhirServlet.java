package com.inidus.platform;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

/**
 * The RESTful server responding to FHIR requests
 */
@WebServlet(urlPatterns = {"/fhir/**"}, displayName = "FHIR Adaptor")
@Component
public class FhirServlet extends RestfulServer {
    @Autowired
    AllergyProvider allergyProvider;

    public FhirServlet() {
        super(FhirContext.forDstu3());
    }

    @Override
    protected void initialize() throws ServletException {
        super.initialize();

        LoggerFactory.getLogger(getClass()).info("Initialising FHIR Servlet");

        List<IResourceProvider> providers = new ArrayList<>();
        providers.add(this.allergyProvider);
        setResourceProviders(providers);

        registerInterceptor(new ResponseHighlighterInterceptor());
        setDefaultPrettyPrint(true);
        setDefaultResponseEncoding(EncodingEnum.JSON);


   /*
 Code to create FHIR narrative - working but narrative template is immature so disable for now.

     String propFile = "file:./src/main/java/com/inidus/platform/customnarrative.properties";
        CustomThymeleafNarrativeGenerator gen = new CustomThymeleafNarrativeGenerator(propFile);
        getFhirContext().setNarrativeGenerator(gen);

*/
    }
}
