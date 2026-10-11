package com.ssafy.backend.post.service;

import com.ssafy.backend.global.error.BusinessException;
import com.ssafy.backend.global.error.CommonErrorCode;
import com.ssafy.backend.post.dto.PostClosureResponse;
import com.ssafy.backend.post.entity.CloseReason;
import com.ssafy.backend.post.repository.PostLifecycleRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PostClosureService {
    private final PostMutationGuard guard;
    private final PostLifecycleRepository posts;
    private final TransactionTemplate transaction;

    public PostClosureService(
            PostMutationGuard guard,
            PostLifecycleRepository posts,
            PlatformTransactionManager manager) {
        this.guard = guard;
        this.posts = posts;
        this.transaction = new TransactionTemplate(manager);
        transaction.setTimeout(30);
    }

    public PostClosureResponse close(long postId, long memberId, long version, CloseReason reason) {
        try {
            return transaction.execute(
                    status -> {
                        guard.requireEditable(postId, memberId, version, true);
                        return posts.close(postId, version, reason);
                    });
        } catch (DataAccessException | TransactionException ex) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
