import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class parser{
    public static ArrayList<String> topicparser(String name) {
        ArrayList<String> queries = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> bodies = new ArrayList<String>();
        try {
            FileReader file = new FileReader(name);
            BufferedReader bf = new BufferedReader(file);
            String str;
            while ((str = bf.readLine()) != null) {
                if (str.contains("<title>")) {
                    titles.add(str.substring(8).trim());
                } else if (str.contains("<desc>")) {
                    StringBuilder temp = new StringBuilder();
                    str = bf.readLine();
                    while (str.trim().length() != 0) {
                        temp.append(str.trim());
                        temp.append(" ");
                        str = bf.readLine();
                    }
                    bodies.add(temp.toString().trim());
                }

            }
            bf.close();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        titles.toArray();
        bodies.toArray();
        for (int i = 0; i < titles.size(); i++) {
            queries.add(titles.get(i) + " " + bodies.get(i));
        }
        //System.out.print(queries);
        //System.out.print("\n");
        //System.out.print(queries.size());
        return queries;
    }
    /*
    public static ArrayList<String> dataparser(String dirpath) {
        ArrayList<String> listFileName = new ArrayList<String>();
        File file = new File(dirpath);
        File [] files = file.listFiles();
        String [] names = file.list();
        while(names != null){
            String [] completNames = new String[names.length];
            for(int i=0;i<names.length;i++){
                completNames[i] = dirpath + names[i];
            }
            listFileName.addAll(Arrays.asList(completNames));
        }
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        dataparser.folderMethod2(file2.getAbsolutePath());
                    } else {
                        System.out.println("文件:" + file2.getAbsolutePath());
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        public static void getAllFileName(String dirpath, ArrayList<String> listFileName){
            File file = new File(dirpath);


            for(File a:files){
                if(a.isDirectory()){//如果文件夹下有子文件夹，获取子文件夹下的所有文件全路径。
                    getAllFileName(a.getAbsolutePath()+"\\",listFileName);
                }
            }
        }

        public static void main(String[] args){
            ArrayList<String> listFileName = new ArrayList<String>();
            getAllFileName("D:\\testfiles\\",listFileName);
            for(String name:listFileName){
                if(name.contains(".txt")||name.contains(".properties")){
                    System.out.println(name);
                }
            }
        }
    }
    */

}
