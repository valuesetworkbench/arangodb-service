package edu.mayo.cts2.framework.plugin.service.arangodb.fhir;

import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller("fhirValueSetController")
@RequestMapping("/fhir")
public class FhirValueSetController extends AbstractFhirController {

    @Autowired
    private ValueSetDefinitionResolutionService valueSetDefinitionResolutionService;

    @RequestMapping("/ValueSet/{valueSetId}/$expand")
    public void getValueSetExpansion(@PathVariable("valueSetId") String valueSetId,
                                     HttpServletResponse response) throws IOException {
        ResolvedValueSet valueSet = valueSetDefinitionResolutionService.resolveDefinitionAsCompleteSet(new ValueSetDefinitionReadId(valueSetId, null), null, null, null, null);

        if(valueSet == null) {
            this.sendNotFound(response);
            return;
        }

        ValueSet vs = new ValueSet();

        ValueSet.ValueSetExpansionComponent expansionComponent = new ValueSet.ValueSetExpansionComponent();

        for(URIAndEntityName entry : valueSet.getEntry()) {
            ValueSet.ValueSetExpansionContainsComponent component = new ValueSet.ValueSetExpansionContainsComponent();
            component.setCode(entry.getName());
            component.setSystem(entry.getNamespace());
            component.setDisplay(entry.getDesignation());

            expansionComponent.getContains().add(component);
        }

        vs.setExpansion(expansionComponent);

        this.sendResponse(vs, response);
    }

}
