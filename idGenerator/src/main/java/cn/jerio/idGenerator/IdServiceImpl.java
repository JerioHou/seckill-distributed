package cn.jerio.idGenerator;

import cn.jerio.idGenerator.service.IdService;
import com.alibaba.dubbo.config.annotation.Service;

/**
 * Created by Jerio on 2019/03/04
 */
@Service
public class IdServiceImpl implements IdService {

    private static final IdGenerator idGenerator = new IdGenerator(0,0);


    @Override
    public long nextId() {
        return idGenerator.nextId();
    }
}
