package com.dark.aiagent.module.common.upload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "公共上传控制器", description = "统一下发上传接口，根据 type 路径自动路由不同业务策略")
@RestController
@RequestMapping("/rest/dark/v1/upload")
public class FileUploadController {

    private final Map<String, FileUploadStrategy> strategyMap = new HashMap<>();

    public FileUploadController(List<FileUploadStrategy> strategies) {
        for (FileUploadStrategy strategy : strategies) {
            strategyMap.put(strategy.getUploadType(), strategy);
        }
    }

    @Operation(summary = "按照类型路由上传业务业务 (e.g. knowledge)")
    @PostMapping("/{type}")
    public Object uploadFile(
            @PathVariable("type") String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam Map<String, Object> extraParams) {
        
        FileUploadStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported file upload type: " + type);
        }
        return strategy.handleUpload(file, extraParams);
    }
}
