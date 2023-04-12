package edu.zuel.picture.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("pictures")
public class Picture implements Serializable {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField("url")
    private String url;
    @TableField("results")
    private String results;
    @TableField("deleteId")
    private int deleteId;

    public int getId() {
        return id;
    }
    public String getUrl() {
        return url;
    }
    public String getResults() {
        return results;
    }
    public int getDeleteId() {
        return deleteId;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setResults(String results) {
        this.results = results;
    }
    public void setDeleteId(int deleteId) {
        this.deleteId = deleteId;
    }



    @Override
    public String toString() {
        return "Picture{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", results='" + results + '\'' +
                ", deleteId=" + deleteId +
                '}';
    }
}
