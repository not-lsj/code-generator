package com.genService.serviceImpl.${module_name};

import com.alibaba.dubbo.config.annotation.Service;
import com.genService.serviceImpl.BaseServiceImpl;

import pojo.${pojo_name};
import services.${module_name}.${pojo_name}Service;


@Service
public class ${pojo_name}ServiceImpl extends BaseServiceImpl<${pojo_name}> implements ${pojo_name}Service{

}
