package act.view.velocity;

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
