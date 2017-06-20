package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import com.google.gson.JsonObject;
import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.AbstractLocalIdTypeAdapter;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

@Component
public class AssociationTypeAdapterProvider implements TypeAdapterProvider<LocalIdAssociation> {

    @Override
    public AssociationTypeAdapter getTypeAdapter() {
        return new AssociationTypeAdapter();
    }

    private static class AssociationTypeAdapter extends AbstractLocalIdTypeAdapter<LocalIdAssociation, Association>{
        @Override
        protected Class<Association> getResourceClass() {
            return Association.class;
        }

        @Override
        protected LocalIdAssociation toLocalIdResource(String id, Association association) {
            return new LocalIdAssociation(id, association);
        }

        @Override
        protected JsonObject decorate(JsonObject jsonObject, LocalIdAssociation object) {
            jsonObject = super.decorate(jsonObject, object);

            jsonObject.addProperty(ArangoDbServiceConstants.IS_HIERARCY_PROP, true);

            return jsonObject;
        }
    }

    @Override
    public Class<LocalIdAssociation> getType() {
        return LocalIdAssociation.class;
    }

}
