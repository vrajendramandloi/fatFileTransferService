package com.util.uni.fatfiletransfer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableAutoConfiguration
public class MainRestController implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);

	@Value("${server.port}")
	private int serverPort;
	private static Map<String, String> fileMap = new HashMap<String, String>();
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	private String generateRandomUUID() {
		String uuidText = UUID.randomUUID().toString();
		do {
			uuidText = UUID.randomUUID().toString();
		}while(fileMap.containsKey(uuidText));
		return uuidText;
	}
	
	@RequestMapping(
			value="/files", method= RequestMethod.GET)
	public String getStatusValidate() throws UnknownHostException {
		StringBuilder builder = new StringBuilder();
		String iphost = Inet4Address.getLocalHost().getHostAddress();
		String jarPath = "";
		String sharedFiles = System.getProperty("sharedFiles");
		List<String> filesList = new ArrayList<String>();
		if(!StringUtils.isBlank(sharedFiles)) {
			for(String str : sharedFiles.split(";")) {
				filesList.add(str);
			}
		}
		logger.info("filesList.====================== {}",filesList.toString());
		try {
			builder.append("<HTML><body><br/>");
			File file = new File(".");
			jarPath = file.getAbsolutePath();
			logger.info("Jar Path.====================== {}",jarPath);
			if(!StringUtils.isBlank(jarPath)) {
				File[] files = file.listFiles();
				logger.info("Files in Path.====================== {}",Arrays.toString(files));
				for(File fl : files) {
					String flpath = fl.getAbsolutePath();
					String flName = fl.getName();
					boolean isFileSharable = false;
					for(String str : filesList) {
						logger.info("filesPath >>>>>>>>>>>>>>>>>>>.====================== {},==========, {}",str, flpath);
						if(flpath.contains(str)) isFileSharable = true;
					}
					
					if(isFileSharable && fl.isFile()) {
						logger.info("isFileSharable .====================== {}",fl.getName().toString());
						String uuid = generateRandomUUID();
						fileMap.put(uuid, flpath);
						builder.append("<a href=\"http://"+iphost+":"+serverPort+"/downloadFile?uuid="+uuid+"\"target=\"_blank\">"+flpath+"</a>");
						builder.append("</br>");
					}
				}
			}
			builder.append("</br><body></HTML>");
			logger.info("Builder.====================== {}",builder.toString());
		} catch(Exception e) {
			logger.info("Builder.====================== {}",builder.toString());
			e.printStackTrace();
		}
		return builder.toString();
	}
	
	@Autowired
	private ServletContext servletContext;
	
	@GetMapping(value="downloadFile", produces = "application/zip")
	public synchronized void zipDownload(@RequestParam Map<String, String> map, HttpServletResponse response) {
		synchronized(this) {
			try {
				String uuid = map.get("uuid");
				String fileAbsoluteName = fileMap.get(uuid);
				File file = new File(fileAbsoluteName);
				response.setStatus(HttpServletResponse.SC_OK);
				response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getName()+"\"");
				
				MediaType mediaType = getMediaTypeForFileName(servletContext, file.getName());
				response.setContentType(mediaType.getType());
				response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+file.getName());
				response.setContentLength((int)file.length());
				
				BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
				BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());
				
				byte[] buffer = new byte[1024];
				int byteRead = 0;
				while((byteRead =inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, byteRead);
				}
				outStream.flush();
				inStream.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static MediaType getMediaTypeForFileName(ServletContext servletContext, String fileName) {
		String mineType = servletContext.getMimeType(fileName);
		try {
			MediaType mediaType = MediaType.parseMediaType(mineType);
			return MediaType.APPLICATION_OCTET_STREAM;
		} catch(Exception e) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}
}


