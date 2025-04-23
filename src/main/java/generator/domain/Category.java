package generator.domain;


import java.io.Serializable;

import java.util.Date;

/**
* 商品分类
* @TableName category
*/
public class Category implements Serializable {


    private Long id;
    /**
    * 分类名称
    */

    private String name;
    /**
    * 
    */

    private String imageUrl;
    /**
    * 父分类id
    */

    private Long parentId;
    /**
    * 是否显示[0-不显示，1显示]
    */

    private Integer status;
    /**
    * 排序
    */

    private Integer orderNum;
    /**
    * 创建时间
    */

    private Date createTime;
    /**
    * 更新时间
    */

    private Date updateTime;
    /**
    * 删除标记（0:不可用 1:可用）
    */

    private Integer isDeleted;

    /**
    * 分类id
    */
    private void setId(Long id){
    this.id = id;
    }

    /**
    * 分类名称
    */
    private void setName(String name){
    this.name = name;
    }

    /**
    * 
    */
    private void setImageUrl(String imageUrl){
    this.imageUrl = imageUrl;
    }

    /**
    * 父分类id
    */
    private void setParentId(Long parentId){
    this.parentId = parentId;
    }

    /**
    * 是否显示[0-不显示，1显示]
    */
    private void setStatus(Integer status){
    this.status = status;
    }

    /**
    * 排序
    */
    private void setOrderNum(Integer orderNum){
    this.orderNum = orderNum;
    }

    /**
    * 创建时间
    */
    private void setCreateTime(Date createTime){
    this.createTime = createTime;
    }

    /**
    * 更新时间
    */
    private void setUpdateTime(Date updateTime){
    this.updateTime = updateTime;
    }

    /**
    * 删除标记（0:不可用 1:可用）
    */
    private void setIsDeleted(Integer isDeleted){
    this.isDeleted = isDeleted;
    }


    /**
    * 分类id
    */
    private Long getId(){
    return this.id;
    }

    /**
    * 分类名称
    */
    private String getName(){
    return this.name;
    }

    /**
    * 
    */
    private String getImageUrl(){
    return this.imageUrl;
    }

    /**
    * 父分类id
    */
    private Long getParentId(){
    return this.parentId;
    }

    /**
    * 是否显示[0-不显示，1显示]
    */
    private Integer getStatus(){
    return this.status;
    }

    /**
    * 排序
    */
    private Integer getOrderNum(){
    return this.orderNum;
    }

    /**
    * 创建时间
    */
    private Date getCreateTime(){
    return this.createTime;
    }

    /**
    * 更新时间
    */
    private Date getUpdateTime(){
    return this.updateTime;
    }

    /**
    * 删除标记（0:不可用 1:可用）
    */
    private Integer getIsDeleted(){
    return this.isDeleted;
    }

}
