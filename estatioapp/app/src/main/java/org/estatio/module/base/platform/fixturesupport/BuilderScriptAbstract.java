/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.module.base.platform.fixturesupport;

import javax.inject.Inject;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.runtime.system.context.IsisContext;

import org.estatio.module.party.dom.role.PartyRoleTypeService;

public abstract class BuilderScriptAbstract<T extends BuilderScriptAbstract> extends FixtureScript {

    public T build(ExecutionContext executionContext) {
        IsisContext.getSessionFactory().getServicesInjector().injectServicesInto(this);
        execute(executionContext);
        return (T)this;
    }

    @Inject
    PartyRoleTypeService partyRoleTypeService;

}

