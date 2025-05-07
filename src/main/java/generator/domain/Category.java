package generator.domain;


import java.io.Serializable;

import java.time.LocalDateTime;


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

    private LocalDateTime createTime;
    /**
     * 更新时间
     */

    private LocalDateTime updateTime;
    /**
     * 删除标记（0:不可用 1:可用）
     */

    private Integer isDeleted;

    /**
     * 分类id
     */
    public void setId(Long id){
        this.id = id;
    }

    /**
     * 分类名称
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     *
     */
    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    /**
     * 父分类id
     */
    public void setParentId(Long parentId){
        this.parentId = parentId;
    }

    /**
     * 是否显示[0-不显示，1显示]
     */
    public void setStatus(Integer status){
        this.status = status;
    }

    /**
     * 排序
     */
    public void setOrderNum(Integer orderNum){
        this.orderNum = orderNum;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(LocalDateTime createTime){
        this.createTime = createTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime){
        this.updateTime = updateTime;
    }

    /**
     * 删除标记（0:不可用 1:可用）
     */
    public void setIsDeleted(Integer isDeleted){
        this.isDeleted = isDeleted;
    }


    /**
     * 分类id
     */
    public Long getId(){
        return this.id;
    }

    /**
     * 分类名称
     */
    public String getName(){
        return this.name;
    }

    /**
     *
     */
    public String getImageUrl(){
        return this.imageUrl;
    }

    /**
     * 父分类id
     */
    public Long getParentId(){
        return this.parentId;
    }

    /**
     * 是否显示[0-不显示，1显示]
     */
    public Integer getStatus(){
        return this.status;
    }

    /**
     * 排序
     */
    public Integer getOrderNum(){
        return this.orderNum;
    }

    /**
     * 创建时间
     */
    public LocalDateTime getCreateTime(){
        return this.createTime;
    }

    /**
     * 更新时间
     */
    public LocalDateTime getUpdateTime(){
        return this.updateTime;
    }

    /**
     * 删除标记（0:不可用 1:可用）
     */
    private Integer getIsDeleted(){
        return this.isDeleted;
    }

}
