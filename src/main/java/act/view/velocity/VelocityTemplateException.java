package act.view.velocity;

import act.Act;
import act.app.SourceInfo;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;

import java.lang.reflect.Method;
import java.util.List;

public class VelocityTemplateException extends act.view.TemplateException {

    public VelocityTemplateException(ParseException t) {
        super(t);
    }

    public VelocityTemplateException(freemarker.template.TemplateException t) {
        super(t);
        sourceInfo = getJavaSourceInfo(t.getCause());
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
        if (t instanceof ParseException) {
            templateInfo = new FreeMarkerSourceInfo((ParseException) t);
        } else if (t instanceof freemarker.template.TemplateException) {
            templateInfo = new FreeMarkerSourceInfo((freemarker.template.TemplateException) t);
        } else {
            throw E.unexpected("Unknown exception type: %s", t.getClass());
        }
    }

    @Override
    public String errorMessage() {
        Throwable t = getCauseOrThis();
        if (t instanceof ParseException || t instanceof TemplateException) {
            try {
                Method m = t.getClass().getDeclaredMethod("getDescription");
                m.setAccessible(true);
                return $.invokeVirtual(t, m);
            } catch (NoSuchMethodException e) {
                throw E.unexpected(e);
            }
        }
        return getCauseOrThis().getMessage();
    }

    @Override
    public List<String> stackTrace() {
        if (getCause() instanceof ParseException) {
            return C.list();
        }
        return super.stackTrace();
    }

    @Override
    protected boolean isTemplateEngineInvokeLine(String s) {
        return s.contains("freemarker.ext.beans.BeansWrapper.invokeMethod");
    }

    private static class FreeMarkerSourceInfo extends SourceInfo.Base {

        FreeMarkerSourceInfo(ParseException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateName();
            lines = readTemplateSource(fileName);
        }

        FreeMarkerSourceInfo(freemarker.template.TemplateException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateSourceName();
            lines = readTemplateSource(fileName);
        }

        private static List<String> readTemplateSource(String template) {
            FreeMarkerView view = (FreeMarkerView) Act.viewManager().view(FreeMarkerView.ID);
            return view.loadResources(template);
        }
    }
}
