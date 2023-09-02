package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public AlphaService() {
//        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
//        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy() {
//        System.out.println("销毁AlphaService");
    }

    public String find() {
        return alphaDao.select();
    }

    // 通过注解管理事务，使save1成为一个整体，有任何错误都将回滚
    // 隔离级别设置为“只读取已提交的数据”
    /* propagation为传播机制
        A调B，对于B来说，A为当前事务（外部事物）
       1. REQUIRED: 如果存在当前事务，那就加入该事务；如果不存在，就创建新事务；
       2. REQUIRED_NEW：创建一个新事务，并且暂停当前事务；
       3. NESTED：如果当前存在事务，则嵌套在该事物中执行（可独立地提交和回滚）；否则和REQUIRED一样
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1(){
        // 新增用户
        User user = new User();
        user.setUsername("Zhao jing");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setEmail("zhaojing@qq.com");
        user.setPassword(CommunityUtil.md5("123123"+user.getSalt()));
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("赵靖最帅？");
        post.setContent("不是吧，易烊千玺也挺帅啊！");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        // 人为错误，将回滚
        Integer.valueOf("abc");

        return "ok";
    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setEmail("beta@qq.com");
                user.setPassword(CommunityUtil.md5("123123"+user.getSalt()));
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("赵靖不帅？");
                post.setContent("对啊，易烊千玺最帅啊！");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                // 人为错误，将回滚
                Integer.valueOf("abc");

                return "ok";
            }
        });
    }

}
