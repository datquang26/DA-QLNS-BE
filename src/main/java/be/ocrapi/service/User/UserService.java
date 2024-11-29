package be.ocrapi.service.User;

import be.ocrapi.common.BusinessErrorCode;
import be.ocrapi.common.BusinessException;
import be.ocrapi.model.*;
import be.ocrapi.repository.*;
import be.ocrapi.request.UserRequest;
import be.ocrapi.response.LoginResponse;
import be.ocrapi.response.MappingResponseDto;
import be.ocrapi.response.User.UserResponse;
import be.ocrapi.security.JwtService;
import be.ocrapi.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private MappingResponseDto responseDto;
    @Autowired
    private SalaryRepository salaryRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private EmployerTypeRepository employerTypeRepository;
    @Autowired
    private CertificateRepository certificateRepository;


    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private  AuthenticationManager authenticationManager;
    @Autowired
    private CloudinaryService cloudinaryService;

    public static Date parseDate(String dateString, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Invalid date format: " + e.getMessage());
            return null;
        }
    }
    private User setData(UserRequest u, User d) {
        if(d == null) {
            d = new User();
            d.setPassword(passwordEncoder.encode(u.getPassword()));

        }

        if(u.getDob() != null && !u.getDob().isEmpty()) {
            Date fromDate = this.parseDate(u.getDob(), "yyyy-MM-dd");
            d.setDob(fromDate);
        }
        if(u.getCccdDate() != null && !u.getCccdDate() .isEmpty()) {
            Date date = this.parseDate(u.getCccdDate() , "yyyy-MM-dd");
            d.setCccdDate(date);
        }

//        d.setAvatar(u.getAvatar());
        d.setAvatar(String.valueOf(u.getAvatar()));
        d.setStatus(u.getStatus());
        d.setCode(u.getCode());
        d.setName(u.getName());
        d.setEmail(u.getEmail());
        d.setPhone(u.getPhone());
        d.setUserType(u.getUserType());
        d.setGender(u.getGender());
        d.setAddress(u.getAddress());

        d.setCccd(u.getCccd());
        d.setCccdAddress(u.getCccdAddress());
        d.setRegion(u.getRegion());

        EmployerType e = d.getEmployerType();
        Rank r = d.getRank();
        Certificate c = d.getCertificate();
        Room room = d.getRoom();

        if(u.getEmployerTypeId() != null) {
            e = employerTypeRepository.getById(u.getEmployerTypeId());
        }
        if(u.getUserRankId() != null) {
            r = rankRepository.getById(u.getUserRankId());
        }

        if(u.getCertificateId() != null) {
            c = certificateRepository.getById(u.getCertificateId());
        }
        if(u.getRoomId() != null) {
            room = roomRepository.getById(u.getRoomId());
        }

        d.setEmployerType(e);
        d.setRank(r);
        d.setCertificate(c);
        d.setRoom(room);

        return d;
    }

    @Override
    public UserResponse findById(Integer id) {
        User u = userRepository.getById(id);
        return responseDto.getUserInfo(u);
    }

    @Override
    public UserResponse findByAccessToken(String access_token) {
        User u = userRepository.findUserByAccessToken(access_token);
        if(u == null) {
            return null;
        }
        return responseDto.getUserInfo(u);
    }

    @Override
    public LoginResponse login(UserRequest data) {
        log.debug("email=====> " + data.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        data.getEmail(),
                        data.getPassword()
                )
        );

        var user = userRepository.findByEmail(data.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("USER not active");
        }


        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setAccessToken(jwtToken);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        log.debug("token=========> " + jwtToken);
        return new LoginResponse(jwtToken, refreshToken, responseDto.getUserInfo(user));
    }

