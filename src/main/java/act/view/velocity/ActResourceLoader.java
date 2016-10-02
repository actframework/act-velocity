package act.view.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.osgl.util.S;

import java.io.InputStream;

public class ActResourceLoader extends ClasspathResourceLoader {

    private String root;

    @Override
    public void init(ExtendedProperties configuration) {
        super.init(configuration);
        root = configuration.getString("path");
        if (S.blank(root)) {
            root = "/" + VelocityView.ID;
        }
    }

    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
        InputStream is = null;
        if (name.startsWith(root)) {
            is = super.getResourceStream(name);
        }
        return null == is ? super.getResourceStream(attachPrefix(name)) : is;
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
