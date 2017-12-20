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

import act.view.TemplateBase;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.osgl.$;
import org.osgl.util.E;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class VelocityTemplate extends TemplateBase {

    Template tmpl;

    VelocityTemplate(Template tmpl) {
        this.tmpl = $.notNull(tmpl);
    }

    @Override
    protected String render(Map<String, Object> renderArgs) {
        Writer w = new StringWriter();
        Context ctx = new VelocityContext(renderArgs);
        try {
            tmpl.merge(ctx, w);
        } catch (VelocityException e) {
            throw new VelocityTemplateException(e);
        } catch (Exception e) {
            throw E.unexpected(e);
        }
        return w.toString();
    }
}
