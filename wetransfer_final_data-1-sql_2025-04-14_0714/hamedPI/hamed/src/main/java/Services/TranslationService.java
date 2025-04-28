package Services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TranslationService {
    private static final String TRANSLATE_API_URL = "https://api.mymemory.translated.net/get";
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();

    public static String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            // Par défaut, on suppose que le texte est en anglais
            String sourceLanguage = "en";
            // Si la langue cible est le français, on utilise fr comme code
            String targetLangCode = targetLanguage.equals("fr") ? "fr" : "en";

            String url = String.format("%s?q=%s&langpair=%s|%s",
                TRANSLATE_API_URL,
                java.net.URLEncoder.encode(text, "UTF-8"),
                sourceLanguage,
                targetLangCode);

            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorMessage = "Erreur serveur : " + response.code();
                    if (response.code() == 429) {
                        errorMessage = "Trop de requêtes. Veuillez réessayer plus tard.";
                    }
                    throw new IOException(errorMessage);
                }

                String responseBody = response.body().string();
                if (responseBody == null || responseBody.isEmpty()) {
                    throw new IOException("Réponse vide du serveur");
                }

                JSONObject jsonResponse = new JSONObject(responseBody);
                if (!jsonResponse.has("responseData")) {
                    throw new IOException("Format de réponse invalide");
                }

                JSONObject responseData = jsonResponse.getJSONObject("responseData");
                return responseData.getString("translatedText");
            }
        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            throw new RuntimeException("Impossible de se connecter au service de traduction. Veuillez vérifier votre connexion internet et réessayer.", e);
        } catch (Exception e) {
            System.err.println("Erreur lors de la traduction : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la traduction : " + e.getMessage(), e);
        }
    }
} 