package com.rokin.celltracker;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class ImageUploader implements WebMvcConfigurer {

  private static final String FILE_PREFIX = "file:";
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  // private static final String PARTIAL_IMAGE_FOLDER_PATH = "resources" + File.separator + "static"
  // + File.separator + "image";
  private static final String PARTIAL_IMAGE_FOLDER_PATH = ".." + File.separator + ".."
      + File.separator + "resources" + File.separator + "static" + File.separator + "image";

  @Data
  public static class FileInfo {
    private String fileName;
    private long fileSize;
  }

  /**
   * Save uploaded image.
   * 
   * @param files
   *          image file list
   * @return
   */
  public List<FileInfo> saveUploadedFiles(List<MultipartFile> files) {
    List<FileInfo> fileInfoList = new ArrayList<>();
    if (files != null && !files.isEmpty()) {
      try {
        String iamgeFolderPath = getExecutionPath() + File.separator + PARTIAL_IMAGE_FOLDER_PATH
            + File.separator;
        for (MultipartFile inputFile : files) {
          if (!inputFile.isEmpty()) {
            String originalFilename = inputFile.getOriginalFilename();
            String storeFileName = System.currentTimeMillis() + "_" + originalFilename;
            File destinationFile = new File(iamgeFolderPath + storeFileName);
            if (destinationFile.createNewFile()) {
              inputFile.transferTo(destinationFile);
              FileInfo fileInfo = new FileInfo();
              fileInfo.setFileName("/" + PARTIAL_IMAGE_FOLDER_PATH + "/" + storeFileName);
              fileInfo.setFileSize(inputFile.getSize());
              fileInfoList.add(fileInfo);
            } else {
              log.error("file creation failed for absolute path: {}",
                  destinationFile.getAbsolutePath());
              log.error("file creation failed: {}{}", iamgeFolderPath, storeFileName);
            }
          }
        }
      } catch (IOException ex) {
        log.error(ex.getMessage(), ex);
      }
    }
    return fileInfoList;
  }

  private int indexOf(String s, Pattern pattern) {
    Matcher matcher = pattern.matcher(s);
    return matcher.find() ? matcher.start() : -1;
  }

  private String getExecutionPath() throws UnsupportedEncodingException {
    String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    int index = indexOf(absolutePath, Pattern.compile("(/[^/]*jar)"));
    if (index == -1) {
      index = absolutePath.lastIndexOf('/');
    }
    absolutePath = absolutePath.substring(0, index);
    index = absolutePath.lastIndexOf(FILE_PREFIX);
    if (index != -1) {
      absolutePath = absolutePath.substring(index);
      absolutePath = absolutePath.replaceAll(FILE_PREFIX, "");
    }
    absolutePath = URLDecoder.decode(absolutePath, "UTF-8");
    return absolutePath;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/" + ImageUploader.PARTIAL_IMAGE_FOLDER_PATH + "/**")
        .addResourceLocations(FILE_PREFIX + ImageUploader.PARTIAL_IMAGE_FOLDER_PATH + "/");
  }
}
