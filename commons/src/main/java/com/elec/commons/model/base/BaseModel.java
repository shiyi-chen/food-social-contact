package com.elec.commons.model.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BaseModel implements Serializable {
    private Integer id;
    private Date createDate;
    private Date updateDate;
    private int isValid;
}
