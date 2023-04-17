# pictureRegnitionBackend
简要介绍：

图片识别系统Springboot后端代码

功能描述：

这是图片识别系统的后端代码，使用了Springboot框架，用户在系统前端界面提交图片后，后端接收前端请求，并对redis和mysql数据库进行操作（完成前端显示识别图片、删除或查看历史识别
记录的请求），将图片上传至fastAPI端，fastAPI端使用Yolov5模型对上传图片进行实体识别，上传识别图片至七牛云，返回识别结果到Springboot后端，后端再将识别结果返回到前端，并获取
七牛云图片外链并返回到前端。
