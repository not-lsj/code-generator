package com.not_lsj.utils;

import freemarker.template.*;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 代码生成工具
 *
 * @author Administrator
 */
public class Revoker {

    public Revoker() {

    }


    private List<String> primaryKeys;

    public void revoke() throws Exception {
        Version version = new Version("2.3.27");
        // 这个版本号不知道是干吗的。https://freemarker.apache.org/docs/api/freemarker/template/Configuration.html#Configuration-freemarker.template.Version-
        URL url = Revoker.class.getResource("/" + CGConstants.propLocation);

        Configuration conf = new Configuration(version);
        conf.setDefaultEncoding("UTF-8");
        conf.setObjectWrapper(new DefaultObjectWrapper(version));
        conf.setDirectoryForTemplateLoading(new File(url.getFile()));

        String tableName = PropLoader.getPlanProp("db.tableName");
        // freemarker测试
        // generateTest(conf,tableName);

        // 生成POJO类
        deletePOJO(conf, tableName);
        // 生成service接口类
        deleteService(conf, tableName);
        // 生成serviceimpl实现类
        deleteServiceImpl(conf, tableName);
        // 生成Controller
        deleteController(conf, tableName);
        // 生成页面
        deletePage(conf, tableName);
        // 生成config.js的配置建议
        generatePageConf(conf, tableName);
        //TODO 辅助功能，帮忙实现一下Mybatis的mapper和xml
        //默认实现CRUD以后还是加到BaseServiceImpl里。这里只作为工具方法，生成一下。
        //generateMyBatisMapper(conf,tableName);
        //generateMyBatisXml(conf,tableName);
    }

    private void generatePageConf(Configuration conf, String tableName) throws Exception {
        // TODO Auto-generated method stub
        Template template = conf.getTemplate("configjs.ftl");
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        Writer writer = new OutputStreamWriter(System.out, "UTF-8");
        template.process(paramMap, writer);
        writer.flush();
        //writer.close();
    }

    private void deletePage(Configuration conf, String tableName) throws Exception {
        //生成Html页面，html页面是变化最多的部分。目前的实现逻辑麻烦的就在查询条件那一部分， 初期的最初实现就把POJO中配置了TableColumn属性的列全部生成为文本类型的查询条件， 以后再优化。
        System.out.println("==== 开始生成 " + tableName + " 对应的管理页面 ====");
        Template template = conf.getTemplate("Html.ftl");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();

        // 生成文件
        String folderLocation = CGConstants.HtmlLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String filelocation = folderLocation + "/" + pojoName + ".html";
        File pageFile = new File(filelocation);
        if (pageFile.exists()) {
            pageFile.delete();
            System.out.println("页面文件删除成功");
        } else {
            System.out.println("页面 文件不存在，略过");
        }
        System.out.println("==== " + tableName + " 表对应的页面文件删除成功 ====");
    }

    private void deleteController(Configuration conf, String tableName) throws Exception {
        System.out.println("==== 开始生成 " + tableName + " 表对应的Controller实现类 ====");
        Template template = conf.getTemplate("Controller.ftl");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();
        // 生成文件
        String folderLocation = CGConstants.ControllerLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String serviceLocation = folderLocation + "/" + pojoName + "Controller.java";
        File serviceFile = new File(serviceLocation);
        if (serviceFile.exists()) {
            serviceFile.delete();
            System.out.println("controller文件删除成功");
        } else {
            System.out.println("controller 文件不存在，略过");
        }
        System.out.println("==== " + tableName + " 表对应的Controller接口类删除成功 ====");
    }

    private void deleteServiceImpl(Configuration conf, String tableName) throws Exception {
        System.out.println("==== 开始删除 " + tableName + " 表对应的serviceImpl实现类 ====");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();
        // 生成文件
        String folderLocation = CGConstants.ServiceImplLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String serviceLocation = folderLocation + "/" + pojoName + "ServiceImpl.java";
        File serviceFile = new File(serviceLocation);
        if (serviceFile.exists()) {
            serviceFile.delete();
            System.out.println("service文件删除成功");
        } else {
            System.out.println("service 文件不存在，略过");
        }
        System.out.println("==== " + tableName + " 表对应的Service接口类删除成功 ====");
    }

