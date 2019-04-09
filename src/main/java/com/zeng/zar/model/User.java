package com.zeng.zar.model;

import com.zeng.zar.annotation.Column;
import com.zeng.zar.annotation.Id;
import com.zeng.zar.annotation.Table;
import com.zeng.zar.core.Model;
import com.zeng.zar.type.GenType;

@Table("t_user")
public class User extends Model{
    
    private static final long serialVersionUID = -3796031963997823317L;

    @Id(value="id", generate=GenType.UUID)
    private Integer id;
    
    @Column("user_name")
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
