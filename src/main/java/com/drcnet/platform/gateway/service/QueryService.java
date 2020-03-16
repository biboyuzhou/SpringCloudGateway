package com.drcnet.platform.gateway.service;

import com.drcnet.platform.common.entity.DatasetSource;
import com.drcnet.platform.gateway.mapper.DatasetSourceMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2020/2/27 20:19
 * @Desc:
 **/
@Service
public class QueryService {

    @Resource
    private DatasetSourceMapper datasetSourceMapper;


    public DatasetSource queryDataSource(Long userId, Long datasetId, int dataType) {
        return datasetSourceMapper.getDataSource(userId, datasetId, dataType);
    }

    @Cacheable(cacheNames = "secretKey", key = "#secretKey", unless = "#result != null")
    public Long getUserIdBySecretKey(String secretKey) {
        return datasetSourceMapper.getUserIdBySecretKey(secretKey);
    }

    public boolean isRightRelation(String fileId, Long datasetId) {
        Long resultId = datasetSourceMapper.getDatasetIdByFileId(fileId);
        return datasetId.equals(resultId);
    }
}
