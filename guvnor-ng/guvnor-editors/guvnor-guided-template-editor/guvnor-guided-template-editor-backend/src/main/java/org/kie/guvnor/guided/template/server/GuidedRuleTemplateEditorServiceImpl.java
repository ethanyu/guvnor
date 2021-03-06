/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.guided.template.server;

import java.util.Collection;
import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.kie.guvnor.commons.data.workingset.WorkingSetConfigData;
import org.kie.guvnor.commons.service.validation.model.BuilderResult;
import org.kie.guvnor.commons.service.verification.model.AnalysisReport;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.datamodel.service.DataModelService;
import org.kie.guvnor.guided.template.model.GuidedTemplateEditorContent;
import org.kie.guvnor.guided.template.model.TemplateModel;
import org.kie.guvnor.guided.template.server.util.BRDRTPersistence;
import org.kie.guvnor.guided.template.server.util.BRDRTXMLPersistence;
import org.kie.guvnor.guided.template.service.GuidedRuleTemplateEditorService;
import org.kie.guvnor.services.config.ResourceConfigService;
import org.kie.guvnor.services.metadata.MetadataService;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.security.Identity;

@Service
@ApplicationScoped
public class GuidedRuleTemplateEditorServiceImpl
        implements GuidedRuleTemplateEditorService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private Paths paths;

    @Inject
    private DataModelService dataModelService;

    @Inject
    private ResourceConfigService resourceConfigService;

    @Inject
    private MetadataService metadataService;

    @Inject
    private Identity identity;

    @Override
    public GuidedTemplateEditorContent loadContent( final Path path ) {
        final TemplateModel model = loadTemplateModel( path );
        final DataModelOracle dataModel = dataModelService.getDataModel( path );
        return new GuidedTemplateEditorContent( dataModel, model );
    }

    @Override
    public TemplateModel loadTemplateModel( final Path path ) {
        return (TemplateModel) BRDRTXMLPersistence.getInstance().unmarshal( ioService.readAllString( paths.convert( path ) ) );
    }

    @Override
    public void save( final Path path,
                      final TemplateModel model,
                      final String comment ) {
        ioService.write( paths.convert( path ),
                         BRDRTXMLPersistence.getInstance().marshal( model ),
                         makeCommentedOption( comment ) );
    }

    @Override
    public void save( final Path resource,
                      final TemplateModel model,
                      final Metadata metadata,
                      final String comment ) {

        ioService.write(paths.convert(resource),
                BRDRTXMLPersistence.getInstance().marshal(model),
                metadataService.setUpAttributes(resource, metadata),
                makeCommentedOption(comment));
    }
    
    @Override
    public void delete( final Path path, final String comment ) {
        System.out.println( "USER:" + identity.getName() + " DELETING asset [" + path.getFileName() + "]");

        ioService.delete( paths.convert( path ) );
    }
    
    @Override
    public void rename( final Path path, final String newName, final String comment ) {
        System.out.println( "USER:" + identity.getName() + " RENAMING asset [" + path.getFileName() + "] to [" + newName + "]" );

        String targetName = path.getFileName().substring(0, path.getFileName().lastIndexOf("/")+1) + newName;
        String targetURI = path.toURI().substring(0, path.toURI().lastIndexOf("/")+1) + newName;
        Path targetPath = PathFactory.newPath(path.getFileSystem(), targetName, targetURI);
        ioService.move(paths.convert( path ), paths.convert( targetPath ), new CommentedOption( identity.getName(), comment ));
    }
    
    @Override
    public void copy( final Path path, final String newName, final String comment ) {
        System.out.println( "USER:" + identity.getName() + " COPYING asset [" + path.getFileName() + "] to [" + newName + "]" );
        
        String targetName = path.getFileName().substring(0, path.getFileName().lastIndexOf("/")+1) + newName;
        String targetURI = path.toURI().substring(0, path.toURI().lastIndexOf("/")+1) + newName;
        Path targetPath = PathFactory.newPath(path.getFileSystem(), targetName, targetURI);
        ioService.copy(paths.convert( path ), paths.convert( targetPath ), new CommentedOption( identity.getName(), comment ));
    }
    
    @Override
    public String toSource( final TemplateModel model ) {
        return BRDRTPersistence.getInstance().marshal( model );
    }

    @Override
    public AnalysisReport verify( final Path path,
                                  final TemplateModel content,
                                  final Collection<WorkingSetConfigData> activeWorkingSets ) {
        //TODO {porcelli} verify
        return new AnalysisReport();
    }

    @Override
    public BuilderResult validate( final Path path,
                                   final TemplateModel content ) {
        //TODO {porcelli} validate
        return new BuilderResult();
    }

    @Override
    public boolean isValid( final Path path,
                            final TemplateModel content ) {
        return !validate( path,
                          content ).hasLines();
    }

    private CommentedOption makeCommentedOption( final String commitMessage ) {
        final String name = identity.getName();
        final Date when = new Date();
        final CommentedOption co = new CommentedOption( name,
                                                        null,
                                                        commitMessage,
                                                        when );
        return co;
    }

}
