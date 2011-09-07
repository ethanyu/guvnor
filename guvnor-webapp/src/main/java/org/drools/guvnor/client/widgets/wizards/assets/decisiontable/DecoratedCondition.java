/*
 * Copyright 2011 JBoss Inc
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
package org.drools.guvnor.client.widgets.wizards.assets.decisiontable;

import org.drools.ide.common.client.modeldriven.dt52.ConditionCol52;

/**
 * 
 */
class DecoratedCondition {

    private ConditionCol52 condition;
    private boolean        isDefinitionComplete;

    DecoratedCondition(ConditionCol52 condition) {
        this.condition = condition;
    }

    boolean isDefinitionComplete() {
        return isDefinitionComplete;
    }

    void setDefinitionComplete(boolean isDefinitionComplete) {
        this.isDefinitionComplete = isDefinitionComplete;
    }

    ConditionCol52 getCondition() {
        return condition;
    }

    String getFactField() {
        return this.condition.getFactField();
    }

    String getFieldType() {
        return this.condition.getFieldType();
    }

}
