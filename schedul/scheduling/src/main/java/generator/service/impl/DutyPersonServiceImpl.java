package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.DutyPerson;
import generator.service.DutyPersonService;
import generator.mapper.DutyPersonMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【duty_person(值班人员表)】的数据库操作Service实现
* @createDate 2025-11-15 14:32:16
*/
@Service
public class DutyPersonServiceImpl extends ServiceImpl<DutyPersonMapper, DutyPerson>
    implements DutyPersonService{

}




