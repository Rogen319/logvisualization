package k8sapi.util;

import java.io.*;

public class MyUtil {
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
            System.err.println("The OS does not support " + encoding);
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
//                System.out.println(file.getName() + " is deleted");
            } else {
                System.out.println(String.format("Delete file [%s] failed!", file.getName()));
            }
        } catch (Exception e) {
            System.out.println("Exception occured when delete file");
            e.printStackTrace();
        }
    }
}
