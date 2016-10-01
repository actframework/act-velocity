package act.view.velocity;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.InputStream;

public class ActResourceLoader extends ClasspathResourceLoader {
    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
        InputStream is = null;
        if (name.startsWith("/velocity/")) {
            is = super.getResourceStream(name);
        }
        return null == is ? super.getResourceStream(attachPrefix(name)) : is;
    }

    @Override
    public boolean resourceExists(String resourceName) {
        if (resourceName.startsWith("/velocity/")) {
            if (super.resourceExists(resourceName)) {
                return true;
            }
        }
        return super.resourceExists(attachPrefix(resourceName));
    }

    private String attachPrefix(String path) {
        return (path.startsWith("/") ? "/velocity" : "/velocity/") + path;
    }
}
