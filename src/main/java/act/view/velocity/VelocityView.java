package act.view.velocity;

import act.app.App;
import act.app.event.AppEventId;
import act.app.event.AppEventListener;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.util.EventObject;
import java.util.Properties;

public class VelocityView extends View {

    private VelocityEngine engine;

    @Override
    public String name() {
        return "velocity";
    }

    @Override
    protected Template loadTemplate(String resourcePath, ActContext context) {
        try {
            org.apache.velocity.Template template = engine.getTemplate(resourcePath);
            return new VelocityTemplate(template);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    @Override
    protected void init() {
        initEngine();
    }

    private void initEngine() {
        engine = new VelocityEngine();
        engine.init(conf());
    }

    private Properties conf() {
        Properties p = new Properties();
        p.setProperty("resource.loader", "act");
        p.setProperty("act.resource.loader.class", ActResourceLoader.class.getName());
        return p;
    }
}
