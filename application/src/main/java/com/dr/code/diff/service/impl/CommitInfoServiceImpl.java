package com.dr.code.diff.service.impl;

import com.dr.code.diff.service.CommitInfoService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CommitInfoServiceImpl implements CommitInfoService {
    @Override
    public Map<String, String> getCommitInfo(String repoPath) {
        Map<String, String> res = new HashMap<>();
        File root = new File(repoPath);
        Git git;
        try {
            git = Git.open(root);
            //获取最近两次的提交记录
            Iterable<RevCommit> commits = git.log().setMaxCount(10).call();
            for (RevCommit commit : commits) {
                res.put(commit.getId().getName(), commit.getFullMessage());
            }
            return res;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
