/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.guvnor.server;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.rpc.DetailedSerializationException;
import org.drools.guvnor.client.rpc.RuleAsset;
import org.drools.guvnor.client.rpc.StandaloneGuidedEditorService;
import org.drools.guvnor.server.guidededitor.BRLRuleAssetProvider;
import org.drools.guvnor.server.guidededitor.NewRuleAssetProvider;
import org.drools.guvnor.server.guidededitor.RuleAssetProvider;
import org.drools.guvnor.server.guidededitor.UUIDRuleAssetProvider;
import org.drools.guvnor.server.util.LoggingHelper;
import org.drools.ide.common.client.modeldriven.brl.RuleMetadata;
import org.drools.ide.common.client.modeldriven.brl.RuleModel;
import org.drools.ide.common.server.util.BRLPersistence;
import org.drools.repository.RulesRepository;
import org.jboss.seam.annotations.In;


import org.drools.ide.common.server.util.BRXMLPersistence;

/**
 * All the needed Services in order to get the Guided Editor running as standalone
 * app.
 * @author esteban.aliverti
 */
public class StandaloneGuidedEditorServiceImplementation extends RemoteServiceServlet
        implements
        StandaloneGuidedEditorService {

    @In
    public RulesRepository repository;
    private static final long serialVersionUID = 520l;
    private static final LoggingHelper log = LoggingHelper.getLogger(StandaloneGuidedEditorServiceImplementation.class);

    public RulesRepository getRulesRepository() {
        return this.repository;
    }

    private ServiceImplementation getService() {
        return RepositoryServiceServlet.getService();
    }

    /**
     * To open the Guided Editor as standalone, you should be gone through 
     * GuidedEditorServlet first. This servlet put all the POST parameters into
     * session. This method takes those parameters and load the corresponding
     * assets.
     * If you are passing BRLs to the Guided Editor, this method will create
     * one asset per BRL with a unique name.
     * @return
     * @throws DetailedSerializationException
     */   
    public RuleAsset[] loadRuleAssetsFromSession() throws DetailedSerializationException{
        
        //Get the parameters from the session
        HttpSession session = this.getThreadLocalRequest().getSession();
        
        String packageName = (String)session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_PACKAGE_PARAMETER_NAME.getParameterName());
        String categoryName = (String)session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_CATEGORY_PARAMETER_NAME.getParameterName());
        String[] initialBRL = (String[])session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_BRL_PARAMETER_NAME.getParameterName());
        String[] assetsUUIDs = (String[])session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_ASSETS_UUIDS_PARAMETER_NAME.getParameterName());
        
        boolean createNewAsset = false;
        Object attribute = session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_CREATE_NEW_ASSET_PARAMETER_NAME.getParameterName());
        if (attribute != null){
            createNewAsset = Boolean.parseBoolean(attribute.toString());
        }
        String ruleName = (String)session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_RULE_PARAMETER_NAME.getParameterName());
        
        
        
        boolean hideLHSInEditor = false;
        attribute = session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_HIDE_RULE_LHS_PARAMETER_NAME.getParameterName());
        if (attribute != null){
            hideLHSInEditor = Boolean.parseBoolean(attribute.toString());
        }
        
        boolean hideRHSInEditor = false;
        attribute = session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_HIDE_RULE_RHS_PARAMETER_NAME.getParameterName());
        if (attribute != null){
            hideRHSInEditor = Boolean.parseBoolean(attribute.toString());
        }
        
        boolean hideAttributesInEditor = false;
        attribute = session.getAttribute(GuidedEditorServlet.GUIDED_EDITOR_SERVLET_PARAMETERS.GE_HIDE_RULE_ATTRIBUTES_PARAMETER_NAME.getParameterName());
        if (attribute != null){
            hideAttributesInEditor = Boolean.parseBoolean(attribute.toString());
        }
        
        
        RuleAssetProvider provider; 
        Object data;
        if (createNewAsset){
            provider = new NewRuleAssetProvider();
            data = ruleName;
        }else if (assetsUUIDs != null){
            provider = new UUIDRuleAssetProvider();
            data = assetsUUIDs;
        }else if (initialBRL != null){
            provider = new BRLRuleAssetProvider();
            data = initialBRL;
        }else{
            throw new IllegalStateException();
        }
        
        return provider.getRuleAssets(packageName, categoryName, data, hideLHSInEditor, hideRHSInEditor, hideAttributesInEditor);
        
    }
    
    /**
     * Returns the DRL source code of the given assets.
     * @param assetsUUIDs
     * @return
     * @throws SerializationException
     */
    public String[] getAsstesDRL(String[] assetsUUIDs) throws SerializationException{
        
        String[] sources = new String[assetsUUIDs.length];
        
        for (int i = 0; i < assetsUUIDs.length; i++) {
            RuleAsset ruleAsset = this.getService().loadRuleAsset(assetsUUIDs[i]);
            sources[i] = this.getService().buildAssetSource(ruleAsset);
        }
        
        return sources;
    }
    
    /**
     * Returns the DRL source code of the given assets.
     * @param assetsUUIDs
     * @return
     * @throws SerializationException
     */
    public String[] getAsstesBRL(String[] assetsUUIDs) throws SerializationException{
        
        String[] sources = new String[assetsUUIDs.length];
        
        BRLPersistence converter = BRXMLPersistence.getInstance();
        for (int i = 0; i < assetsUUIDs.length; i++) {
            RuleAsset ruleAsset = this.getService().loadRuleAsset(assetsUUIDs[i]);
            sources[i] = converter.marshal((RuleModel) ruleAsset.content);
        }
        
        return sources;
    }
    
    /**
     * Remove all the given assets
     * @param assetsUUIDs the assets UUIDs
     */
    public void removeAssets(String[] assetsUUIDs){
        this.getService().removeAssets(assetsUUIDs);
    }
}
