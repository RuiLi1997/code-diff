package com.dr.code.diff.service;

import java.util.Map;

/**
 *  获取git信息
 */
public interface CommitInfoService {
    Map<String, String> getCommitInfo(String repoPath);
}
