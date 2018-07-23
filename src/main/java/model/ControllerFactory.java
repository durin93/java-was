package model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotation.Controller;
import annotation.RequestMapping;

public class ControllerFactory {

	private static final Logger log = LoggerFactory.getLogger(ControllerFactory.class);
	private static Map<String, Object> controllers = new HashMap<String, Object>();
	private static String ROOT;

	static {

		URL packageDirURL = Thread.currentThread().getContextClassLoader().getResource("./");
		ROOT = packageDirURL.getFile();
		log.debug("ROOT : "+ROOT);

		File directory = new File(ROOT);
		List<Class<?>> classes = new ArrayList<>();

		if (directory.exists()) {
			scan(directory.listFiles(), classes, ROOT);
		}

		for (Class<?> clazz : classes) {
			if (clazz.isAnnotationPresent(Controller.class)) {
				addController(clazz);
			}
		}
	}

	public static void scan(File[] files, List<Class<?>> classes, String presentPath) {
		for (File file : files) {
			isFile(file, classes, presentPath);
		}
	}
	
	public static void isFile(File file, List<Class<?>> classes, String presentPath) {
		if (file.isDirectory()) {
			scan(file.listFiles(), classes, presentPath + file.getName());
			return;
		} 
		if(file.getName().endsWith(".class")) {
			addClassFileToList(file.getName(), presentPath, classes);
		}
	}

	public static void addClassFileToList(String fileName, String directoryName, List<Class<?>> classes) {
		try {
			Class<?> clazz = Class.forName(directoryName.substring(ROOT.length())+"." + fileName.substring(0, fileName.length() - 6)); // Dynamic Loading
			classes.add(clazz);
		    log.debug("Path ! : {}", directoryName);
            log.debug("File Name! : {}", fileName);
		} catch (ClassNotFoundException e) {
			log.debug("addClassFileToList error");
			e.printStackTrace();
		}
	}

	public static void addController(Class<?> clazz) {
		if (clazz.isAnnotationPresent(RequestMapping.class)) {
			log.debug("requestMapping url : {}", clazz.getAnnotation(RequestMapping.class).value());
			try {
				controllers.put(clazz.getAnnotation(RequestMapping.class).value(), clazz.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				log.debug("addController error");
				e.printStackTrace();
			}
		}
	}

	public static Object getController(String requestUrl) {
		Set<String> set = controllers.keySet();
		for (String string : set) {
			if (requestUrl.startsWith(string) && !requestUrl.contains(".")) {
				return controllers.get(string);
			}
		}
		return null;
	}

}