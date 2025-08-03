/*
 * AI-Based Recommendation System
 * Author: Rahulkumar Arya
 * Date: August 2, 2025
 *
 * This Java program provides content-based recommendations
 * for electronic products based on user preferences (custom implementation).
 */

import java.util.*;

public class RecommendationSystem {
    // Sample user preferences: userId, productId, rating (1-5)
    private static final int[][] USER_RATINGS = {
        {1, 101, 5}, {1, 102, 3}, {1, 103, 2},
        {2, 101, 4}, {2, 104, 5},
        {3, 102, 4}, {3, 103, 5}, {3, 104, 3}
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===== AI-Based Recommendation System =====");
        if (ProductData.PRODUCTS == null) {
            System.out.println("Error: Product data not loaded.");
            scanner.close();
            return;
        }

        System.out.println("Available Products:");
        for (int i = 0; i < ProductData.PRODUCTS.length; i++) {
            Product p = ProductData.PRODUCTS[i];
            System.out.println((i + 1) + ". " + p.name + " (ID: " + p.id + ")");
        }

        System.out.print("\nSelect a product by entering its number (1-" + ProductData.PRODUCTS.length + "): ");
        int choice = scanner.nextInt();
        while (choice < 1 || choice > ProductData.PRODUCTS.length) {
            System.out.print("Invalid choice. Enter again: ");
            choice = scanner.nextInt();
        }

        Product selected = ProductData.PRODUCTS[choice - 1];
        System.out.println("\nYou selected: " + selected.name);

        int userId = 1; // Using default user for demonstration
        int topN = ProductData.PRODUCTS.length;
        List<ProductScore> recommendations = recommendProducts(userId, topN);
        System.out.println("\nRecommended products for you:");
        for (ProductScore ps : recommendations) {
            System.out.println("Product: " + ps.product.name + " (ID: " + ps.product.id + ") | Score: " + String.format("%.3f", ps.score));
        }

        scanner.close();
    }

    // Recommend top N products for a user based on content similarity
    public static List<ProductScore> recommendProducts(int userId, int topN) {
        Set<Integer> ratedProductIds = new HashSet<>();
        Map<Integer, Integer> userRatings = new HashMap<>();
        for (int[] entry : USER_RATINGS) {
            if (entry[0] == userId) {
                ratedProductIds.add(entry[1]);
                userRatings.put(entry[1], entry[2]);
            }
        }

        // Build user profile vector (average of liked product vectors)
        List<String> likedDescriptions = new ArrayList<>();
        for (Product p : ProductData.PRODUCTS) {
            if (userRatings.getOrDefault(p.id, 0) >= 4) {
                likedDescriptions.add(p.description);
            }
        }
        Map<String, Double> userProfile = averageTfIdfVector(likedDescriptions);

        // Score all products not yet rated by user
        List<ProductScore> scores = new ArrayList<>();
        for (Product p : ProductData.PRODUCTS) {
            if (!ratedProductIds.contains(p.id)) {
                Map<String, Double> prodVec = tfIdfVector(p.description);
                double score = cosineSimilarity(userProfile, prodVec);
                scores.add(new ProductScore(p, score));
            }
        }
        scores.sort((a, b) -> Double.compare(b.score, a.score));
        return scores.subList(0, Math.min(topN, scores.size()));
    }

    // --- Simple TF-IDF and Cosine Similarity ---
    private static Map<String, Double> tfIdfVector(String text) {
        Map<String, Integer> tf = new HashMap<>();
        String[] tokens = text.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+");
        for (String token : tokens) {
            if (!token.isEmpty()) tf.put(token, tf.getOrDefault(token, 0) + 1);
        }
        Map<String, Double> tfidf = new HashMap<>();
        for (String word : tf.keySet()) {
            tfidf.put(word, tf.get(word) * 1.0); // IDF=1 for demo
        }
        return tfidf;
    }

    private static Map<String, Double> averageTfIdfVector(List<String> texts) {
        Map<String, Double> avg = new HashMap<>();
        if (texts.isEmpty()) return avg;
        for (String text : texts) {
            Map<String, Double> vec = tfIdfVector(text);
            for (String k : vec.keySet()) {
                avg.put(k, avg.getOrDefault(k, 0.0) + vec.get(k));
            }
        }
        for (String k : avg.keySet()) {
            avg.put(k, avg.get(k) / texts.size());
        }
        return avg;
    }

    private static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allKeys = new HashSet<>(v1.keySet());
        allKeys.addAll(v2.keySet());
        double dot = 0, norm1 = 0, norm2 = 0;
        for (String k : allKeys) {
            double a = v1.getOrDefault(k, 0.0);
            double b = v2.getOrDefault(k, 0.0);
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }
        return (norm1 == 0 || norm2 == 0) ? 0 : dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}

class ProductScore {
    public final Product product;
    public final double score;
    public ProductScore(Product product, double score) {
        this.product = product;
        this.score = score;
    }
}
