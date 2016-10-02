package act.view.velocity;

import act.app.App;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.IO;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import static org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER;

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
    protected void init(App app) {
        initEngine(app);
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

    private void initEngine(App app) {
        engine = new VelocityEngine();
        engine.init(conf(app));
    }

    private Properties conf(App app) {
        Properties p = new Properties();

        p.setProperty(RESOURCE_LOADER, "file,class");
        p.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
        p.setProperty("file.resource.loader.path", templateRootDir().getAbsolutePath());
        p.setProperty("file.resource.loader.cache", app.isDev() ? "false" : "true");
        p.setProperty("file.resource.loader.modificationCheckInterval", "0");

        p.setProperty("class.resource.loader.class", ActResourceLoader.class.getName());
        p.setProperty("class.resource.loader.path", templateHome());
        return p;
    }
}
