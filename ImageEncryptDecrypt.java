/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author P V S Sri Harshita
 */
//package test1;
//import static img.decryptText;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.security.*;
//import javax.crypto.spec.SecretKeySpec;

public class ImageEncryptDecrypt extends JFrame {
    private JLabel label;
    private JButton encryptButton, decryptButton, selectImageButton, saveEncryptedButton, saveDecryptedButton,
            cancelButton;
    private JRadioButton aesRadioButton, desRadioButton, tdesRadioButton;
    private ButtonGroup algorithmGroup;
    private File imageFile;
    private BufferedImage originalImage, encryptedImage, decryptedImage;
    private JLabel imagePreviewLabel;
    private static final int PREVIEW_WIDTH = 300;
    private static final int PREVIEW_HEIGHT = 200;
    private SecretKey currentKey; // Store the current encryption key
    private String currentAlgorithm; // Store the current algorithm

    public ImageEncryptDecrypt() {
        setTitle("Image Encryption & Decryption");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize GUI components
        label = new JLabel("Select an Image to Encrypt/Decrypt", SwingConstants.CENTER);
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        selectImageButton = new JButton("Select Image");
        cancelButton = new JButton("Cancel");
        saveEncryptedButton = new JButton("Save Encrypted Image");
        saveDecryptedButton = new JButton("Save Decrypted Image");
        imagePreviewLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        aesRadioButton = new JRadioButton("AES");
        desRadioButton = new JRadioButton("DES");
        tdesRadioButton = new JRadioButton("3DES");
        algorithmGroup = new ButtonGroup();
        algorithmGroup.add(aesRadioButton);
        algorithmGroup.add(desRadioButton);
        algorithmGroup.add(tdesRadioButton);

        // Set radio buttons layout
        JPanel algorithmPanel = new JPanel();
        algorithmPanel.add(aesRadioButton);
        algorithmPanel.add(desRadioButton);
        algorithmPanel.add(tdesRadioButton);

        // Set button defaults
        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        saveEncryptedButton.setEnabled(false);
        saveDecryptedButton.setEnabled(false);
        cancelButton.setEnabled(false);

        // Layout setup
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Add components to the layout
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(label, gbc);

        gbc.gridy = 1;
        add(imagePreviewLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(selectImageButton, gbc);

        gbc.gridx = 1;
        add(cancelButton, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(algorithmPanel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 4;
        gbc.gridx = 0;
        add(encryptButton, gbc);

        gbc.gridx = 1;
        add(decryptButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        add(saveEncryptedButton, gbc);

        gbc.gridx = 1;
        add(saveDecryptedButton, gbc);

        // Button action listeners
        selectImageButton.addActionListener(e -> selectImage());
        cancelButton.addActionListener(e -> cancelImage());
        encryptButton.addActionListener(e -> encryptImage());
        decryptButton.addActionListener(e -> decryptImage());
        saveEncryptedButton.addActionListener(e -> saveEncryptedImage());
        saveDecryptedButton.addActionListener(e -> saveDecryptedImage());
    }

    private void updateImagePreview(BufferedImage image) {
        if (image != null) {
            Image scaledImage = image.getScaledInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(scaledImage));
            imagePreviewLabel.setText("");
        } else {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No image selected");
        }
    }

    private void cancelImage() {
        imageFile = null;
        originalImage = null;
        encryptedImage = null;
        decryptedImage = null;
        currentKey = null;
        currentAlgorithm = null;
        updateImagePreview(null);
        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        saveEncryptedButton.setEnabled(false);
        saveDecryptedButton.setEnabled(false);
        cancelButton.setEnabled(false);
        label.setText("Select an Image to Encrypt/Decrypt");
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image to Encrypt");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            imageFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(imageFile);
                updateImagePreview(originalImage);
                encryptButton.setEnabled(true);
                decryptButton.setEnabled(true);
                saveEncryptedButton.setEnabled(false);
                saveDecryptedButton.setEnabled(false);
                cancelButton.setEnabled(true);
                label.setText("Image Selected: " + imageFile.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage());
            }
        }
    }

    private void encryptImage() {
        try {
            if (imageFile == null) {
                JOptionPane.showMessageDialog(this, "Please select an image first.");
                return;
            }

            // Generate new key for encryption
            currentAlgorithm = getSelectedAlgorithm();
            currentKey = generateSecretKey(currentAlgorithm);

            BufferedImage imageToEncrypt = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    int rgb = originalImage.getRGB(x, y);
                    int encryptedRgb = encryptPixel(rgb, currentKey, currentAlgorithm);
                    imageToEncrypt.setRGB(x, y, encryptedRgb);
                }
            }

            encryptedImage = imageToEncrypt;
            updateImagePreview(encryptedImage);
            saveEncryptedButton.setEnabled(true);
            saveDecryptedButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Image encrypted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error encrypting image: " + ex.getMessage());
        }
    }

    private void decryptImage() {
        try {
            if (imageFile == null) {
                JOptionPane.showMessageDialog(this, "Please select an image first.");
                return;
            }

            if (encryptedImage == null) {
                JOptionPane.showMessageDialog(this, "Please encrypt the image first.");
                return;
            }

            if (currentKey == null) {
                JOptionPane.showMessageDialog(this, "Encryption key not found. Please encrypt the image first.");
                return;
            }

            // Verify the algorithm matches
            String selectedAlgorithm = getSelectedAlgorithm();
            if (!selectedAlgorithm.equals(currentAlgorithm)) {
                JOptionPane.showMessageDialog(this,
                        "Please select the same algorithm used for encryption: " + currentAlgorithm);
                return;
            }

            // Create a new image for decryption
            BufferedImage imageToDecrypt = new BufferedImage(encryptedImage.getWidth(), encryptedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            // Process each pixel
            for (int y = 0; y < encryptedImage.getHeight(); y++) {
                for (int x = 0; x < encryptedImage.getWidth(); x++) {
                    int rgb = encryptedImage.getRGB(x, y);
                    int decryptedRgb = decryptPixel(rgb, currentKey, currentAlgorithm);
                    imageToDecrypt.setRGB(x, y, decryptedRgb);
                }
            }

            decryptedImage = imageToDecrypt;
            updateImagePreview(decryptedImage);
            saveDecryptedButton.setEnabled(true);
            saveEncryptedButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Image decrypted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error decrypting image: " + ex.getMessage());
        }
    }

    private int encryptPixel(int rgb, SecretKey secretKey, String algorithm) throws Exception {
        // Extract RGB components
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        // Create a block for encryption (8 bytes for DES/3DES, 16 for AES)
        int blockSize = algorithm.equals("AES") ? 16 : 8;
        byte[] paddedBytes = new byte[blockSize];

        // Store RGB values in the first three bytes
        paddedBytes[0] = (byte) red;
        paddedBytes[1] = (byte) green;
        paddedBytes[2] = (byte) blue;

        // Use NoPadding to avoid padding issues
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(paddedBytes);

        // Take only the first three bytes for RGB
        int encryptedRed = encryptedBytes[0] & 0xFF;
        int encryptedGreen = encryptedBytes[1] & 0xFF;
        int encryptedBlue = encryptedBytes[2] & 0xFF;

        // Reconstruct the pixel with the encrypted RGB values
        return (encryptedRed << 16) | (encryptedGreen << 8) | encryptedBlue;
    }

    private int decryptPixel(int rgb, SecretKey secretKey, String algorithm) throws Exception {
        // Extract RGB components from encrypted pixel
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        // Create a block for decryption (8 bytes for DES/3DES, 16 for AES)
        int blockSize = algorithm.equals("AES") ? 16 : 8;
        byte[] paddedBytes = new byte[blockSize];

        // Store encrypted RGB values in the first three bytes
        paddedBytes[0] = (byte) red;
        paddedBytes[1] = (byte) green;
        paddedBytes[2] = (byte) blue;

        // Use NoPadding to match encryption
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(paddedBytes);

        // Take only the first three bytes for RGB
        int decryptedRed = decryptedBytes[0] & 0xFF;
        int decryptedGreen = decryptedBytes[1] & 0xFF;
        int decryptedBlue = decryptedBytes[2] & 0xFF;

        // Reconstruct the pixel with the decrypted RGB values
        return (decryptedRed << 16) | (decryptedGreen << 8) | decryptedBlue;
    }

    private String getSelectedAlgorithm() {
        if (aesRadioButton.isSelected()) {
            return "AES";
        } else if (desRadioButton.isSelected()) {
            return "DES";
        } else if (tdesRadioButton.isSelected()) {
            return "DESede"; // 3DES
        }
        return "AES"; // default
    }

    private SecretKey generateSecretKey(String algorithm) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = null;
        if (algorithm.equals("AES")) {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128); // AES key size 128 bits
        } else if (algorithm.equals("DES")) {
            keyGenerator = KeyGenerator.getInstance("DES");
            keyGenerator.init(56); // DES key size 56 bits
        } else if (algorithm.equals("DESede")) {
            keyGenerator = KeyGenerator.getInstance("DESede");
            keyGenerator.init(168); // 3DES key size 168 bits
        }
        return keyGenerator.generateKey();
    }

    private void saveEncryptedImage() {
        try {
            if (encryptedImage != null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Encrypted Image");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    ImageIO.write(encryptedImage, "png", file);
                    JOptionPane.showMessageDialog(this, "Encrypted image saved successfully.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving encrypted image: " + e.getMessage());
        }
    }

    private void saveDecryptedImage() {
        try {
            if (decryptedImage != null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Decrypted Image");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    ImageIO.write(decryptedImage, "png", file);
                    JOptionPane.showMessageDialog(this, "Decrypted image saved successfully.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving decrypted image: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ImageEncryptDecrypt().setVisible(true);
        });
    }
}
