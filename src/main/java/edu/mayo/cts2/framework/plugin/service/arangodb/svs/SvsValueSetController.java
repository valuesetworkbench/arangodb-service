package edu.mayo.cts2.framework.plugin.service.arangodb.svs;

import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import edu.mayo.cts2.framework.webapp.rest.extensions.controller.ControllerProvider;
import ihe.iti.svs._2008.CD;
import ihe.iti.svs._2008.ConceptListType;
import ihe.iti.svs._2008.ObjectFactory;
import ihe.iti.svs._2008.RetrieveValueSetResponseType;
import ihe.iti.svs._2008.ValueSetResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;

@Controller("svsValueSetController")
@RequestMapping("/svs")
public class SvsValueSetController implements ControllerProvider {

    @Autowired
    private ValueSetDefinitionResolutionService valueSetDefinitionResolutionService;

    private ObjectFactory objectFactory = new ObjectFactory();

    private JAXBContext jaxbContext = JAXBContext.newInstance(RetrieveValueSetResponseType.class);

    public SvsValueSetController() throws JAXBException {
    }

    @RequestMapping("/RetrieveValueSet")
    public void getValueSetExpansion(@RequestParam("oid") String oid,
                                     HttpServletResponse response) throws Exception {
        ResolvedValueSet valueSet = valueSetDefinitionResolutionService.resolveDefinitionAsCompleteSet(new ValueSetDefinitionReadId(oid, null), null, null, null, null);

        if(valueSet == null) {
            this.sendNotFound(response);
            return;
        }

        RetrieveValueSetResponseType vsResponse = this.objectFactory.createRetrieveValueSetResponseType();

        ValueSetResponseType vs = new ValueSetResponseType();
        vs.setId(oid);

        ConceptListType conceptList = new ConceptListType();

        for(URIAndEntityName entry : valueSet.getEntry()) {
            CD cd = new CD();
            cd.setCode(entry.getName());
            cd.setCodeSystem(entry.getNamespace());
            cd.setDisplayName(entry.getDesignation());

            conceptList.getConcept().add(cd);
        }

        vs = vs.withConceptList(conceptList);

        vsResponse.setValueSet(vs);

        this.sendResponse(vsResponse, response);
    }

    protected void sendResponse(RetrieveValueSetResponseType resource, HttpServletResponse response) throws Exception {
        response.setContentType("application/xml");
        try {
            Marshaller jaxbMarshaller = this.jaxbContext.createMarshaller();

            JAXBElement<RetrieveValueSetResponseType> jaxbResponse = this.objectFactory.createRetrieveValueSetResponse(resource);

            jaxbMarshaller.marshal(jaxbResponse, response.getWriter());
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
        throw new UnsupportedOperationException("not implemented");
    }
}
