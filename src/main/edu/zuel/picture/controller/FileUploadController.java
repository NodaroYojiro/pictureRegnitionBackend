package edu.zuel.picture.controller;

import edu.zuel.picture.entity.Picture;
import edu.zuel.picture.utils.QiniuUtils;
import edu.zuel.picture.service.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@CrossOrigin
@ComponentScan
public class FileUploadController {
    @Autowired
    private QiniuUtils qiniuUtils;
    @Autowired
    private Service service;

    public static void remove(File file) {
        File[] files = file.listFiles();//将file子目录及子文件放进文件数组
        if (files != null) {//如果包含文件进行删除操作
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {//删除子文件
                    files[i].delete();
                } else if (files[i].isDirectory()) {//通过递归方法删除子目录的文件
                    remove(files[i]);
                }
                files[i].delete();//删除子目录
            }
        }
    }

    //处理前端的查看所有记录请求
    @GetMapping("/records")
    public List<Picture> getAllPictures() {
        return service.getAllPictures();
    }

    //处理前端删除某记录的请求
    @PutMapping("/delete/{id}")
    public void delete(@PathVariable("id") int id) {
        service.logicDelete(id);
    }
    @PostMapping("/upload")
    public String upload(MultipartFile photo, HttpServletRequest request) throws  IOException {
        //输出文件名字、类型、路径
        System.out.println(photo.getOriginalFilename());
        System.out.println(photo.getContentType());
        String path = request.getServletContext().getRealPath("/upload/");
        System.out.println(path);
        //清除static文件夹下的所有文件
        File baseFile = new File(System.getProperty("user.dir") + "/src/main/resources/static");
        remove(baseFile);
        //保存文件到本地
        saveFile(photo);
        //上传文件到fastapi端
        String imageUrl = System.getProperty("user.dir") + "/src/main/resources/static/"+photo.getOriginalFilename();
        byte[] imageBytes = Files.readAllBytes(Paths.get(imageUrl));
        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            public String getFilename() {
                return photo.getOriginalFilename();
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange("http://47.120.34.242:8001//analysisResult", org.springframework.http.HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        if (responseEntity.getStatusCodeValue() == 200) {
            //获取返回的识别数据
            Map<String, Object> responseData = responseEntity.getBody();
            System.out.println(responseData.get("results_json").getClass());
            System.out.println(responseData.get("results_json"));
            List resultsList = (List) responseData.get("results_json");
            System.out.println(resultsList.getClass());
            List<String> namesList = new ArrayList<>();
            List<Double> confidenceList = new ArrayList<>();
            for (Object obj : resultsList) {
                Map<String, Object> result = (Map<String, Object>) obj;
                String name = (String) result.get("name");
                Double confidence = (Double) result.get("confidence");
                namesList.add(name);
                confidenceList.add(confidence);
            }
            String result = "";
            for (int i = 0; i < namesList.size(); i++) {
                result = result + namesList.get(i) + ":" + confidenceList.get(i) + " ";
            }
            System.out.println(result);
            String analysis_picname = (String) responseData.get("analysis_picname");
            System.out.println(responseData.get("analysis_picname"));
            //获取七牛云里的识别图片url
            System.out.println(responseData.get("image_file"));
            String url = service.getUrlFromQiniuyun(analysis_picname);
            System.out.println(url);
            //将识别数据存入数据库
            service.insertTomysql(url,result,0);
            return url;
        }
        return "fail";
    }
    public void saveFile(MultipartFile photo) throws IOException {
        String location = System.getProperty("user.dir") + "/src/main/resources/static/"+photo.getOriginalFilename();
        File file = new File(location);
        photo.transferTo(file);
    }
}






















