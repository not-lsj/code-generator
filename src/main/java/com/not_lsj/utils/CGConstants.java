package com.not_lsj.utils;

public class CGConstants {

	//ftl文件地址
	public static final String propLocation = "ftl";
	
	//配置文件名
	public static final String propFileName = "common.properties";
	//实体类的生成路径
	public static final String POJOLocation = System.getProperty("user.dir")+"/src/main/java/com/not_lsj/entity";
	//Service接口类的生成路径
	public static final String ServiceLocation = System.getProperty("user.dir")+"/src/main/java/com/not_lsj/service";
	//Service接口实现类的生成路径
	public static final String ServiceImplLocation = System.getProperty("user.dir")+"/src/main/java/com/not_lsj/service/impl";
	//Service接口实现类的生成路径
	public static final String ControllerLocation = System.getProperty("user.dir")+"/src/main/java/com/not_lsj/controller";
	
	public static final String HtmlLocation = System.getProperty("user.dir")+"/src/main/resources/static/views";
}
