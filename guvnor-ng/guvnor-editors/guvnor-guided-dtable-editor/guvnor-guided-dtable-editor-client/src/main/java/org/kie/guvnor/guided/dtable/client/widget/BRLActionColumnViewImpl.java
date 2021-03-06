/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kie.guvnor.guided.dtable.client.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.shared.EventBus;
import org.kie.guvnor.datamodel.model.IAction;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.datamodel.oracle.DataType;
import org.kie.guvnor.guided.dtable.client.resources.i18n.Constants;
import org.kie.guvnor.guided.dtable.model.ActionCol52;
import org.kie.guvnor.guided.dtable.model.BRLActionColumn;
import org.kie.guvnor.guided.dtable.model.BRLActionVariableColumn;
import org.kie.guvnor.guided.dtable.model.BRLColumn;
import org.kie.guvnor.guided.dtable.model.BRLRuleModel;
import org.kie.guvnor.guided.dtable.model.GuidedDecisionTable52;
import org.kie.guvnor.guided.rule.client.editor.RuleModellerConfiguration;
import org.kie.guvnor.guided.rule.model.RuleModel;
import org.kie.guvnor.guided.template.model.InterpolationVariable;
import org.kie.guvnor.guided.template.model.RuleModelCloneVisitor;
import org.uberfire.backend.vfs.Path;

/**
 * An editor for a BRL Action Columns
 */
public class BRLActionColumnViewImpl extends AbstractBRLColumnViewImpl<IAction, BRLActionVariableColumn>
        implements
        BRLActionColumnView {

    private Presenter presenter;

    public BRLActionColumnViewImpl( final Path path,
                                    final DataModelOracle oracle,
                                    final GuidedDecisionTable52 model,
                                    final boolean isNew,
                                    final BRLActionColumn column,
                                    final EventBus eventBus ) {
        super( path,
               oracle,
               model,
               isNew,
               column,
               eventBus );

        setTitle( Constants.INSTANCE.ActionBRLFragmentConfiguration() );
    }

    protected boolean isHeaderUnique( String header ) {
        for ( ActionCol52 o : model.getActionCols() ) {
            if ( o.getHeader().equals( header ) ) {
                return false;
            }
        }
        return true;
    }

    protected BRLRuleModel getRuleModel( BRLColumn<IAction, BRLActionVariableColumn> column ) {
        BRLRuleModel ruleModel = new BRLRuleModel( model );
        List<IAction> definition = column.getDefinition();
        ruleModel.rhs = definition.toArray( new IAction[ definition.size() ] );
        return ruleModel;
    }

    protected RuleModellerConfiguration getRuleModellerConfiguration() {
        return new RuleModellerConfiguration( true,
                                              false,
                                              true );
    }

    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    protected void doInsertColumn() {
        this.editingCol.setDefinition( Arrays.asList( this.ruleModel.rhs ) );
        presenter.insertColumn( (BRLActionColumn) this.editingCol );
    }

    @Override
    protected void doUpdateColumn() {
        this.editingCol.setDefinition( Arrays.asList( this.ruleModel.rhs ) );
        presenter.updateColumn( (BRLActionColumn) this.originalCol,
                                (BRLActionColumn) this.editingCol );
    }

    @Override
    protected List<BRLActionVariableColumn> convertInterpolationVariables( Map<InterpolationVariable, Integer> ivs ) {

        //If there are no variables add a boolean column to specify whether the fragment should apply 
        if ( ivs.size() == 0 ) {
            BRLActionVariableColumn variable = new BRLActionVariableColumn( "",
                                                                            DataType.TYPE_BOOLEAN );
            variable.setHeader( editingCol.getHeader() );
            variable.setHideColumn( editingCol.isHideColumn() );
            List<BRLActionVariableColumn> variables = new ArrayList<BRLActionVariableColumn>();
            variables.add( variable );
            return variables;
        }

        //Convert to columns for use in the Decision Table
        BRLActionVariableColumn[] variables = new BRLActionVariableColumn[ ivs.size() ];
        for ( Map.Entry<InterpolationVariable, Integer> me : ivs.entrySet() ) {
            InterpolationVariable iv = me.getKey();
            int index = me.getValue();
            BRLActionVariableColumn variable = new BRLActionVariableColumn( iv.getVarName(),
                                                                            iv.getDataType(),
                                                                            iv.getFactType(),
                                                                            iv.getFactField() );
            variable.setHeader( editingCol.getHeader() );
            variable.setHideColumn( editingCol.isHideColumn() );
            variables[ index ] = variable;
        }

        //Convert the array into a mutable list (Arrays.toList provides an immutable list)
        List<BRLActionVariableColumn> variableList = new ArrayList<BRLActionVariableColumn>();
        for ( BRLActionVariableColumn variable : variables ) {
            variableList.add( variable );
        }
        return variableList;
    }

    @Override
    protected BRLColumn<IAction, BRLActionVariableColumn> cloneBRLColumn( BRLColumn<IAction, BRLActionVariableColumn> col ) {
        BRLActionColumn clone = new BRLActionColumn();
        clone.setHeader( col.getHeader() );
        clone.setHideColumn( col.isHideColumn() );
        clone.setChildColumns( cloneVariables( col.getChildColumns() ) );
        clone.setDefinition( cloneDefinition( col.getDefinition() ) );
        return clone;
    }

    private List<BRLActionVariableColumn> cloneVariables( List<BRLActionVariableColumn> variables ) {
        List<BRLActionVariableColumn> clone = new ArrayList<BRLActionVariableColumn>();
        for ( BRLActionVariableColumn variable : variables ) {
            clone.add( cloneVariable( variable ) );
        }
        return clone;
    }

    private BRLActionVariableColumn cloneVariable( BRLActionVariableColumn variable ) {
        BRLActionVariableColumn clone = new BRLActionVariableColumn( variable.getVarName(),
                                                                     variable.getFieldType(),
                                                                     variable.getFactType(),
                                                                     variable.getFactField() );
        clone.setHeader( variable.getHeader() );
        clone.setHideColumn( variable.isHideColumn() );
        clone.setWidth( variable.getWidth() );
        return clone;
    }

    private List<IAction> cloneDefinition( List<IAction> definition ) {
        RuleModelCloneVisitor visitor = new RuleModelCloneVisitor();
        RuleModel rm = new RuleModel();
        for ( IAction action : definition ) {
            rm.addRhsItem( action );
        }
        RuleModel rmClone = visitor.visitRuleModel( rm );
        List<IAction> clone = new ArrayList<IAction>();
        for ( IAction action : rmClone.rhs ) {
            clone.add( action );
        }
        return clone;
    }

}
