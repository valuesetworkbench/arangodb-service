package edu.mayo.cts2.framework.plugin.service.arangodb.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import edu.mayo.cts2.framework.webapp.rest.extensions.controller.ControllerProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractFhirController implements ControllerProvider {

    FhirContext ctxDstu3 = FhirContext.forDstu3();

    protected void sendResponse(IBaseResource resource, HttpServletResponse response) {
        IParser jsonParser = ctxDstu3.newJsonParser();

        response.setContentType("application/json");
        try {
            jsonParser.encodeResourceToWriter(resource, response.getWriter());
            response.setStatus(200);
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    protected void sendNotFound(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setStatus(404);
    }

    @Override
    public Object getController() {
        return this;
    }

}
