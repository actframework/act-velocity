package act.view.velocity;

import act.app.App;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.osgl.$;
import org.osgl.util.E;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import static org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER;

public class VelocityView extends View {

    public static final String ID = "velocity";

    private VelocityEngine engine;
    private VelocityEngine stringEngine;

    private ResourceManager resourceManager;

    private String suffix;

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
            if (resourcePath.endsWith(suffix)) {
                return null;
            }
            return loadTemplate(S.concat(resourcePath, suffix), context);
        } catch (VelocityException e) {
            throw new VelocityTemplateException(e);
        }
    }

    @Override
    protected Template loadInlineTemplate(String s, ActContext actContext) {
        stringResourceRepository().putStringResource(s, s);
        org.apache.velocity.Template template = stringEngine.getTemplate(s);
        return new VelocityTemplate(template);
    }

    private StringResourceRepository stringResourceRepository() {
        return (StringResourceRepository) stringEngine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
    }

    @Override
    protected void init(App app) {
        engine = new VelocityEngine();
        engine.init(conf(app));
        suffix = app.config().get("view.velocity.suffix");
        if (null == suffix) {
            suffix = ".vm";
        } else {
            suffix = suffix.startsWith(".") ? suffix : S.concat(".", suffix);
        }
        initStringEngine();
    }

    private void initStringEngine() {
        stringEngine = new VelocityEngine();
        stringEngine.setProperty(Velocity.RESOURCE_LOADER, "string");
        stringEngine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        stringEngine.addProperty("string.resource.loader.repository.static", "false");
        stringEngine.init();
    }

    public List<String> loadContent(String template) {
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
