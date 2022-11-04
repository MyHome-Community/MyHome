package pro.dengyi.myhome.model.device;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import pro.dengyi.myhome.model.base.BaseModel;

/**
 * @author BLab
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName("device_device")
@ApiModel("设备实体")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Device extends BaseModel {

  @ApiModelProperty(value = "芯片ID")
  private String chipId;

  @ApiModelProperty(value = "产品ID")
  @NotBlank(message = "所属产品必选")
  private String productId;

//  @ApiModelProperty(value = "连接密码")
//  private String loginPassword;

  @ApiModelProperty(value = "是否在线")
  private Boolean online;

  @ApiModelProperty(value = "是否启用")
  private Boolean enable;

  @ApiModelProperty(value = "固件版本")
  private Integer framewareVersion;

  @ApiModelProperty(value = "设备名", notes = "只是用户起的别名")
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  @Length(min = 2, max = 20)
  @NotBlank(message = "设备名必须填写")
  private String nickName;

  @ApiModelProperty(value = "所属楼层ID")
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  @NotBlank(message = "必须选择楼层")
  private String floorId;

  @ApiModelProperty(value = "所属房间ID")
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  @NotBlank(message = "必须选择分类")
  private String roomId;


}
