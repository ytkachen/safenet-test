package com.cisco.wap;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

/**
 * @author Yuri Tkachenko
 */
public class ExtLibClassLoaderContainer {
    private static final String PROTOCOL_TYPE_FILE = "file";

    private URLClassLoader urlClassLoader;

    public ExtLibClassLoaderContainer (String libRepoPath) {
        File classPath = new File(libRepoPath);
        File[] libraryList = classPath.listFiles();
        if (libraryList == null || libraryList.length == 0) {
            return;
        }
        try {
            URL[] urls = new URL[libraryList.length];
            URLStreamHandler handler = null;
            for(int i = 0; i < libraryList.length; i++) {
                String libURL = new URL(PROTOCOL_TYPE_FILE, null,
                        libraryList[i].getCanonicalPath()).toString();
                urls[i] = new URL(null, libURL, handler);
            }
            urlClassLoader = new URLClassLoader(urls, null);
        } catch (Exception e) {
            urlClassLoader = null;
        }
    }

    public URLClassLoader getURLClassLoader() {
        return urlClassLoader;
    }

    public void destroyClassLoader() {
        if (urlClassLoader != null) {
            try {
                urlClassLoader.close();
            } catch (Exception e) {

            }
        }
    }
}