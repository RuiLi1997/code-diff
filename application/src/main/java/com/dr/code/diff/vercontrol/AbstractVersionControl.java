package com.dr.code.diff.vercontrol;

import com.alibaba.fastjson.JSON;
import com.dr.code.diff.dto.ClassInfoResult;
import com.dr.code.diff.dto.DiffEntryDto;
import com.dr.code.diff.dto.MethodInfoResult;
import com.dr.code.diff.dto.VersionControlDto;
import com.dr.code.diff.enums.CodeManageTypeEnum;
import com.dr.code.diff.util.MethodParserUtils;
import com.dr.common.log.LoggerUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @ProjectName: code-diff-parent
 * @Package: com.dr.code.diff.vercontrol
 * @Description: 代码差异获取流程类定义
 * @Author: duanrui
 * @CreateDate: 2021/4/5 9:56
 * @Version: 1.0
 * <p>
 * Copyright: Copyright (c) 2021
 */
@Data
@Slf4j
public abstract class AbstractVersionControl {

    protected VersionControlDto versionControlDto;


    @Resource(name = "asyncExecutor")
    private Executor executor;


    /**
     * 执行handler
     *
     * @return
     */
    public List<ClassInfoResult> handler(VersionControlDto versionControlDto) {
        this.versionControlDto = versionControlDto;
        getDiffCodeClasses();
        return getDiffCodeMethods();
    }

    public abstract void getDiffCodeClasses();

    /**
     * 获取操作类型
     */
    public abstract CodeManageTypeEnum getType();


    public List<ClassInfoResult> getDiffCodeMethods() {
        if(CollectionUtils.isEmpty(versionControlDto.getDiffClasses())){
            return null;
        }
        LoggerUtil.info(log,"需要对比的差异类数",versionControlDto.getDiffClasses().size());
        List<CompletableFuture<ClassInfoResult>> priceFuture = versionControlDto.getDiffClasses().stream()
                .map(this::getClassMethods)
                .collect(Collectors.toList());
        CompletableFuture.allOf(priceFuture.toArray(new CompletableFuture[0])).join();
        List<ClassInfoResult> list = priceFuture.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(list)){
            LoggerUtil.info(log,"计算出最终差异类数",list.size());
        }
        return list;
    }

    private CompletableFuture<ClassInfoResult> getClassMethods(DiffEntryDto diffEntry) {
        return CompletableFuture.supplyAsync(() -> {
            String className = diffEntry.getNewPath().split("\\.")[0].split("src/main/java/")[1];
            String moduleName = diffEntry.getNewPath().split("/")[0];
            //新增类和修改类
            return ClassInfoResult.builder()
                    .classFile(className)
                    .type(DiffEntry.ChangeType.ADD.name())
                    .moduleName(moduleName)
                    .lines(diffEntry.getLines())
                    .build();
        }, executor);
    }

}
