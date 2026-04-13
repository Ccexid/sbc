package me.link.bootstrap.modules.system.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.OperateLogPO;
import me.link.bootstrap.modules.system.infrastructure.persistence.mapper.OperateLogMapper;
import me.link.bootstrap.modules.system.application.service.OperateLogService;
import org.springframework.stereotype.Service;

@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLogPO> implements OperateLogService {
}
