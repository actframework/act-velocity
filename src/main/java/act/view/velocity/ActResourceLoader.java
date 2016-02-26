package act.view.velocity;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.InputStream;

public class ActResourceLoader extends ClasspathResourceLoader {
    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
        return super.getResourceStream("/velocity" + name);
    }

    @Override
    public boolean resourceExists(String resourceName) {
        return super.resourceExists("/velocity" + resourceName);
    }
}