//    @Override
//    public Page<User> findAll(int page, int page_size) {
//        Pageable pageable = PageRequest.of(page, page_size);
//        return userRepository.findAll(pageable);
//    }

    @Override
    public List<UserResponse> findAndCount(String page, String page_size,
                                           String status, String name,
                                           String email,
                                           String rank_id,
                                           String room_id,
                                           String certificate_id,
                                           String user_type) {
        List<User> data = this.userRepository.findAndCount((parseInt(page) - 1) * parseInt(page_size),
                parseInt(page_size), status, name, email, rank_id, room_id, certificate_id, user_type
                );
        List<UserResponse> users = new ArrayList<>();
        if(!data.isEmpty()) {
            for (User item: data) {
                users.add(responseDto.getUserInfo(item));
            }
        }


        return users;
    }
    @Override
    public Integer countTotalCondition( String status, String name,
                                        String email,
                                        String rank_id,
                                        String room_id,
                                        String certificate_id,
                                        String user_type) {
        return this.userRepository.countByConditions(status, name, email,
                rank_id, room_id, certificate_id, user_type);
    }

//    @Override
//    public UserRequest save(UserRequest dataRequest) {
//        User u = setData(dataRequest, null);
//        User newData = userRepository.save(u);
//        if(dataRequest.getCode() == null ) {
//            newData.setCode("MEMBER0000" + newData.getId());
//            userRepository.save(newData);
//        }
//        return dataRequest;
//    }

    @Override
    public UserRequest save(UserRequest dataRequest) {
        // Chuyển đổi từ UserRequest thành User entity
        User u = setData(dataRequest, null);

        // Lưu vào cơ sở dữ liệu
        User newData = userRepository.save(u);

        // Nếu không có mã code, tạo mã code mới
        if (dataRequest.getCode() == null) {
            newData.setCode("MEMBER0000" + newData.getId());
            userRepository.save(newData);
        }

        // Lưu avatar URL vào trong User entity (trường avatar đã được cập nhật ở trên)
        if (dataRequest.getAvatarUrl() != null) {
            newData.setAvatar(dataRequest.getAvatarUrl());  // Lưu URL vào trường avatar
            userRepository.save(newData);  // Cập nhật lại vào cơ sở dữ liệu
        }

        if (dataRequest.getCccdImgUrl() != null) {
            newData.setCccdImg(dataRequest.getCccdImgUrl());
            userRepository.save(newData);
        }

        return dataRequest;
    }





