package pro.dengyi.myhome.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import pro.dengyi.myhome.dao.DeviceLogDao;
import pro.dengyi.myhome.dao.FamilyDao;
import pro.dengyi.myhome.dao.OperationLogDao;
import pro.dengyi.myhome.dao.ScheduleTaskDao;
import pro.dengyi.myhome.dao.UserDao;
import pro.dengyi.myhome.model.device.DeviceLog;
import pro.dengyi.myhome.model.system.Family;
import pro.dengyi.myhome.model.system.OperationLog;
import pro.dengyi.myhome.model.system.User;
import pro.dengyi.myhome.properties.SystemProperties;
import pro.dengyi.myhome.utils.UserHolder;
import pro.dengyi.myhome.utils.queue.DeviceLogQueue;
import pro.dengyi.myhome.utils.queue.OperationLogQueue;
import pro.dengyi.myhome.utils.queue.RoomSelectQueue;

/**
 * 项目启动监听
 *
 * @author dengyi (email:dengyi@dengyi.pro)
 * @date 2022-01-22
 */
@Slf4j
@Component
public class ApplicationRunListener implements ApplicationRunner {

  @Autowired
  private SystemProperties systemProperties;
  @Autowired
  private UserDao userDao;
  @Autowired
  private FamilyDao familyDao;
  @Autowired
  private MqttClient mqttClient;
  @Autowired
  private Executor executor;

  @Autowired
  private Scheduler scheduler;

  @Autowired
  private ScheduleTaskDao scheduleTaskDao;

  @Autowired
  private OperationLogDao operationLogDao;
  @Autowired
  private DeviceLogDao deviceLogDao;


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
//    List<User> userList = userDao.selectList(
//        new LambdaQueryWrapper<User>().eq(User::getHouseHolder, true));
//    if (CollectionUtils.isEmpty(userList)) {
//      //初始化一个默认家庭
//      //默认用户不存在，新增一个
//      User user = new User();
//      user.setName(systemProperties.getDefaultName());
//      user.setAvatar(systemProperties.getDefaultAvatar());
//      user.setEmail(systemProperties.getDefaultEmail());
//      user.setPassw(systemProperties.getDefaultPassword());
//      user.setHouseHolder(true);
//      LocalDateTime now = LocalDateTime.now();
//      user.setCreateTime(now);
//      user.setUpdateTime(now);
//      userDao.insert(user);
//    }

    //连接mqtt服务器
    executor.execute(() -> {
      try {
        TimeUnit.SECONDS.sleep(5);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setKeepAliveInterval(60);
        connOpts.setUserName("admin");
        connOpts.setPassword("admin".toCharArray());
        // 保留会话
        connOpts.setCleanSession(true);
        mqttClient.connect(connOpts);
        mqttClient.subscribe("report/#", 2);
//        加载任务
//        scheduleTaskDao.selectList(
//                new LambdaQueryWrapper<ScheduleTask>().eq(ScheduleTask::getEnable, true))
//            .forEach(task -> {
//
//              JobDetail jobDetail = JobBuilder.newJob(MyScheduleJob.class)
//                  .withIdentity(JobKey.jobKey(task.getId())).build();
//              Trigger jobTrigger = TriggerBuilder.newTrigger().startNow()
//                  .withIdentity(TriggerKey.triggerKey(task.getId()))
//                  .withSchedule(CronScheduleBuilder.cronSchedule(task.getCron())).build();
//              try {
//                scheduler.scheduleJob(jobDetail, jobTrigger);
//              } catch (SchedulerException e) {
//                log.error("加载任务失败", e);
//              }
//            });
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    //操作日志处理线程
    executor.execute(() -> {
      while (true) {
        OperationLog operationLog = OperationLogQueue.consume();
        operationLogDao.insert(operationLog);
      }
    });
    //设备日志队列
    executor.execute(() -> {
      while (true) {
        DeviceLog deviceLog = DeviceLogQueue.consume();
        deviceLogDao.insert(deviceLog);
      }
    });
    //用户选择上报
    executor.execute(() -> {
      while (true) {
        Map<String, String> params = RoomSelectQueue.consume();
        User user = userDao.selectById(UserHolder.getUser().getId());
        user.setSelectedFloorId(params.get("floorId"));
        user.setSelectedRoomId(params.get("roomId"));
        userDao.updateById(user);
      }
    });
  }


}
