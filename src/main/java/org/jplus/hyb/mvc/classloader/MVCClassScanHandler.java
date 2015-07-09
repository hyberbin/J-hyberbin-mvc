/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.hyb.mvc.classloader;

import org.jplus.annotation.Action;
import org.jplus.annotation.After;
import org.jplus.annotation.Before;
import org.jplus.annotation.Mapping;
import org.jplus.hyb.log.Logger;
import org.jplus.hyb.log.LoggerManager;
import org.jplus.hyb.mvc.bean.MVCBean;
import org.jplus.hyb.mvc.mapping.IMappingManager;
import org.jplus.hyb.mvc.mapping.MappingManager;
import org.jplus.scanner.IScanHandler;
import org.jplus.util.FileCopyUtils;
import org.jplus.util.Reflections;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author hyberbin
 */
public class MVCClassScanHandler extends ClassLoader implements IScanHandler, IMVCLoader {

    private static final Logger log = LoggerManager.getLogger(MVCClassScanHandler.class);
    private static final String[] separates = new String[]{"/", "{", "}"};
    private static final Integer[] empty = new Integer[]{};
    private final IMappingManager mappingManager = new MappingManager();
    public static final MVCClassScanHandler INSTANCE = new MVCClassScanHandler();
    private static final Properties config = new Properties();

    static {
        setProperties(VAR_SCAN_JAR, "false");
        setProperties(VAR_SCAN_JAR_REGEX, ".*Controller.*.\\.class");
        setProperties(VAR_SCAN_CLASSPATH_REGEX, ".*Controller.*.\\.class");
    }
    private MVCClassScanHandler() {
    }

    @Override
    public boolean filterJar(String path) {
        return Boolean.valueOf(config.getProperty(VAR_SCAN_JAR))&&path.matches(config.getProperty(VAR_SCAN_JAR_REGEX));
    }

    @Override
    public boolean filterPath(String path) {
        return path.matches(config.getProperty(VAR_SCAN_CLASSPATH_REGEX));
    }

    @Override
    public void dealWith(InputStream is) throws Exception {
        byte[] copyToByteArray = FileCopyUtils.copyToByteArray(is);
        Class defineClass = defineClass(null, copyToByteArray, 0, copyToByteArray.length);
        Class clazz = Class.forName(defineClass.getName());//如果不调用这个不能初始化
        try {
            if (clazz.isAnnotationPresent(Action.class)) {
                Object mvcObject = clazz.newInstance();
                Action annotation = (Action) clazz.getAnnotation(Action.class);
                String[] urlPatterns = annotation.urlPatterns();
                for (String url : urlPatterns) {
                    for (Method method : Reflections.getAllMethods(clazz)) {
                        MVCBean mvcb;
                        if (method.isAnnotationPresent(Mapping.class)) {
                            Mapping annotation1 = method.getAnnotation(Mapping.class);
                            String mode = annotation1.name().toLowerCase();
                            Set<Integer> ints = new TreeSet<Integer>();
                            if (mode.contains(separates[1])) {
                                String[] split = (url + mode).split(separates[0]);
                                for (int i = 0; i < split.length; i++) {
                                    String s = split[i];
                                    if (s.startsWith(separates[1]) && s.endsWith(separates[2])) {
                                        ints.add(i);
                                    }
                                }
                            }
                            Integer[] aints = ints.toArray(new Integer[]{});
                            mvcb = new MVCBean(method, mvcObject, aints);
                            if (annotation1.isDefault()) {
                                mappingManager.putMapping(url, mvcb);
                            } else {
                                mappingManager.putMapping(url + mode, mvcb);
                            }
                            mappingManager.putMapping(url, mvcb);
                        } else if (method.isAnnotationPresent(After.class)) {
                            mvcb = new MVCBean(method, mvcObject, empty);
                            mappingManager.putAfter(url, mvcb);
                        } else if (method.isAnnotationPresent(Before.class)) {
                            mvcb = new MVCBean(method, mvcObject, empty);
                            mappingManager.putBefore(url, mvcb);
                        }
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            log.error("GetAllClass不能访问类：{}", clazz.getName(), ex);
        } catch (InstantiationException ex) {
            log.error("GetAllClass不能创建实例：{}", clazz.getName(), ex);
        }
    }

    @Override
    public MVCBean getActionAfter(String url) {
        return mappingManager.getAfter(url);
    }

    @Override
    public MVCBean getActionBefore(String url) {
        return mappingManager.getBefore(url);
    }

    @Override
    public MVCBean getActionClass(String url) {
        return mappingManager.getMapping(url);
    }

    @Override
    public IMappingManager getMappingManager() {
        return mappingManager;
    }

    public static void setProperties(String key, String value) {
        config.setProperty(key, value);
    }
}
