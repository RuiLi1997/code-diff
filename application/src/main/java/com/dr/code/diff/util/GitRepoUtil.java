package com.dr.code.diff.util;

import com.dr.common.errorcode.BizCode;
import com.dr.common.exception.BizException;
import com.dr.common.log.LoggerUtil;
import com.dr.common.utils.file.FileUtil;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @ProjectName: code-diff-parent
 * @Package: com.dr.code.diff.util
 * @Description: java类作用描述
 * @Author: duanrui
 * @CreateDate: 2021/4/5 11:16
 * @Version: 1.0
 * <p>
 * Copyright: Copyright (c) 2021
 */
@Slf4j
public class GitRepoUtil {


    /**
     * 获取本地仓库
     *
     * @param codePath
     * @return
     */
    public static Git getGit(String codePath) {
        Git git = null;
        try {
            File root = new File(codePath);
            git = Git.open(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return git;
    }


    /**
     * 将代码转成树状
     *
     * @param repository
     * @param commit
     * @return
     */

    public static AbstractTreeIterator prepareTreeParserByRevCommit(Repository repository, RevCommit commit) {
        try (RevWalk walk = new RevWalk(repository)){
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repository.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }
            walk.dispose();
            return oldTreeParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将代码转成树状
     * @param repository
     * @param commitId
     * @return
     */
    public static AbstractTreeIterator prepareTreeParserByStringId(Repository repository, String commitId) {
        try (RevWalk walk = new RevWalk(repository)){
            RevCommit commit = walk.parseCommit(repository.resolve(commitId));
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repository.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }
            walk.dispose();
            return oldTreeParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
