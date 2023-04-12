package edu.zuel.picture.demo;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 架构师小跟班 www.jiagou1216.com
 * 官方API说明：https://developer.qiniu.com/kodo/sdk/1239/java#1
 */
//@SpringBootApplication
public class DemoApplication {
    //七牛云秘钥AK
    static String accessKey = "USfijONzQnXjRCNg8Ow2njvYlFBlhqrET_PJ-mAD";
    //七牛云秘钥SK
    static String secretKey = "8tIoDaAJGhu50AMBlHmE35OKiOvDgKpTzUf6xAkQ";
    //七牛云空间名称
    static String bucket = "analysis-results-location";

//    public static void main(String[] args) {
//        SpringApplication.run(DemoApplication.class, args);
//        list();
//    }

    /**
     * 列表
     */
    public static void list() {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Region.huanan());
        //...其他参数参考类注释
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);

        //文件名前缀
        String prefix = "";
        //每次迭代的长度限制，最大1000，推荐值 1000
        int limit = 10;
        //指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";

        //列举空间文件列表
        BucketManager.FileListIterator fileListIterator = bucketManager.createFileListIterator(bucket, prefix, limit, delimiter);
        while (fileListIterator.hasNext()) {
            //处理获取的file list结果
            FileInfo[] items = fileListIterator.next();
            for (FileInfo item : items) {
                download(item.key);//下载
            }
        }
    }

    /**
     * 下载
     */
    public static void download(String fileName) {
        String domainOfBucket = "http://rrk5wibmd.hn-bkt.clouddn.com";
        String encodedFileName = null;
        try {
            encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);

        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);
        HttpUtil.downloadFile(finalUrl, FileUtil.file("G:/IdeaProjects/picture/src/main/resources/static/images/" + fileName));
    }
}
