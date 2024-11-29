package be.ocrapi.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map<String, String> uploadFile(MultipartFile file) {
        String folderName = getFolderNameByDate(); // Lấy tên thư mục theo ngày hiện tại
        Map<String, String> result = new HashMap<>(); // Kết quả trả về

        try {
            String fileName = createNameImage(file); // Tạo tên tệp ảnh mới
            String publicId = folderName + "/" + fileName; // Tạo publicId

            // Upload tệp lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "quality", "auto:low")); // Giảm chất lượng hình ảnh

            // Lưu kết quả vào map
            result.put("publicId", publicId);
            result.put("url", uploadResult.get("url").toString());

            return result; // Trả về kết quả duy nhất
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi
        }
    }


    public boolean deleteFile(String publicId) {
        try {
            Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(deleteResult.get("result"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteFilesByPublicIds(List<String> publicIds) {
        List<Boolean> results = new ArrayList<>();

        for (String publicId : publicIds) {
            boolean deleteSuccess = deleteFile(publicId);
            results.add(deleteSuccess);
            log.info("Deleted file: {} " , publicId);
            if (!deleteSuccess) {
                log.error("Không thể xóa ảnh có publicId: {}" , publicId);
            }
        }

    }

    private String getFolderNameByDate() {
        LocalDate now = LocalDate.now();
        int day = now.getDayOfMonth();
        int month = now.getMonthValue();
        int year = now.getYear();
        return year + "_" + month + "_" + day;
    }

    private String createNameImage(MultipartFile file) {
//        String originalFileName = file.getOriginalFilename();
        return "file_" + UUID.randomUUID();
    }
}
