package controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Categorie;
import entities.Produit;
import entities.Utilisateur;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import services.CategorieService;
import services.ProduitService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Ajouterproduit {

    private static final Logger logger = Logger.getLogger(Ajouterproduit.class.getName());

    // FXML Elements
    @FXML private ImageView productImage;
    @FXML private Button uploadButton;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private TextField quantiteField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField nomProduitField;
    @FXML private TextField prixField;
    @FXML private TextField discountPercentageField;
    @FXML private TextField codePromoField;
    @FXML private Button generateDescriptionButton; // Button for generating description
    @FXML private Button ajout;

    // Services
    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    // Cloudinary
    private final Cloudinary cloudinary;

    // OpenAI API Key
    private final String openAiApiKey;

    // Variables
    private File selectedImageFile;
    private String generatedCodePromo;

    // Constantes pour les validations
    private static final int MAX_QUANTITE = 10000;
    private static final float MAX_PRIX = 1000000.0f;
    private static final int MIN_NOM_LENGTH = 2;
    private static final int MAX_NOM_LENGTH = 100;
    private static final int MIN_DESC_LENGTH = 10;
    private static final int MAX_DESC_LENGTH = 500;
    private static final String CODE_PROMO_PATTERN = "^[A-Z0-9]{8}$"; // 8 caractères alphanumériques
    private static final String NOM_PATTERN = "^[a-zA-Z\\s-]+$"; // Lettres, espaces, tirets (pas de chiffres)

    // Retry configuration for OpenAI API
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 5000; // 5 seconds initial delay

    private Utilisateur loggedInUser; // Add a field for the logged-in user

    public void initData(Utilisateur user) {
        this.loggedInUser = user; // Initialize the logged-in user
    }

    public Ajouterproduit() {
        // Load environment variables
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();

        // Load Cloudinary URL
        String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            throw new IllegalStateException("CLOUDINARY_URL not found in .env file");
        }
        cloudinary = new Cloudinary(cloudinaryUrl);

        // Load OpenAI API key
        openAiApiKey = dotenv.get("OPENAI_API_KEY");
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY not found in .env file");
        }
    }

    @FXML
    public void initialize() {
        chargerCategories();

        categorieComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categorie categorie) {
                return (categorie != null) ? categorie.getNom_categorie() : "";
            }

            @Override
            public Categorie fromString(String nom) {
                return categorieComboBox.getItems().stream()
                        .filter(c -> c.getNom_categorie().equals(nom))
                        .findFirst()
                        .orElse(null);
            }
        });

        generatedCodePromo = generateCodePromo();
        codePromoField.setText(generatedCodePromo);

        // Debug: Confirm button is loaded
        System.out.println("generateDescriptionButton: " + (generateDescriptionButton != null ? "Loaded" : "Not Loaded"));
    }

    private String generateCodePromo() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieComboBox.getItems().addAll(categories);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
            showError("Erreur lors du chargement des catégories : " + e.getMessage());
        }
    }

    @FXML
    private void handleCategorieChange(ActionEvent actionEvent) {
        Categorie selected = categorieComboBox.getValue();
        System.out.println("Catégorie sélectionnée : " + (selected != null ? selected.getNom_categorie() : "Aucune"));
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de produit");
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.gif"
        );
        fileChooser.getExtensionFilters().add(imageFilter);
        selectedImageFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedImageFile != null) {
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                productImage.setImage(image);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors du chargement de l'image", e);
                showError("Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
    }

    @FXML
    private void generateDescription(ActionEvent event) {
        System.out.println("generateDescription method called"); // Debug

        String nomProduit = nomProduitField.getText().trim();
        if (nomProduit.isEmpty()) {
            showWarning("Veuillez entrer le nom du produit avant de générer une description.");
            return;
        }

        try {
            System.out.println("Calling OpenAI API for product: " + nomProduit); // Debug
            // Generate description using OpenAI API
            String generatedDescription = generateProductDescription(nomProduit);
            System.out.println("Generated description: " + generatedDescription); // Debug

            if (generatedDescription.equals("Description non générée.") || generatedDescription.startsWith("Erreur")) {
                // Fallback to local description if API fails
                generatedDescription = generateFallbackDescription(nomProduit);
                System.out.println("Fallback description: " + generatedDescription); // Debug
                showWarning("Impossible de générer la description via l'API. Une description par défaut a été utilisée.");
            }

            descriptionArea.setText(generatedDescription);

            // Validate description length
            if (generatedDescription.length() < MIN_DESC_LENGTH || generatedDescription.length() > MAX_DESC_LENGTH) {
                showWarning("La description générée doit contenir entre " + MIN_DESC_LENGTH + " et " + MAX_DESC_LENGTH + " caractères.");
                if (generatedDescription.length() > MAX_DESC_LENGTH) {
                    descriptionArea.setText(generatedDescription.substring(0, MAX_DESC_LENGTH));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur inattendue lors de la génération de la description", e);
            showError("Erreur inattendue : " + e.getMessage());
        }
    }

    private String generateProductDescription(String productName) {
        int retryCount = 0;
        long delay = INITIAL_RETRY_DELAY_MS;

        while (retryCount <= MAX_RETRIES) {
            try {
                OkHttpClient client = new OkHttpClient();
                String prompt = "Génère une description de produit pour : " + productName;
                System.out.println("Prompt sent to OpenAI: " + new String(prompt.getBytes(), StandardCharsets.UTF_8)); // Debug with proper encoding

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String jsonBody = "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}], \"max_tokens\": 100}";
                System.out.println("Request body: " + new String(jsonBody.getBytes(), StandardCharsets.UTF_8)); // Debug with proper encoding
                RequestBody body = RequestBody.create(jsonBody, JSON);
                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + openAiApiKey)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println("API Response Code: " + response.code()); // Debug

                if (response.code() == 429) {
                    retryCount++;
                    if (retryCount > MAX_RETRIES) {
                        logger.log(Level.SEVERE, "Nombre maximum de tentatives atteint après " + MAX_RETRIES + " essais pour 429 erreur");
                        return "Erreur lors de la génération de la description : Trop de requêtes (Code: 429). Veuillez réessayer plus tard.";
                    }
                    logger.log(Level.WARNING, "Erreur 429 - Trop de requêtes. Tentative " + retryCount + " après un délai de " + delay + "ms");
                    Thread.sleep(delay);
                    delay *= 2; // Exponential backoff
                    continue;
                }

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    logger.log(Level.SEVERE, "Erreur API OpenAI : Code " + response.code() + " - " + response.message() + " - Body: " + errorBody);
                    return "Erreur lors de la génération de la description : " + response.message() + " (Code: " + response.code() + ")";
                }

                String responseBody = response.body().string();
                System.out.println("Raw API Response: " + responseBody); // Debug
                return parseGeneratedDescription(responseBody);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erreur lors de la génération de la description avec l'API AI", e);
                return "Erreur lors de la génération de la description : " + e.getMessage();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interruption lors de la pause pour la rétentative", e);
                Thread.currentThread().interrupt();
                return "Erreur lors de la génération de la description : Interruption pendant la rétentative";
            }
        }
        return "Erreur lors de la génération de la description : Trop de requêtes après plusieurs tentatives. Veuillez réessayer plus tard.";
    }

    private String parseGeneratedDescription(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String description = choices.get(0).path("message").path("content").asText().trim();
                System.out.println("Parsed description: " + description); // Debug
                return description;
            }
            logger.log(Level.WARNING, "Réponse OpenAI vide ou mal formée : " + responseBody);
            return "Description non générée.";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du parsing de la réponse OpenAI", e);
            return "Description non générée : " + e.getMessage();
        }
    }

    private String generateFallbackDescription(String productName) {
        // Simple fallback description in French
        return "Découvrez notre " + productName + ", un produit de qualité supérieure, parfait pour tous vos besoins. Profitez de ses caractéristiques uniques et de son design élégant.";
    }

    @FXML
    public void ajouterproduit(ActionEvent actionEvent) {
        try {
            // Validation des champs
            String nomProduit = nomProduitField.getText().trim();
            if (nomProduit.isEmpty()) {
                showWarning("Veuillez entrer le nom du produit.");
                return;
            }
            if (nomProduit.length() < MIN_NOM_LENGTH || nomProduit.length() > MAX_NOM_LENGTH) {
                showWarning("Le nom du produit doit contenir entre " + MIN_NOM_LENGTH + " et " + MAX_NOM_LENGTH + " caractères.");
                return;
            }
            if (!Pattern.matches(NOM_PATTERN, nomProduit)) {
                showWarning("Le nom du produit ne doit contenir que des lettres, espaces ou tirets (pas de chiffres).");
                return;
            }

            String description = descriptionArea.getText().trim();
            if (description.isEmpty()) {
                showWarning("Veuillez entrer une description pour le produit.");
                return;
            }
            if (description.length() < MIN_DESC_LENGTH || description.length() > MAX_DESC_LENGTH) {
                showWarning("La description doit contenir entre " + MIN_DESC_LENGTH + " et " + MAX_DESC_LENGTH + " caractères.");
                return;
            }

            int quantite = parseInt(quantiteField.getText(), "quantité");
            if (quantite == -1) return;
            if (quantite < 0) {
                showWarning("La quantité ne peut pas être négative.");
                return;
            }
            if (quantite > MAX_QUANTITE) {
                showWarning("La quantité ne peut pas dépasser " + MAX_QUANTITE + ".");
                return;
            }

            float prix = parseFloat(prixField.getText(), "prix");
            if (prix == -1) return;
            if (prix < 0) {
                showWarning("Le prix ne peut pas être négatif.");
                return;
            }
            if (prix > MAX_PRIX) {
                showWarning("Le prix ne peut pas dépasser " + MAX_PRIX + " DT.");
                return;
            }

            Categorie selectedCategorie = categorieComboBox.getValue();
            if (selectedCategorie == null) {
                showWarning("Veuillez sélectionner une catégorie.");
                return;
            }

            if (selectedImageFile == null) {
                showWarning("Veuillez sélectionner une image pour le produit.");
                return;
            }

            // Validation du pourcentage de réduction
            float discountPercentage;
            try {
                discountPercentage = discountPercentageField.getText().isEmpty() ? 0 : Float.parseFloat(discountPercentageField.getText());
                if (discountPercentage < 0 || discountPercentage > 100) {
                    showWarning("Le pourcentage de réduction doit être entre 0 et 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showWarning("Veuillez entrer un pourcentage de réduction valide (ex: 10).");
                return;
            }

            // Récupérer et valider le code promo
            String codePromo = codePromoField.getText().trim();
            if (codePromo.isEmpty()) {
                showWarning("Le code promo ne peut pas être vide.");
                return;
            }
            if (!Pattern.matches(CODE_PROMO_PATTERN, codePromo)) {
                showWarning("Le code promo doit contenir exactement 8 caractères alphanumériques.");
                return;
            }

            if (loggedInUser == null) {
                showError("Utilisateur non connecté. Veuillez vous reconnecter.");
                return;
            }

            // Upload image to Cloudinary
            String imageUrl;
            try {
                Map uploadResult = cloudinary.uploader().upload(selectedImageFile, ObjectUtils.asMap(
                        "resource_type", "image",
                        "public_id", "products/" + nomProduit + "_" + System.currentTimeMillis()
                ));
                imageUrl = (String) uploadResult.get("secure_url");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erreur lors de l'upload de l'image vers Cloudinary", e);
                showError("Erreur lors de l'upload de l'image vers Cloudinary : " + e.getMessage());
                return;
            }

            // Créer le produit et l'ajouter
            Produit produit = new Produit(quantite, selectedCategorie.getId(), prix, imageUrl, description, nomProduit, codePromo, discountPercentage, loggedInUser.getId());
            produitService.ajouter(produit);

            showInfo("Produit ajouté avec succès!");
            redirectToProductList();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ajout du produit", e);
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    private int parseInt(String text, String fieldName) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            showError("Le " + fieldName + " doit être un nombre entier valide.");
            return -1;
        }
    }

    private float parseFloat(String text, String fieldName) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            showError("Le " + fieldName + " doit être un nombre décimal valide (ex : 12.5).");
            return -1;
        }
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ajout réussi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToProductList() {
        try {
            // Get the current stage and its controller
            Stage stage = (Stage) nomProduitField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dbEvenement.fxml"));
            Parent root = loader.load();

            Affichierproduit controller = loader.getController();
            //controller.initData(loggedInUser); // Pass the logged-in user
            controller.chargerProduits(); // Refresh the product list

            // Update the scene with the refreshed data
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de la mise à jour de la liste des produits", e);
            showError("Erreur de mise à jour : " + e.getMessage());
        }
    }

    private void clearFields() {
        nomProduitField.clear();
        quantiteField.clear();
        prixField.clear();
        descriptionArea.clear();
        productImage.setImage(null);
        selectedImageFile = null;
        categorieComboBox.getSelectionModel().clearSelection();
        discountPercentageField.clear();
        generatedCodePromo = generateCodePromo();
        codePromoField.setText(generatedCodePromo);
    }
}