package com.dr.code.diff.vercontrol.git;

import com.alibaba.fastjson.JSON;
import com.dr.code.diff.config.CustomizeConfig;
import com.dr.code.diff.dto.ChangeLine;
import com.dr.code.diff.dto.DiffEntryDto;
import com.dr.code.diff.enums.CodeManageTypeEnum;
import com.dr.code.diff.util.GitRepoUtil;
import com.dr.code.diff.util.PathUtils;
import com.dr.code.diff.vercontrol.AbstractVersionControl;
import com.dr.common.errorcode.BizCode;
import com.dr.common.exception.BizException;
import com.dr.common.log.LoggerUtil;
import com.dr.common.utils.mapper.OrikaMapperUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dr.code.diff.util.GitRepoUtil.getGit;

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
@Component
@Slf4j
public class GitAbstractVersionControl extends AbstractVersionControl {

    @Autowired
    private CustomizeConfig customizeConfig;


    /**
     * 获取操作类型
     */
    @Override
    public CodeManageTypeEnum getType() {
        return CodeManageTypeEnum.GIT;
    }

    @Override
    public void getDiffCodeClasses() {
        try {
            Git git = getGit(super.versionControlDto.getRepoPath());
            Repository repository = git.getRepository();
            List<RevCommit> commitList = new ArrayList<>();
            Iterable<RevCommit> commits = git.log().setMaxCount(2).call();
            AbstractTreeIterator baseTree;
            AbstractTreeIterator nowTree;
            for(RevCommit commit:commits){
                commitList.add(commit);
            }
            if(super.versionControlDto.getOldVersion().equals("empty")) {
                // 假如没有old version的id，默认最近两个版本的差异
                // 获取上一个版本的数据
                baseTree = GitRepoUtil.prepareTreeParserByRevCommit(repository, commitList.get(1));
            } else {
                // 获取目标commit的数据
                baseTree = GitRepoUtil.prepareTreeParserByStringId(repository, super.versionControlDto.getOldVersion());
            }

            // 默认是最新版本的(容器内运行版本)
           nowTree = GitRepoUtil.prepareTreeParserByRevCommit(repository, commitList.get(0));

            //获取两个版本之间的差异代码
            List<DiffEntry> diff = null;
            diff = git.diff().setOldTree(baseTree).setNewTree(nowTree).setShowNameAndStatusOnly(true).call();
            //过滤出有效的差异代码
            Collection<DiffEntry> validDiffList = diff.stream()
                    // 只计算java文件
                    .filter(e -> e.getNewPath().endsWith(".java"))
                    // 排除测试文件
                    .filter(e -> e.getNewPath().contains("src/main/java"))
                    // 只计算新增和变更文件
                    .filter(e -> DiffEntry.ChangeType.ADD.equals(e.getChangeType()) || DiffEntry.ChangeType.MODIFY.equals(e.getChangeType()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(validDiffList)) {
                LoggerUtil.info(log, "没有需要对比的类");
                return;
            }
            List<DiffEntryDto> diffEntries = OrikaMapperUtils.mapList(validDiffList, DiffEntry.class, DiffEntryDto.class);

            Map<String, DiffEntryDto> diffMap = diffEntries.stream().collect(Collectors.toMap(DiffEntryDto::getNewPath, Function.identity()));
            LoggerUtil.info(log, "需要对比的差异类为：", JSON.toJSON(diffEntries));
            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setContext(0);
            //此处是获取变更行，有群友需求新增行或变更行要在类中打标记，此处忽略删除行
            for (DiffEntry diffClass : validDiffList) {
                //获取变更行
                EditList edits = diffFormatter.toFileHeader(diffClass).toEditList();
                if (CollectionUtils.isEmpty(edits)) {
                    continue;
                }
                //获取出新增行和变更行
                List<Edit> list = edits.stream().filter(e -> Edit.Type.INSERT.equals(e.getType()) || Edit.Type.REPLACE.equals(e.getType())).collect(Collectors.toList());
                List<ChangeLine> lines = new ArrayList<>(list.size());
                list.forEach(
                        edit -> {
                            ChangeLine build = ChangeLine.builder().startLineNum(edit.getBeginB()).endLineNum(edit.getEndB()).type(edit.getType().name()).build();
                            lines.add(build);
                        }
                );
                if (diffMap.containsKey(diffClass.getNewPath())) {
                    DiffEntryDto diffEntryDto = diffMap.get(diffClass.getNewPath());
                    diffEntryDto.setLines(lines);
                }
            }
            //设置变更行
            super.versionControlDto.setDiffClasses(new ArrayList<DiffEntryDto>(diffMap.values()));
        } catch (IOException |
                GitAPIException e) {
            e.printStackTrace();
            throw new BizException(BizCode.GET_DIFF_CLASS_ERROR);
        }

    }
}
