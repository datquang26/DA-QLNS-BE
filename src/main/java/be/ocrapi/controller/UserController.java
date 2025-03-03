package be.ocrapi.controller;

import be.ocrapi.common.BaseResponse;
import be.ocrapi.common.BusinessErrorCode;
import be.ocrapi.common.BusinessException;
import be.ocrapi.request.UserRequest;
import be.ocrapi.service.CloudinaryService;
import be.ocrapi.service.User.UserServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/user")
@Slf4j
public class UserController {
    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private CloudinaryService cloudinaryService;


    @GetMapping("show/{id}")
    public BaseResponse<?> findOne(@PathVariable Integer id) {
        try {
            return BaseResponse.ofSucceeded(userService.findById(id));
        } catch (Exception e) {
            log.debug("[USER CONTROLLER]------>error findOne", e);
            String message = e.getMessage();
            var error = new BusinessException(new BusinessErrorCode(400, message, message, 400));
            log.error("[USER CONTROLLER]------>findOne", error);
            return BaseResponse.ofFailed(error);
        }
    }

    @GetMapping("list")
    public BaseResponse<?> findAll(
            @RequestParam(name = "page", required = false, defaultValue = "1") String page,
            @RequestParam(name = "page_size", required = false, defaultValue = "20") String page_size,
            @RequestParam(name = "status", required = false, defaultValue = "") String status,
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "email", required = false, defaultValue = "") String email,
            @RequestParam(name = "rank_id", required = false, defaultValue = "") String rank_id,
            @RequestParam(name = "salary_id", required = false, defaultValue = "") String salary_id,
            @RequestParam(name = "certificate_id", required = false, defaultValue = "") String certificate_id,
            @RequestParam(name = "room_id", required = false, defaultValue = "") String room_id,
            @RequestParam(name = "user_type", required = false, defaultValue = "") String user_type,
            @RequestParam(name = "type_id", required = false, defaultValue = "") String type_id
    ) {
        try {
            int number_page = 0;
            if(Integer.parseInt(page) > 1) {
                number_page = Integer.parseInt(page) - 1;
            }
//            var response = userService.findAll(number_page, Integer.parseInt(page_size));
            var users = userService.findAndCount(page, page_size,status, name, email, rank_id, room_id, certificate_id, user_type);
            Integer total = userService.countTotalCondition(status, name, email, rank_id, room_id, certificate_id, user_type);
            BaseResponse.Metadata paging = new BaseResponse.Metadata("", number_page ,  Integer.parseInt(page_size), Long.parseLong(total + ""), "", null);
            return BaseResponse.ofSucceeded().setData(users).setMeta(paging);
        } catch (Exception e) {
            log.debug("[USER CONTROLLER]------>error list", e);
            String message = e.getMessage();
            var error = new BusinessException(new BusinessErrorCode(400, message, message, 400));
            log.error("[USER CONTROLLER]------>list", error);
            return BaseResponse.ofFailed(error);
        }
    }

//    @PostMapping("store")
//    public BaseResponse<?> save(@RequestBody UserRequest data) {
//        try {
//            return BaseResponse.ofSucceeded(userService.save(data));
//        } catch (Exception e) {
//            log.debug("[USER CONTROLLER]------>error create", e);
//            String message = e.getMessage();
//            var error = new BusinessException(new BusinessErrorCode(400, message, "Tạo mới thất bại", 400));
//            log.error("[USER CONTROLLER]------>create", error);
//            return BaseResponse.ofFailed(error);
//        }
//    }


    @PostMapping("store")
    public BaseResponse<?> save(@ModelAttribute UserRequest data) {
        try {
            // Kiểm tra nếu có avatar trong request
            if (data.getAvatar() != null && !data.getAvatar().isEmpty()) {
                // Upload avatar lên Cloudinary và nhận URL
                Map<String, String> uploadResult = cloudinaryService.uploadFile(data.getAvatar());
                if (uploadResult != null && uploadResult.containsKey("url")) {
                    // Lưu URL avatar vào UserRequest
                    data.setAvatarUrl(uploadResult.get("url"));
                }
            }


            if (data.getCccdImg() != null && !data.getCccdImg().isEmpty()) {
                // Upload avatar lên Cloudinary và nhận URL
                Map<String, String> uploadResult1 = cloudinaryService.uploadFile(data.getCccdImg());
                if (uploadResult1 != null && uploadResult1.containsKey("url")) {
                    // Lưu URL avatar vào UserRequest
                    data.setCccdImgUrl(uploadResult1.get("url"));
                }
            }



            // Tạo đối tượng UserRequest từ các trường dữ liệu
            UserRequest userRequest = new UserRequest()
                    .setName(data.getName())
                    .setEmail(data.getEmail())
                    .setPhone(data.getPhone())
                    .setGender(data.getGender())
                    .setPassword(data.getPassword())
                    .setAddress(data.getAddress())
                    .setStatus(data.getStatus())
                    .setCccd(data.getCccd())
                    .setUserType(data.getUserType())
                    .setDob(data.getDob())
                    .setCccdAddress(data.getCccdAddress())
                    .setCccdDate(data.getCccdDate())
                    .setRegion(data.getRegion())
                    .setAvatarUrl(data.getAvatarUrl())
                    .setEmployerTypeId(data.getEmployerTypeId())
                    .setCertificateId(data.getCertificateId())
                    .setRoomId(data.getRoomId())
                    .setUserRankId(data.getUserRankId())
                    .setAccessToken(data.getAccessToken())
                    .setRefreshToken(data.getRefreshToken())
                    .setCccdImgUrl(data.getCccdImgUrl());
            // Đảm bảo URL avatar đã được set

            // Lưu thông tin người dùng vào cơ sở dữ liệu và trả về kết quả
            return BaseResponse.ofSucceeded(userService.save(userRequest));
        } catch (Exception e) {
            log.debug("[USER CONTROLLER]------>error create", e);
            String message = e.getMessage();
            var error = new BusinessException(new BusinessErrorCode(400, message, "Tạo mới thất bại", 400));
            log.error("[USER CONTROLLER]------>create", error);
            return BaseResponse.ofFailed(error);
        }
    }








