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
     * @return
     */
    public List<DiffEntryDto> handler(VersionControlDto versionControlDto) {
        this.versionControlDto = versionControlDto;
        getDiffCodeClasses();
        return versionControlDto.getDiffClasses();
    }

    public abstract void getDiffCodeClasses();

    /**
     * 获取操作类型
     */
    public abstract CodeManageTypeEnum getType();





}
