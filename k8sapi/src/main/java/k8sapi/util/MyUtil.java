package k8sapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class MyUtil {
    private static Logger log = LoggerFactory.getLogger(MyUtil.class);

    public static final String DEFAULT_NAMESPACE = "default";

    //Read the whole file(Delete after read completion)
    public static String readWholeFile(String path){
        String encoding = "UTF-8";
        File file = new File(path);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }finally {
            deleteFile(path);
        }
    }

    //Delete the temporary file
    private static void deleteFile(String filePath){
        try {
            File file = new File(filePath);
            if (file.delete()) {
//                log.info(file.getName() + " is deleted");
            } else {
                log.info(String.format("Delete file [%s] failed!", file.getName()));
            }
        } catch (Exception e) {
            log.info("Exception occured when delete file");
            e.printStackTrace();
        }
    }
}
