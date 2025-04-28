package Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryService {
    private static final String CLOUD_NAME = "dsgayicoc";
    private static final String API_KEY = "282122788749718";
    private static final String API_SECRET = "dTrdMR0LSfY0A3AMmoIu-9V8L2Q";

    private static Cloudinary cloudinary;

    static {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);
            cloudinary = new Cloudinary(config);
            System.out.println("Cloudinary initialisé avec succès pour le cloud : " + CLOUD_NAME);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de Cloudinary : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String uploadImage(File file) throws IOException {
        if (cloudinary == null) {
            throw new IOException("Cloudinary n'est pas initialisé. Vérifiez vos identifiants.");
        }

        try {
            System.out.println("Début de l'upload vers Cloudinary : " + file.getAbsolutePath());
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("url");
            System.out.println("Upload réussi. URL : " + url);
            return url;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'upload vers Cloudinary : " + e.getMessage());
            throw e;
        }
    }
} 