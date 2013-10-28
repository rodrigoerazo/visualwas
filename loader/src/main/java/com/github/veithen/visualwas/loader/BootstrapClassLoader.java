package com.github.veithen.visualwas.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Loads classes from <tt>bootstrap.jar</tt>. Delegates to the specified parent class loader, except
 * for classes that we need to override.
 */
final class BootstrapClassLoader extends URLClassLoader {
    BootstrapClassLoader(File wasHome, ClassLoader parent) throws IOException {
        super(new URL[] { new File(wasHome, "lib/bootstrap.jar").toURI().toURL() }, parent);
    }
    
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.equals("com.ibm.websphere.logging.WsLevel")) {
            // TODO: is ignoring resolve OK?
            return BootstrapClassLoader.class.getClassLoader().loadClass(name);
        } else {
            return super.loadClass(name, resolve);
        }
    }
}
