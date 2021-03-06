package org.kie.guvnor.enums.client.handlers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.handlers.DefaultNewResourceHandler;
import org.kie.guvnor.commons.ui.client.save.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.save.SaveOperationService;
import org.kie.guvnor.enums.client.resources.i18n.Constants;
import org.kie.guvnor.enums.client.resources.images.ImageResources;
import org.kie.guvnor.enums.service.EnumService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PathPlaceRequest;
import org.uberfire.shared.mvp.PlaceRequest;

/**
 * Handler for the creation of new Enumerations
 */
@ApplicationScoped
public class NewEnumHandler extends DefaultNewResourceHandler {

    private static String FILE_TYPE = "enumeration";

    @Inject
    private Caller<EnumService> enumService;

    @Inject
    private PlaceManager placeManager;

    @Override
    public String getFileType() {
        return FILE_TYPE;
    }

    @Override
    public String getDescription() {
        return Constants.INSTANCE.newEnumDescription();
    }

    @Override
    public IsWidget getIcon() {
        return new Image( ImageResources.INSTANCE.enumsIcon() );
    }

    @Override
    public void create(final String fileName) {
        final Path path = buildFullPathName(fileName);

        new SaveOperationService().save(path, new CommandWithCommitMessage() {
            @Override
            public void execute(final String comment) {
                enumService.call(new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void aVoid) {
                        notifySuccess();
                        final PlaceRequest place = new PathPlaceRequest(path,
                                "EnumEditor");
                        placeManager.goTo(place);
                    }
                }).save(path,
                        "",
                        comment);
            }
        });
    }

}
