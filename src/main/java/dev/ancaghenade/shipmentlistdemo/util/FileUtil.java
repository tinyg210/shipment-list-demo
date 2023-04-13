package dev.ancaghenade.shipmentlistdemo.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {

  public static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
    File file = File.createTempFile("temp", null);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(multipartFile.getBytes());
    }
    return file;
  }
}
