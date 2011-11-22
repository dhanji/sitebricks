package com.google.sitebricks.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softee.management.exception.ManagementException;
import org.softee.management.helper.MBeanRegistration;
import javax.management.*;
import java.util.Random;

/**
 * @author jochen@pedesis.org (Jochen Bekmann)
 */
public class JmxUtil {
  private static final Logger log = LoggerFactory.getLogger(JmxUtil.class);
  private static final Random random = new Random();

  public static MBeanRegistration registerMBean(Object object, String packageName,
                                   String typeName, String uniqueName) {

    try {
      MBeanRegistration registration = new MBeanRegistration(object, new ObjectName(packageName + ":type=" +
          typeName + ", name=" + uniqueName + "[" + random.nextInt() + "]"));
      registration.register();
      return registration;
    } catch (ManagementException e) {
      log.error("Failed to register MBean.", e);
    } catch (MalformedObjectNameException e) {
      log.error("Failed to register MBean.", e);
    }
    return null;
  }

  public static void unregister(MBeanRegistration registration) {
    try {
      registration.unregister();
    } catch (ManagementException e) {
      log.error("Failed to unregister MBean.", e);
    }
  }

}
