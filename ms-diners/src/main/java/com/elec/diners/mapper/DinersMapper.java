package com.elec.diners.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elec.commons.model.pojo.Diners;
import com.elec.commons.model.vo.ShortDinerInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface DinersMapper extends BaseMapper<Diners> {

    // 根据 ID 集合查询多个食客信息
    @Select("<script> " +
            " select id, nickname, avatar_url from t_diners " +
            " where is_valid = 1 and id in " +
            " <foreach item=\"id\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\"> " +
            "   #{id} " +
            " </foreach> " +
            " </script>")
    List<ShortDinerInfo> findByIds(@Param("ids") List<Integer> ids);
}
