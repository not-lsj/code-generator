package com.not_lsj.utils;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * 加载配置文件
 *
 * @author Administrator
 */
public class PropLoader {

    private static Properties prop = null;

    private static Properties planProp = null;

    public PropLoader() {
    }

    public static void load() throws IOException {
        if (null == prop) {
            prop = new Properties();
            URL url = PropLoader.class.getResource("/" + CGConstants.propFileName);
            File file = new File(url.getFile());
            //加载配置文件
            FileInputStream fileInputStream = new FileInputStream(file);
            prop.load(fileInputStream);
            fileInputStream.close();
            for (Object key : prop.keySet()) {
                System.out.println("加载通用配置：" + key + " => " + prop.getProperty(key.toString(), ""));
            }
        }
    }

    public static String getProp(String propName) {
        String res = "";
        if (null == prop) {
            System.err.println("加载通用配置出错， 请先加载配置文件");
        } else {
            res = prop.containsKey(propName) ? prop.getProperty(propName) : "";
        }
        return res;
    }

    public static Properties getProp() {
        return prop;
    }

    public static void loadPlan(String plan) throws IOException {
        if (null == planProp) {
            planProp = new Properties();
            try (InputStream inputStream = PropLoader.class.getResourceAsStream("/realize/" + plan + ".properties")) {
                if (inputStream == null) {
                    throw new FileNotFoundException("生成计划配置文件 " + plan + ".properties 没有找到");
                }
                planProp.load(inputStream);
            }
            for (Object key : planProp.keySet()) {
                System.out.println("加载生成计划配置：" + key + " => " + planProp.getProperty(key.toString(), ""));
            }
        }
    }

    public static String getPlanProp(String propName) {
        String res = "";
        if (null == planProp) {
            System.err.println("加载通用配置出错， 请先加载配置文件");
        } else {
            res = planProp.containsKey(propName) ? planProp.getProperty(propName) : "";
        }
        return res;
    }
}