//    @PutMapping("update/{id}")
//    public BaseResponse<?> update(@PathVariable Integer id,@RequestBody UserRequest data) {
//        try {
//            return BaseResponse.ofSucceeded(userService.update(id, data));
//        } catch (Exception e) {
//            log.debug("[USER CONTROLLER]------>error update", e);
//            String message = e.getMessage();
//            var error = new BusinessException(new BusinessErrorCode(400, message, "Cập nhật thất bại", 400));
//            log.error("[USER CONTROLLER]------>update", error);
//            return BaseResponse.ofFailed(error);
//        }
//    }

    @PutMapping("update/{id}")
    public BaseResponse<?> update(@PathVariable Integer id, @ModelAttribute UserRequest data) {
        try {
            // Nếu có ảnh avatar, upload lên Cloudinary và lấy URL trả về
            if (data.getAvatar() != null && !data.getAvatar().isEmpty()) {
                Map<String, String> cloudResult = cloudinaryService.uploadFile(data.getAvatar());
                if (cloudResult != null && cloudResult.containsKey("url")) {
                    // Lưu URL trả về từ Cloudinary vào trường avatarUrl
                    data.setAvatarUrl(cloudResult.get("url"));
                } else {
                    // Xử lý khi không thể tải ảnh lên Cloudinary
                    return BaseResponse.ofFailed(new BusinessErrorCode(500, "Upload file to Cloudinary failed", "Tải ảnh lên thất bại", 500));
                }
            }

        // Lưu URL trả về từ Cloudinary vào trường căn cươc cong dan
            if (data.getCccdImg() != null && !data.getCccdImg().isEmpty()) {
                Map<String, String> cloudResult1 = cloudinaryService.uploadFile(data.getCccdImg());
                if (cloudResult1 != null && cloudResult1.containsKey("url")) {

                    data.setCccdImgUrl(cloudResult1.get("url"));
                } else {
                    // Xử lý khi không thể tải ảnh lên Cloudinary
                    return BaseResponse.ofFailed(new BusinessErrorCode(500, "Upload file to Cloudinary failed", "Tải ảnh lên thất bại", 500));
                }
            }


            // Tiến hành gọi service để cập nhật thông tin người dùng
            UserRequest updatedUser = userService.update(id, data);

            // Trả về kết quả với thông tin đã cập nhật, bao gồm URL avatar
            return BaseResponse.ofSucceeded(updatedUser);
        } catch (Exception e) {
            log.debug("[USER CONTROLLER]------>error update", e);
            String message = e.getMessage();
            var error = new BusinessException(new BusinessErrorCode(400, message, "Cập nhật thất bại", 400));
            log.error("[USER CONTROLLER]------>update", error);
            return BaseResponse.ofFailed(error);
        }
    }




    @DeleteMapping("delete/{id}")
    public BaseResponse delete(@PathVariable Integer id) {
        try {
            userService.delete(id);
            return BaseResponse.ofSucceeded();
        } catch (Exception e) {
            log.debug("[BonusAndDisciplineRequest CONTROLLER]------>error update", e);
            String message = e.getMessage();
            var error = new BusinessException(new BusinessErrorCode(400, message, "Cập nhật thất bại", 400));
            log.error("[BonusAndDisciplineRequest CONTROLLER]------>update", error);
            return BaseResponse.ofFailed(error);
        }
    }
}
