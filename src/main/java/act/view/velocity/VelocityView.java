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

import static org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER;

import act.app.App;
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
import osgl.version.Version;
import osgl.version.Versioned;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

@Versioned
public class VelocityView extends View {

    public static final Version VERSION = Version.get();

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
    protected Template loadTemplate(String resourcePath) {
        try {
            org.apache.velocity.Template template = engine.getTemplate(resourcePath);
            return new VelocityTemplate(template);
        } catch (ResourceNotFoundException e) {
            if (resourcePath.endsWith(suffix)) {
                return null;
            }
            return loadTemplate(S.concat(resourcePath, suffix));
        } catch (VelocityException e) {
            throw new VelocityTemplateException(e);
        }
    }

    @Override
    protected Template loadInlineTemplate(String s) {
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
