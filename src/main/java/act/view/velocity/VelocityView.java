package act.view.velocity;

import act.util.ActContext;
import act.view.Template;
import act.view.View;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.IO;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

public class VelocityView extends View {

    public static final String ID = "velocity";

    private VelocityEngine engine;

    private ResourceManager resourceManager;

    @Override
    public String name() {
        return ID;
    }

    @Override
    protected Template loadTemplate(String resourcePath, ActContext context) {
        try {
            org.apache.velocity.Template template = engine.getTemplate(resourcePath);
            return new VelocityTemplate(template);
        } catch (ResourceNotFoundException e) {
            return null;
        } catch (VelocityException e) {
            throw new VelocityTemplateException(e);
        }
    }

    @Override
    protected void init() {
        initEngine();
    }

    List<String> loadContent(String template) {
        if (null == resourceManager) {
            try {
                Field fieldRi = VelocityEngine.class.getDeclaredField("ri");
                fieldRi.setAccessible(true);
                RuntimeInstance ri = (RuntimeInstance) fieldRi.get(engine);

                Field field = RuntimeInstance.class.getDeclaredField("resourceManager");
                field.setAccessible(true);
                resourceManager = (ResourceManager) field.get(ri);
            } catch (Exception e) {
                throw E.unexpected(e);
            }
        }

        try {
            Method method = ResourceManagerImpl.class.getDeclaredMethod("getLoaderForResource", String.class);
            method.setAccessible(true);
            ResourceLoader resourceLoader = $.invokeVirtual(resourceManager, method, template);
            return IO.readLines(resourceLoader.getResourceStream(template));
        } catch (NoSuchMethodException e) {
            throw E.unexpected(e);
        }
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
