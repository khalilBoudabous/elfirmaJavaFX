package controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import entities.Categorie;
import entities.Produit;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CategorieService;
import services.ProduitService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

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

public class ModifierProduit {
    private static final Logger logger = Logger.getLogger(ModifierProduit.class.getName());

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
    @FXML private Button generateDescriptionButton;
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
    private Produit produitActuel;
    private Affichierproduit affichageController;
    private String generatedCodePromo;

    // Constantes pour les validations
    private static final int MAX_QUANTITE = 10000;
    private static final float MAX_PRIX = 1000000.0f;
    private static final int MIN_NOM_LENGTH = 2;
    private static final int MAX_NOM_LENGTH = 100;
    private static final int MIN_DESC_LENGTH = 10;
    private static final int MAX_DESC_LENGTH = 500;
    private static final String CODE_PROMO_PATTERN = "^[A-Z0-9]{8}$";
    private static final String NOM_PATTERN = "^[a-zA-Z\\s-]+$";

    // Retry configuration for OpenAI API
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 5000;

    public ModifierProduit() {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();

        String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            throw new IllegalStateException("CLOUDINARY_URL not found in .env file");
        }
        cloudinary = new Cloudinary(cloudinaryUrl);

        openAiApiKey = dotenv.get("OPENAI_API_KEY");
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY not found in .env file");
        }
    }

    public void setAffichageController(Affichierproduit controller) {
        this.affichageController = controller;
    }

    @FXML
    public void initialize() {
        chargerCategories();
        categorieComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categorie categorie) {
                return categorie != null ? categorie.getNom_categorie() : "";
            }

            @Override
            public Categorie fromString(String nom) {
                return categorieComboBox.getItems().stream()
                        .filter(c -> c.getNom_categorie().equals(nom))
                        .findFirst()
                        .orElse(null);
            }
        });

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
            categorieComboBox.getItems().setAll(categories);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
            showError("Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleCategorieChange(ActionEvent actionEvent) {
        Categorie selected = categorieComboBox.getValue();
        System.out.println("Catégorie sélectionnée: " + (selected != null ? selected.getNom_categorie() : "Aucune"));
    }

    public void setProduit(Produit produit) {
        this.produitActuel = produit;
        nomProduitField.setText(produit.getNom_produit());
        prixField.setText(String.valueOf(produit.getPrix()));
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        descriptionArea.setText(produit.getDescription());
        codePromoField.setText(produit.getCode_promo() != null ? produit.getCode_promo() : generateCodePromo());
        discountPercentageField.setText(String.valueOf(produit.getDiscount_percentage()));

        try {
            productImage.setImage(new Image(produit.getImage()));
            selectedImageFile = null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement de l'image", e);
            showError("Erreur lors du chargement de l'image: " + e.getMessage());
        }

        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieComboBox.getItems().setAll(categories);
            categories.stream()
                    .filter(c -> c.getId() == produit.getCategorie_id())
                    .findFirst()
                    .ifPresent(categorieComboBox::setValue);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
            showError("Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de produit");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));
        selectedImageFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedImageFile != null) {
            try {
                productImage.setImage(new Image(selectedImageFile.toURI().toString()));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors du chargement de l'image", e);
                showError("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void generateDescription(ActionEvent event) {
        System.out.println("generateDescription method called");

        String nomProduit = nomProduitField.getText().trim();
        if (nomProduit.isEmpty()) {
            showWarning("Veuillez entrer le nom du produit avant de générer une description.");
            return;
        }

        try {
            System.out.println("Calling OpenAI API for product: " + nomProduit);
            String generatedDescription = generateProductDescription(nomProduit);
            System.out.println("Generated description: " + generatedDescription);

            if (generatedDescription.equals("Description non générée.") || generatedDescription.startsWith("Erreur")) {
                generatedDescription = generateFallbackDescription(nomProduit);
                System.out.println("Fallback description: " + generatedDescription);
                showWarning("Impossible de générer la description via l'API. Une description par défaut a été utilisée.");
            }

            descriptionArea.setText(generatedDescription);

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
                System.out.println("Prompt sent to OpenAI: " + new String(prompt.getBytes(), StandardCharsets.UTF_8));

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String jsonBody = "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}], \"max_tokens\": 100}";
                RequestBody body = RequestBody.create(jsonBody, JSON);
                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + openAiApiKey)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println("API Response Code: " + response.code());

                if (response.code() == 429) {
                    retryCount++;
                    if (retryCount > MAX_RETRIES) {
                        logger.log(Level.SEVERE, "Nombre maximum de tentatives atteint après " + MAX_RETRIES + " essais pour 429 erreur");
                        return "Erreur lors de la génération de la description : Trop de requêtes (Code: 429). Veuillez réessayer plus tard.";
                    }
                    logger.log(Level.WARNING, "Erreur 429 - Tentative " + retryCount + " après " + delay + "ms");
                    Thread.sleep(delay);
                    delay *= 2;
                    continue;
                }

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    logger.log(Level.SEVERE, "Erreur API OpenAI : Code " + response.code() + " - " + response.message() + " - Body: " + errorBody);
                    return "Erreur lors de la génération de la description : " + response.message() + " (Code: " + response.code() + ")";
                }

                String responseBody = response.body().string();
                System.out.println("Raw API Response: " + responseBody);
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
                System.out.println("Parsed description: " + description);
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
        return "Découvrez notre " + productName + ", un produit de qualité supérieure, parfait pour tous vos besoins. Profitez de ses caractéristiques uniques et de son design élégant.";
    }

    @FXML
    public void modifierProduit() {
        try {
            // Validation des champs
            String nomProduit = nomProduitField.getText().trim();
            if (nomProduit.isEmpty()) {
                showWarning("Le nom du produit est obligatoire.");
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
                showWarning("La description est obligatoire.");
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

            String codePromo = codePromoField.getText().trim();
            if (codePromo.isEmpty()) {
                showWarning("Le code promo ne peut pas être vide.");
                return;
            }
            if (!Pattern.matches(CODE_PROMO_PATTERN, codePromo)) {
                showWarning("Le code promo doit contenir exactement 8 caractères alphanumériques.");
                return;
            }

            // Téléverser une nouvelle image sur Cloudinary si une nouvelle image a été sélectionnée
            String imageUrl = produitActuel.getImage();
            if (selectedImageFile != null) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(selectedImageFile, ObjectUtils.asMap(
                            "resource_type", "image",
                            "public_id", "products/" + nomProduit + "_" + System.currentTimeMillis()
                    ));
                    imageUrl = (String) uploadResult.get("secure_url");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        showError("Erreur lors du téléversement de l'image sur Cloudinary.");
                        return;
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Erreur lors du téléversement de l'image sur Cloudinary", e);
                    showError("Erreur lors du téléversement de l'image: " + e.getMessage());
                    return;
                }
            }

            // Mise à jour des données du produit
            produitActuel.setNom_produit(nomProduit);
            produitActuel.setPrix(prix);
            produitActuel.setQuantite(quantite);
            produitActuel.setDescription(description);
            produitActuel.setCategorie_id(selectedCategorie.getId());
            produitActuel.setCode_promo(codePromo);
            produitActuel.setDiscount_percentage(discountPercentage);
            produitActuel.setImage(imageUrl);

            // Modification du produit
            produitService.modifier(produitActuel);

            showInfo("Produit modifié avec succès!");

            if (affichageController != null) {
                affichageController.initialize();
            }

            redirectToProductList();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la modification du produit", e);
            showError("Erreur SQL: " + e.getMessage());
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
            showError("Le " + fieldName + " doit être un nombre décimal valide (ex: 12.5).");
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
        alert.setTitle("Modification réussie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToProductList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Affichierproduitagriculteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomProduitField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Produits");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement de la page AffichierProduit", e);
            showError("Erreur de redirection : " + e.getMessage());
        }
    }

    @FXML
    public void ajouterproduit(ActionEvent actionEvent) {
        // Method kept for compatibility but not used
    }
}