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
package org.estatio.module.lease.seed;

import org.apache.isis.applib.annotation.Programmatic;

import org.incode.module.docrendering.freemarker.fixture.RenderingStrategyFSForFreemarker;
import org.incode.module.docrendering.stringinterpolator.fixture.RenderingStrategyFSForStringInterpolator;
import org.incode.module.docrendering.stringinterpolator.fixture.RenderingStrategyFSForStringInterpolatorCaptureUrl;
import org.incode.module.docrendering.stringinterpolator.fixture.RenderingStrategyFSForStringInterpolatorPreviewAndCaptureUrl;
import org.incode.module.docrendering.xdocgoten.fixture.RenderingStrategyFSForXDocGotenToPdf;
import org.incode.module.docrendering.xdocreport.fixture.RenderingStrategyFSForXDocReportToDocx;
import org.incode.module.docrendering.xdocreport.fixture.RenderingStrategyFSForXDocReportToPdf;
import org.incode.module.document.fixture.DocumentTemplateFSAbstract;

@Programmatic
public class RenderingStrategies extends DocumentTemplateFSAbstract {

    public static final String REF_SIPC = RenderingStrategyFSForStringInterpolatorPreviewAndCaptureUrl.REF;
    public static final String REF_SINC = RenderingStrategyFSForStringInterpolatorCaptureUrl.REF;
    public static final String REF_SI = RenderingStrategyFSForStringInterpolator.REF;
    public static final String REF_FMK = RenderingStrategyFSForFreemarker.REF;
    public static final String REF_XDP = RenderingStrategyFSForXDocReportToPdf.REF;
    public static final String REF_XDD = RenderingStrategyFSForXDocReportToDocx.REF;
    public static final String REF_XGP = RenderingStrategyFSForXDocGotenToPdf.REF;


    @Override
    protected void execute(final ExecutionContext executionContext) {

        // prereqs

        executionContext.executeChild(this, new RenderingStrategyFSForStringInterpolatorPreviewAndCaptureUrl());
        executionContext.executeChild(this, new RenderingStrategyFSForStringInterpolatorCaptureUrl());
        executionContext.executeChild(this, new RenderingStrategyFSForStringInterpolator());
        executionContext.executeChild(this, new RenderingStrategyFSForFreemarker());

        executionContext.executeChild(this, new RenderingStrategyFSForXDocReportToPdf());
        executionContext.executeChild(this, new RenderingStrategyFSForXDocReportToDocx());
        executionContext.executeChild(this, new RenderingStrategyFSForXDocGotenToPdf());
    }

}
