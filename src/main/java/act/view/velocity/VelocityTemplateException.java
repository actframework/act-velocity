package act.view.velocity;

/*-
 * #%L
 * ACT Velocity
 * %%
 * Copyright (C) 2017 ActFramework
 * %%
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
 * #L%
 */

import act.Act;
import act.app.SourceInfo;
import org.apache.velocity.exception.ExtendedParseException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.VelocityException;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.List;

public class VelocityTemplateException extends act.view.TemplateException {

    private VelocityException velocityException;

    public VelocityTemplateException(VelocityException t) {
        super(t);
        velocityException = t;
    }

    @Override
    public String errorMessage() {
        Throwable t = rootCauseOf(this);
        return t.toString();
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
        if (t instanceof MethodInvocationException) {
            sourceInfo = getJavaSourceInfo(t.getCause());
        }
        if (t instanceof ParseErrorException) {
            templateInfo = new VelocitySourceInfo((ParseErrorException) t);
        } else if (t instanceof ExtendedParseException) {
            templateInfo = new VelocitySourceInfo((ExtendedParseException) t);
        } else {
            throw E.unexpected("Unknown exception type: %s", t.getClass());
        }
    }

    @Override
    public List<String> stackTrace() {
        if (!(velocityException instanceof MethodInvocationException)) {
            return C.list();
        }
        return super.stackTrace();
    }

    @Override
    protected boolean isTemplateEngineInvokeLine(String s) {
        return s.contains("freemarker.ext.beans.BeansWrapper.invokeMethod");
    }

    private static class VelocitySourceInfo extends SourceInfo.Base {

        VelocitySourceInfo(ParseErrorException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateName();
            lines = readTemplateSource(fileName);
        }

        VelocitySourceInfo(ExtendedParseException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateName();
            lines = readTemplateSource(fileName);
        }

        private static List<String> readTemplateSource(String template) {
            VelocityView view = (VelocityView) Act.viewManager().view(VelocityView.ID);
            return view.loadContent(template);
        }
    }
}
