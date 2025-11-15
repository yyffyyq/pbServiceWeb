package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.DutyConfig;
import generator.service.DutyConfigService;
import generator.mapper.DutyConfigMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【duty_config(值班配置表)】的数据库操作Service实现
* @createDate 2025-11-15 14:40:03
*/
@Service
public class DutyConfigServiceImpl extends ServiceImpl<DutyConfigMapper, DutyConfig>
    implements DutyConfigService{

}




