package com.drcnet.platform.gateway.mapper;

import com.drcnet.platform.common.entity.DatasetSource;
import org.apache.ibatis.annotations.Param;

/**
* Created by Mybatis Generator on 2020/02/26
*/
public interface DatasetSourceMapper {

    /**
     * 根据userId、datasetId、dataType获取datasetSource记录
     * @param userId
     * @param datasetId
     * @param dataType
     * @return
     */
    DatasetSource getDataSource(@Param("userId") Long userId, @Param("datasetId") Long datasetId, @Param("dataType") int dataType);

    /**
     * 根据secretKey获取userId
     * @param secretKey
     * @return
     */
    Long getUserIdBySecretKey(@Param("secretKey") String secretKey);

    /**
     * 根据文件id获取块数据id
     * @param fileId
     * @return
     */
    Long getDatasetIdByFileId(@Param("fileId") String fileId);
}