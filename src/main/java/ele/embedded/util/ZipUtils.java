package ele.embedded.util;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
  public static void addFileToZip(ZipOutputStream zipOut, String content, String fileName) throws Exception {
    ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    zipOut.write(content.getBytes());
    zipOut.closeEntry();
  }
  public static void addFileToZip(ZipOutputStream zipOut, byte[] content, String fileName) throws IOException {
    ZipEntry entry = new ZipEntry(fileName);
    zipOut.putNextEntry(entry);
    zipOut.write(content);
    zipOut.closeEntry();
  }
}
