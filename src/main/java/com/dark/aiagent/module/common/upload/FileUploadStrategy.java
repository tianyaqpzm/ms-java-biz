package com.dark.aiagent.module.common.upload;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface FileUploadStrategy {
    /**
     * @return 当前处理器匹配的路径类型，如 "knowledge", "avatar" 等
     */
    String getUploadType();
    
    /**
     * 处理上传的核心业务逻辑
     * @param file 上传的文件对象
     * @param extraParams 其他辅助参数
     * @return 结果对象，供前端解析
     */
    Object handleUpload(MultipartFile file, Map<String, Object> extraParams);
}
