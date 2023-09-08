package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path="/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("headerMsg","您还没有选择图片！");
        }
        // 获取原始文件名及其后缀
        String fileName = headerImage.getOriginalFilename();  // 原始文件名
        String suffix = fileName.substring(fileName.lastIndexOf(".")); //从最后一个点往后截取
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("headerMsg","文件格式不正确");
        }
        // 为了避免同名覆盖，需要给每个头像生成不同的名字
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest); // 将当前文件内容写进目标文件
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }
        // 更新当前用户头像路径(web访问路径)
        // http://localhost:8080/communtiy/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        // 头像更新成功，返回首页
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String fileName, HttpServletResponse response){
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        // 响应图片
        response.setContentType("image/"+suffix);
        try (
                OutputStream os = response.getOutputStream();  // 输出流
                FileInputStream fis = new FileInputStream(fileName);  // 输入流，读图片后才能输出
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @LoginRequired
    @RequestMapping(path = "/modifyPassword", method = RequestMethod.POST)
    public String modifyPassword(String originalPassword, String newPassword1, String newPassword2, Model model){
        if(StringUtils.isBlank(originalPassword)){
            model.addAttribute("originPasswordMsg","密码不能为空！");
            System.out.println("密码不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword1)){
            model.addAttribute("newPasswordMsg1","密码不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword2)){
            model.addAttribute("newPasswordMsg2","密码不能为空！");
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        if(!user.getPassword().equals(CommunityUtil.md5(originalPassword+user.getSalt()))){  // 原密码不正确
            model.addAttribute("originPasswordMsg","密码不正确！");
            System.out.println("密码不正确！");
            return "/site/setting";
        }
        if(!newPassword1.equals(newPassword2)){  // 新密码两次输入不一致
            model.addAttribute("newPasswordMsg2","两次密码不一致！");
            return "/site/setting";
        }
        try {  // 修改数据库中密码
            userMapper.updatePassword(user.getId(), CommunityUtil.md5(newPassword1+user.getSalt()));
            return "redirect:/index";
        } catch (Exception e) {
            logger.error("修改密码失败："+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){  // 防止恶意攻击
            throw new RuntimeException("该用户不存在");
        }
        // 查询的目标用户
        model.addAttribute("user",user);

        // 查询收到点赞数量
        int likeCount = likeService.findUserLikeCount(user.getId());
        model.addAttribute("likeCount",likeCount);

        // 查询用户关注了多少用户
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        // 查询粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);

        // 是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null)
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }
}
