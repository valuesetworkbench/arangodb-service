package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.AbstractLocalIdTypeAdapter;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

@Component
public class ValueSetDefinitionTypeAdapterProvider implements TypeAdapterProvider<LocalIdValueSetDefinition> {

    @Override
    public ValueSetDefinitionTypeAdapter getTypeAdapter() {
        return new ValueSetDefinitionTypeAdapter();
    }

    private static class ValueSetDefinitionTypeAdapter extends AbstractLocalIdTypeAdapter<LocalIdValueSetDefinition, ValueSetDefinition>{
        @Override
        protected Class<ValueSetDefinition> getResourceClass() {
            return ValueSetDefinition.class;
        }

        @Override
        protected LocalIdValueSetDefinition toLocalIdResource(String id, ValueSetDefinition valueSetDefinition) {
            return new LocalIdValueSetDefinition(id, valueSetDefinition);
        }
    }

    @Override
    public Class<LocalIdValueSetDefinition> getType() {
        return LocalIdValueSetDefinition.class;
    }

}
