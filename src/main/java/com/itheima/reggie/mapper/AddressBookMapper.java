package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Date: 2022/11/23 06:47
 * @Auther: cold
 * @Description:
 */

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
