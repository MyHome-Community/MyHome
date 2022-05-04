package pro.dengyi.myhome.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import pro.dengyi.myhome.dao.FamilyDao;
import pro.dengyi.myhome.dao.UserDao;
import pro.dengyi.myhome.model.Family;
import pro.dengyi.myhome.model.User;
import pro.dengyi.myhome.properties.InitProperties;

import java.util.Date;
import java.util.List;

/**
 * 项目启动监听
 *
 * @author dengyi (email:dengyi@dengyi.pro)
 * @date 2022-01-22
 */
@Component
public class ApplicationRunListener implements ApplicationRunner {

  @Autowired
  private InitProperties initProperties;

  @Autowired
  private UserDao userDao;
  @Autowired
  private FamilyDao familyDao;


  @Transactional
  @Override
  public void run(ApplicationArguments args) throws Exception {
    //项目初始化
    Family family = familyDao.selectOne(new LambdaQueryWrapper<>());
    if (ObjectUtils.isEmpty(family)) {
      Family newFamily = new Family();
      newFamily.setName("我的家");
      familyDao.insert(newFamily);
    }
    List<User> userList = userDao.selectList(
        new LambdaQueryWrapper<User>().eq(User::getHouseHolder, true));
    if (CollectionUtils.isEmpty(userList)) {
      //初始化一个默认家庭
      //默认用户不存在，新增一个
      User user = new User();
      user.setName(initProperties.getDefaultName());
      user.setAvatar(initProperties.getDefaultAvatar());
      user.setEmail(initProperties.getDefaultEmail());
      user.setPassw(initProperties.getDefaultPassword());
      user.setHouseHolder(true);
      LocalDateTime now = LocalDateTime.now();
      user.setCreateTime(now);
      user.setUpdateTime(now);
      userDao.insert(user);
    }
  }

}
