package edu.zuel.picture.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiniu.common.QiniuException;
import com.qiniu.util.Auth;
import edu.zuel.picture.entity.Picture;
import edu.zuel.picture.mapper.PictureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@org.springframework.stereotype.Service
@SuppressWarnings({"all"})
public class Service {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PictureMapper pictureMapper;
    //七牛云秘钥AK
    static String accessKey = "USfijONzQnXjRCNg8Ow2njvYlFBlhqrET_PJ-mAD";
    //七牛云秘钥SK
    static String secretKey = "8tIoDaAJGhu50AMBlHmE35OKiOvDgKpTzUf6xAkQ";
    //七牛云空间名称
    static String bucket = "analysis-results-location";

    //七牛云下载功能,返回图片外链
    public void downloadFromQiniu(String fileName) throws QiniuException {
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
        HttpUtil.downloadFile(finalUrl, FileUtil.file(System.getProperty("user.dir") + "/src/main/resources/static/images/" + fileName));
    }

    public String getUrlFromQiniuyun(String fileName) throws QiniuException {
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
        return finalUrl;
    }

    //插入数据到mysql
    public boolean insertTomysql(String url,String results,int deleteId) {
        try{
            String sql = "select * from picture where deleteId = 0";
            String key = "picture:" + sql;
            Picture picture = new Picture();
            picture.setUrl(url);
            picture.setResults(results);
            picture.setDeleteId(deleteId);
            int changedLines = pictureMapper.insert(picture);
            //将更新后的记录保存回数据库中
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("deleteId", 0);
            List<Picture> pictures = pictureMapper.selectList(queryWrapper);
            redisTemplate.opsForValue().set(key, pictures);
            System.out.println(changedLines);
            System.out.println(picture);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    //查询所有记录，返回list(先在redis中查询)
    public List<Picture> getAllPictures() {
        String sql = "select * from picture where deleteId = 0";
        String key = "picture:" + sql;
        List<Picture> pictures = (List<Picture>) redisTemplate.opsForValue().get(key);
        if (pictures == null) {
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("deleteId", 0);
            pictures = pictureMapper.selectList(queryWrapper);
            redisTemplate.opsForValue().set(key, pictures);
        }
        return pictures;
    }

    //逻辑删除某条记录,将指定记录的deleteId更新为1,并更新mysql和redis
    //这里更新redis的目的是为了防止前端删除记录后，记录还在上面的情况出现
    public void logicDelete(int id) {
        String sql = "select * from picture where deleteId = 0";
        String key = "picture:" + sql;
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        Picture picture = pictureMapper.selectOne(queryWrapper);
        if (picture != null) {
            picture.setDeleteId(1);
            //将更新后的记录保存回数据库中
            pictureMapper.updateById(picture);
            QueryWrapper<Picture> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("deleteId", 0);
            List<Picture> pictures = pictureMapper.selectList(queryWrapper1);
            redisTemplate.opsForValue().set(key, pictures);
        }
    }

}



























