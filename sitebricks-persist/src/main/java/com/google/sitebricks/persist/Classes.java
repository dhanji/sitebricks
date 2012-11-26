package com.google.sitebricks.persist;


import com.google.inject.matcher.Matcher;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Utility class that finds all the classes in a given package.
 * (based on a similar utility in TestNG).
 * See <code>org.testng.internal.PackageUtils</code>
 * <p/>
 * Created on Feb 24, 2006
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class Classes {
  private final Matcher<? super Class<?>> matcher;
  private final Logger log = Logger.getLogger(Classes.class.getName());

  private Classes(Matcher<? super Class<?>> matcher) {
    this.matcher = matcher;
  }

  /**
   * @param pack A list of packages to scan, recursively.
   * @return A set of all the classes inside this package
   * @throws RuntimeException           Thrown when error reading from disk or jar.
   * @throws IllegalStateException      Thrown when something very odd is
   *                                    happening with classloaders.
   */
  public Set<Class<?>> in(Package pack) {
    String packageName = pack.getName();
    String packageOnly = pack.getName();

    final boolean recursive = true;

    Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    String packageDirName = packageOnly.replace('.', '/');

    Enumeration<URL> dirs;
    try {
      dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);

    } catch (IOException e) {
      throw new RuntimeException(
          "Could not read from package directory: " + packageDirName, e);
    }

    while (dirs.hasMoreElements()) {
      URL url = dirs.nextElement();
      String protocol = url.getProtocol();

      if ("file".equals(protocol)) {
        findClassesInDirPackage(packageOnly, toPath(url), recursive, classes);
      } else if ("jar".equals(protocol)) {
        JarFile jar;

        try {
          jar = ((JarURLConnection) url.openConnection()).getJarFile();
        } catch (IOException e) {
          throw new RuntimeException("Could not read from jar url: " + url, e);
        }

        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          String name = entry.getName();
          if (name.charAt(0) == '/') {
            name = name.substring(1);
          }
          if (name.startsWith(packageDirName)) {
            int idx = name.lastIndexOf('/');
            if (idx != -1) {
              packageName = name.substring(0, idx).replace('/', '.');
            }

            if ((idx != -1) || recursive) {
              //it's not inside a deeper dir
              if (name.endsWith(".class") && !entry.isDirectory()) {
                String className = name.substring(packageName.length() + 1, name.length() - 6);
                //include this class in our results

                /* Issue #26 - package-info causes ISE during package scan. This ia a bit of
                 * a hack for just package-info.
                 */
                if (!"package-info".equalsIgnoreCase(className)) {
                  add(packageName, classes, className);
                }
              }
            }
          }
        }
      }
    }

    return classes;
  }

  private void add(String packageName, Set<Class<?>> classes, String className) {

    Class<?> clazz;
    try {
      clazz = Class.forName(packageName + '.' + className);
    } catch (ClassNotFoundException e) {
      log.severe("A class discovered by the scanner could not be found by the ClassLoader, " +
          "something very odd has happened with the classloading (see root cause): " +
          e.toString());

      throw new IllegalStateException(
          "A class discovered by the scanner could not be found by the ClassLoader", e);
    }

    if (matcher.matches(clazz))
      classes.add(clazz);
  }

  private void findClassesInDirPackage(String packageName,
                                       String packagePath,
                                       final boolean recursive,
                                       Set<Class<?>> classes) {
    File dir = new File(packagePath);

    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }

    File[] dirfiles = dir.listFiles(new FileFilter() {
      public boolean accept(File file) {
        return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
      }
    });

    for (File file : dirfiles) {
      if (file.isDirectory()) {
        findClassesInDirPackage(packageName + "." + file.getName(),
            file.getAbsolutePath(),
            recursive,
            classes);
      } else {
        String className = file.getName().substring(0, file.getName().length() - 6);
        //include class
        add(packageName, classes, className);
      }
    }
  }

  public static Classes matching(Matcher<? super Class<?>> matcher) {
    return new Classes(matcher);
  }

  private static String toPath(final URL url) {
    String path = url.getPath();
    StringBuilder buf = new StringBuilder();
    for ( int i = 0, length = path.length(); i < length; i++ ) {
      char c = path.charAt( i );
      if ( '/' == c ) {
        buf.append( File.separatorChar );
      } else if ( '%' == c && i < length - 2 ) {
        buf.append( (char) Integer.parseInt( path.substring( ++i, ++i + 1 ), 16 ) );
      } else {
        buf.append( c );
      }
    }
    return buf.toString();
  }
}
