package be.ocrapi;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class OcrApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcrApiApplication.class, args);
	}

	@Bean
	Tesseract getTesseract(){
		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath("./tessdata");
//		tesseract.setHocr(true);
		return tesseract;
	}

	@Bean
	public Cloudinary cloudinaryConfig() {
		Cloudinary cloudinary = null;
		Map config = new HashMap();
		config.put("cloud_name", "da60iqoro");
		config.put("api_key", "616267661569778");
		config.put("api_secret", "lNH005tyO0ddj6eodW4hWvOvJgQ");
		cloudinary = new Cloudinary(config);
		return cloudinary;
	}

}