//    @Override
//    public UserRequest update(int id, UserRequest dataRequest) {
//        User u = userRepository.getById(id);
//        u = setData(dataRequest, u);
//        if(dataRequest.getCode() == null && u.getCode() == null) {
//            u.setCode("MEMBER0000" + u.getId());
//            userRepository.save(u);
//        }
//        userRepository.save(u);
//        return dataRequest;
//    }


    @Override
    public UserRequest update(int id, UserRequest dataRequest) {
        // Tìm người dùng theo id
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(new BusinessErrorCode(404, "User not found", "Không tìm thấy người dùng", 404)));

        // Cập nhật thông tin người dùng
        existingUser.setName(dataRequest.getName());
        existingUser.setStatus(dataRequest.getStatus());
        existingUser.setEmail(dataRequest.getEmail());
        existingUser.setPhone(dataRequest.getPhone());
        existingUser.setGender(dataRequest.getGender());

        // Chuyển đổi dob từ String sang Date
        if (dataRequest.getDob() != null && !dataRequest.getDob().isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Định dạng mong muốn
                Date dob = dateFormat.parse(dataRequest.getDob()); // Chuyển đổi String thành Date
                existingUser.setDob(dob);
            } catch (ParseException e) {
                // Nếu có lỗi trong việc chuyển đổi ngày, ném ra lỗi hoặc thông báo phù hợp
                throw new BusinessException(new BusinessErrorCode(400, "Invalid date format", "Định dạng ngày không hợp lệ", 400));
            }
        }

        // Chuyển đổi cccdDate từ String sang Date
        if (dataRequest.getCccdDate() != null && !dataRequest.getCccdDate().isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Định dạng mong muốn cho cccdDate
                Date cccdDate = dateFormat.parse(dataRequest.getCccdDate()); // Chuyển đổi String thành Date
                existingUser.setCccdDate(cccdDate); // Gán vào entity
            } catch (ParseException e) {
                // Nếu có lỗi trong việc chuyển đổi ngày, ném ra lỗi hoặc thông báo phù hợp
                throw new BusinessException(new BusinessErrorCode(400, "Invalid cccdDate format", "Định dạng ngày CCCD không hợp lệ", 400));
            }
        }

        // Cập nhật thêm các trường mới
        if (dataRequest.getUserType() != null) {
            existingUser.setUserType(dataRequest.getUserType()); // Set userType kiểu String
        }

        // Cập nhật userRankId, roomId, certificateId, employerTypeId
        if (dataRequest.getUserRankId() != null) {
            Rank rank = new Rank(); // Tạo mới đối tượng Rank (giả sử bạn có entity Rank)
            rank.setId(dataRequest.getUserRankId()); // Gán ID rank từ request
            existingUser.setRank(rank); // Set vào entity User
        }

        if (dataRequest.getRoomId() != null) {
            Room room = new Room(); // Tạo mới đối tượng Room (giả sử bạn có entity Room)
            room.setId(dataRequest.getRoomId()); // Gán ID room từ request
            existingUser.setRoom(room); // Set vào entity User
        }

        if (dataRequest.getCertificateId() != null) {
            Certificate certificate = new Certificate(); // Tạo mới đối tượng Certificate (giả sử bạn có entity Certificate)
            certificate.setId(dataRequest.getCertificateId()); // Gán ID certificate từ request
            existingUser.setCertificate(certificate); // Set vào entity User
        }

        if (dataRequest.getEmployerTypeId() != null) {
            EmployerType employerType = new EmployerType(); // Tạo mới đối tượng EmployerType (giả sử bạn có entity EmployerType)
            employerType.setId(dataRequest.getEmployerTypeId()); // Gán ID employerType từ request
            existingUser.setEmployerType(employerType); // Set vào entity User
        }

        // Cập nhật các trường còn lại
        existingUser.setAddress(dataRequest.getAddress());
        existingUser.setCccd(dataRequest.getCccd());
        existingUser.setCccdAddress(dataRequest.getCccdAddress());
        existingUser.setRegion(dataRequest.getRegion());

        // Nếu có URL avatar từ Cloudinary, cập nhật vào entity
        if (dataRequest.getAvatarUrl() != null) {
            existingUser.setAvatar(dataRequest.getAvatarUrl());
        }
        if (dataRequest.getCccdImgUrl() != null) {
            existingUser.setCccdImg(dataRequest.getCccdImgUrl());
        }

        // Lưu vào cơ sở dữ liệu
        User updatedUser = userRepository.save(existingUser);

        // Chuyển đổi entity thành UserRequest để trả về
        return new UserRequest()
                .setName(updatedUser.getName())
                .setStatus(updatedUser.getStatus())
                .setEmail(updatedUser.getEmail())
                .setPhone(updatedUser.getPhone())
                .setGender(updatedUser.getGender())
                .setAvatarUrl(updatedUser.getAvatar())  // Trả về avatar URL
                .setUserType(updatedUser.getUserType())  // Trả về userType
                .setUserRankId(updatedUser.getRank().getId())  // Trả về userRankId
                .setRoomId(updatedUser.getRoom().getId())  // Trả về roomId
                .setCertificateId(updatedUser.getCertificate().getId())  // Trả về certificateId
                .setEmployerTypeId(updatedUser.getEmployerType().getId())
                .setCccdImgUrl(updatedUser.getCccdImg());  // Trả về employerTypeId
    }








    @Override
    public void delete(Integer id) {
        userRepository.deleteById(id);
    }
}
