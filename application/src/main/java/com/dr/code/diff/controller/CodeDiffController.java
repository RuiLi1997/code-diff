package com.dr.code.diff.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dr.code.diff.dto.ClassInfoResult;
import com.dr.code.diff.dto.DiffEntryDto;
import com.dr.code.diff.dto.DiffMethodParams;
import com.dr.code.diff.enums.CodeManageTypeEnum;
import com.dr.code.diff.service.CodeDiffService;
import com.dr.code.diff.service.CommitInfoService;
import com.dr.code.diff.vo.result.CodeDiffResultVO;
import com.dr.common.response.UniqueApoResponse;
import com.dr.common.utils.mapper.OrikaMapperUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author rui.duan
 * @version 1.0
 * @className UserController
 * @description 用户管理
 * @date 2019/11/20 6:25 下午
 */
@RestController
@Api(value = "/api/code/diff", tags = "差异代码模块")
@RequestMapping("/api/code/diff")
public class CodeDiffController {

    @Autowired
    private CodeDiffService codeDiffService;

    @Autowired
    private CommitInfoService commitInfoService;

    @ApiOperation("git获取差异代码")
    @RequestMapping(value = "diff-info", method = RequestMethod.GET)
    public UniqueApoResponse<List<CodeDiffResultVO>> getGitList(
            @ApiParam(required = true, name = "gitPath", value = "git本地地址")
            @RequestParam(value = "gitPath") String gitPath,
            @ApiParam(name = "oldVersion", value = "计算增量代码所需要的对比地址")
            @RequestParam(value = "oldVersion", defaultValue = "empty") String oldVersion){
        DiffMethodParams diffMethodParams = DiffMethodParams.builder()
                .repoPath(StringUtils.trim(gitPath))
                .oldVersion(StringUtils.trim(oldVersion))
                .codeManageTypeEnum(CodeManageTypeEnum.GIT)
                .build();
        List<ClassInfoResult> diffCodeList = codeDiffService.getDiffCode(diffMethodParams);
        List<CodeDiffResultVO> list = OrikaMapperUtils.mapList(diffCodeList,ClassInfoResult.class, CodeDiffResultVO.class);
        return new UniqueApoResponse<List<CodeDiffResultVO>>().success(list, JSON.toJSONString(list,SerializerFeature.WriteNullListAsEmpty));
    }

    @ApiOperation("git获取版本信息")
    @RequestMapping(value = "commit-id", method = RequestMethod.GET)
    public Map<String, String> commitInfo(
            @ApiParam(required = true, name = "gitPath", value = "git本地地址")
            @RequestParam(value = "gitPath") String gitPath){
        return commitInfoService.getCommitInfo(gitPath);
    }

}