    private void deleteService(Configuration conf, String tableName) throws Exception {
        System.out.println("==== 开始删除 " + tableName + " 表对应的service接口 ====");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();
        // 生成文件
        String folderLocation = CGConstants.ServiceLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String serviceLocation = folderLocation + "/" + pojoName + "Service.java";
        File serviceFile = new File(serviceLocation);
        if (serviceFile.exists()) {
            serviceFile.delete();
            System.out.println("service文件删除成功");
        } else {
            System.out.println("service 文件不存在，略过");
        }
        System.out.println("==== " + tableName + " 表对应的Service接口类删除成功 ====");
    }

    private void deletePOJO(Configuration conf, String tableName)
            throws Exception {
        System.out.println("==== 开始删除 " + tableName + " 表对应的POJO ====");
        // 删除文件
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String pojoLocation = CGConstants.POJOLocation + "/" + pojoName + ".java";
        File pojoFile = new File(pojoLocation);
        if (pojoFile.exists()) {
            pojoFile.delete();
            System.out.println("POJO 删除完成");
        } else {
            System.out.println("POJO 未生成，略过此步骤");
        }
        System.out.println("==== " + tableName + " 表对应的POJO类删除成功 ====");
    }

    // 一些公用的参数就一起封装一下。
    private Map<String, Object> getCommonParam(String tableName) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String pojoName = CGUtil.getPOJONameByTableName(tableName);
        paramMap.put("pojo_name", pojoName);
        String moduleName = PropLoader.getPlanProp("plan.moduleName");
        paramMap.put("module_name", moduleName);
        String genTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        paramMap.put("gen_time", genTime);
        return paramMap;
    }

    public static void main(String[] args) throws Exception {
        //mysql
        Class.forName("com.mysql.jdbc.Driver");
        Properties props = new Properties();
        props.put("useInformationSchema", "true"); //mysql获取表注释需要加上这个属性 
        props.put("user", "root");
        props.put("password", "root");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/genauth?characterEncoding=utf-8&autoReconnect=true", props);
        //oracle
//		Class.forName("oracle.jdbc.driver.OracleDriver");
//		Connection con = DriverManager.getConnection("jdbc:oracle:thin:@172.16.49.65:1521/motion","swordrisk","risk#230");
//		Connection con = DriverManager.getConnection("jdbc:oracle:thin:@172.16.49.65:1521/motion","swordui","swo#1234");

//		 Properties props = new Properties();  
//         props.put("remarksReporting", "true");  //要获取注释，需要增加这个属性。
//         props.put("user", "swordrisk");  
//         props.put("password", "risk#230");  
//         Connection con = DriverManager.getConnection("jdbc:oracle:thin:@172.16.49.65:1521/motion",props);
        System.out.println("========映射表信息==============");
        DatabaseMetaData meta = con.getMetaData();
        ResultSet tables = meta.getTables("genserver", "%", "USERINFOTEST", new String[]{"TABLE"});
        while (tables.next()) {
            ResultSetMetaData metaData = tables.getMetaData();
            System.out.println(metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println(metaData.getColumnName(i) + " ==> " + tables.getString(metaData.getColumnName(i)));
            }

            System.out.println(tables.getString("TABLE_NAME") + " --->>> " + tables.getString("REMARKS"));
        }
        System.out.println("========映射列信息==============");
        ResultSet columns = meta.getColumns("genserver", "%", "USERINFOTEST", "%");
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String columnType = columns.getString("TYPE_NAME");
            int datasize = columns.getInt("COLUMN_SIZE");
            int digits = columns.getInt("DECIMAL_DIGITS");
            int nullable = columns.getInt("NULLABLE");
            String remarks = columns.getString("REMARKS");
            System.out.println(columnName + " " + columnType + " " + datasize + " " + digits + " " + nullable + " " + remarks);
        }
        System.out.println("========映射主键信息==============");
        ResultSet primaryKeys = meta.getPrimaryKeys("genserver", "%", "USERINFOTEST");
        while (primaryKeys.next()) {
            ResultSetMetaData metaData = primaryKeys.getMetaData();
            System.out.println(metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println(metaData.getColumnName(i) + " ==> " + primaryKeys.getString(metaData.getColumnName(i)));
            }
        }
    }
}
