package com.dr.code.diff.dto;

import com.dr.code.diff.enums.CodeManageTypeEnum;
import lombok.Builder;
import lombok.Data;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.List;

/**
 * @ProjectName: code-diff-parent
 * @Package: com.dr.code.diff.dto
 * @Description: java类作用描述
 * @Author: duanrui
 * @CreateDate: 2021/4/5 10:10
 * @Version: 1.0
 * <p>
 * Copyright: Copyright (c) 2021
 */
@Data
public class VersionControlDto {


    /**
     *  本地仓库地址
     */
    private String RepoPath;


    /**
     * git现分支或tag版本
     * old commit 假如没有就默认最近两个版本
     */
    private String oldVersion;


    /**
     * 版本控制类型
     */
    private CodeManageTypeEnum codeManageTypeEnum;



    private List<DiffEntryDto> diffClasses;
}
