package act.view.velocity;

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
