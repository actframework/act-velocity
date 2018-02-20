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

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.util.ExtProperties;
import org.osgl.util.S;

import java.io.Reader;

public class ActResourceLoader extends ClasspathResourceLoader {

    private String root;

    @Override
    public void init(ExtProperties configuration) {
        super.init(configuration);
        root = configuration.getString("path");
        if (S.blank(root)) {
            root = "/" + VelocityView.ID;
        }
    }

    @Override
    public Reader getResourceReader(String name, String encoding) throws ResourceNotFoundException {
        Reader reader = null;
        if (name.startsWith(root)) {
            reader = super.getResourceReader(name, encoding);
        }
        return null == reader ? super.getResourceReader(attachPrefix(name), encoding) : reader;
    }

    @Override
    public boolean resourceExists(String resourceName) {
        return super.resourceExists(attachPrefix(resourceName));
    }

    private String attachPrefix(String path) {
        StringBuilder sb = S.builder(root);
        if (!path.startsWith("/")) {
            sb.append("/");
        }
        return sb.append(path).toString();
    }
}
