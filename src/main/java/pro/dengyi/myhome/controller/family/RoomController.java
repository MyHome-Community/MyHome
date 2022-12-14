package pro.dengyi.myhome.controller.family;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.benmanes.caffeine.cache.Cache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.dengyi.myhome.model.dto.RoomDto;
import pro.dengyi.myhome.model.system.Room;
import pro.dengyi.myhome.response.CommonResponse;
import pro.dengyi.myhome.response.DataResponse;
import pro.dengyi.myhome.service.RoomService;

/**
 * 楼层controller
 *
 * @author dengyi (email:dengyi@dengyi.pro)
 * @date 2022-01-23
 */
@Validated
@Api(tags = "房间接口")
@RestController
@RequestMapping("/room")
public class RoomController {

  @Autowired
  private RoomService roomService;
  @Autowired
  private Cache cache;


  @ApiOperation("分页查询")
  @GetMapping("/page")
  public DataResponse<IPage<RoomDto>> page(Integer page, Integer size, String floorId,
      String roomName) {
    IPage<RoomDto> pageRes = roomService.page(page, size, floorId, roomName);
    return new DataResponse<>(pageRes);
  }

  @ApiOperation("查询房间列表")
  @GetMapping("/roomList")
  public DataResponse<List<Room>> roomList() {
    List<Room> roomList = (List<Room>) cache.get("roomList", k -> roomService.roomList());
    return new DataResponse<>(roomList);
  }

  @ApiOperation("根据楼层id查询房间列表")
  @GetMapping("/roomListByFloorId")
  public DataResponse<List<Room>> roomListByFloorId(
      @RequestParam @NotBlank(message = "楼层id不能为空") String floorId) {
    List<Room> roomList = (List<Room>) cache.get("roomListByFloorId:" + floorId,
        k -> roomService.roomListByFloorId(floorId));
    return new DataResponse<>(roomList);
  }


  @ApiOperation("新增或修改房间")
  @PostMapping("/addUpdate")
  public CommonResponse addUpdate(@RequestBody @Validated Room room) {
    roomService.addUpdate(room);
    return CommonResponse.success();
  }

  @ApiOperation("删除房间")
  @DeleteMapping("/delete/{id}")
  public CommonResponse delete(@PathVariable @NotBlank(message = "id不能为空") String id) {
    roomService.delete(id);
    return CommonResponse.success();
  }
}
