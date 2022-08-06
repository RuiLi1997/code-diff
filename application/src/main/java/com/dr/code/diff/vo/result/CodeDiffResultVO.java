package com.dr.code.diff.vo.result;

import com.dr.code.diff.dto.ChangeLine;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.List;

/**
 * @date:2021/1/9
 * @className:CodeDiffResultVO
 * @author:Administrator
 * @description: 差异代码结果集
 */
@Data
@ApiModel("差异代码结果集")
public class CodeDiffResultVO {


    /**
     * 文件包名
     */
    @ApiModelProperty(name = "newPath", value = "变更行信息")
    protected String newPath;

    /**
     * 文件变更类型
     */
    @ApiModelProperty(name = "lines", value = "变更行信息")
    private DiffEntry.ChangeType changeType;


    /**
     * 变更行
     */
    @ApiModelProperty(name = "lines", value = "变更行信息")
    private List<ChangeLineVO> lines;

}
