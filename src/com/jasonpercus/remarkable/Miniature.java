/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, 04/2024
 */
package com.jasonpercus.remarkable;



/**
 * Une classe représentant une miniature d'image affichée dans un JPanel.
 * La miniature est une version réduite de l'image originale, avec des dimensions spécifiques. 
 * Cette classe étend JPanel et implémente une logique pour créer une miniature à partir des données d'image fournies.
 *
 * @author JasonPercus
 * @version 1.0
 */
public class Miniature extends javax.swing.JPanel {
    
    
    
//CONSTANTES
    /**
     * Correspond à la largeur de l'image originale
     */
    private final static int WIDTH_ORIGINAL_IMG  = 1404;
    
    /**
     * Correspond à la hauteur de l'image originale
     */
    private final static int HEIGHT_ORIGINAL_IMG = 1872;
    
    /**
     * Correspond à la proportion de la miniature par rapport à la taille de l'image originale
     */
    private final static int PROPORTION = 6;

    
    
//ATTRIBUTS
    /**
     * Les données de l'image.
     */
    private final byte[] datas;

    /**
     * Le nom du fichier de l'image.
     */
    private final String nameFile;

    /**
     * L'image miniature.
     */
    private java.awt.image.BufferedImage image;

    
    
//CONSTRUCTOR
    /**
     * Construit une nouvelle instance de Miniature avec les données d'image spécifiées et le nom du fichier.L'image est redimensionnée pour devenir une miniature.
     *
     * @param parent Correspond à la fenêtre parente {@link App}
     * @param datas Les données de l'image à partir desquelles créer la miniature.
     * @param name Le nom du fichier de l'image.
     * @throws java.io.IOException Si une erreur d'entrée-sortie se produit lors de la lecture des données de l'image.
     * @throws MiniatureException Si les dimensions de l'image ne correspondent pas à celles attendues pour une miniature.
     */
    public Miniature(java.awt.Frame parent, byte[] datas, String name) throws java.io.IOException, MiniatureException {
        super.setLayout(null);
        super.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        super.setToolTipText("Click on me to view larger image");
        this.datas    = datas;
        this.nameFile = name;

        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(datas);
        java.awt.image.BufferedImage bi  = javax.imageio.ImageIO.read(bis);

        if (bi.getWidth() == WIDTH_ORIGINAL_IMG && bi.getHeight() == HEIGHT_ORIGINAL_IMG) {
            int w = bi.getWidth()  / PROPORTION;
            int h = bi.getHeight() / PROPORTION;

            this.image = new java.awt.image.BufferedImage(w, h, bi.getType());
            java.awt.Graphics2D g = this.image.createGraphics();
            g.drawImage(bi, 0, 0, w, h, null);
            g.dispose();
        } else {
            throw new MiniatureException(String.format("The image must be %dpx in width and %dpx in height !", WIDTH_ORIGINAL_IMG, HEIGHT_ORIGINAL_IMG));
        }
        
        super.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    Viewer viewer = new Viewer(parent, Miniature.this);
                    viewer.setVisible(true);
                } catch (java.io.IOException ex) {}
            }
        });
    }

    
    
//GETTERS
    /**
     * Récupère les données de l'image.
     *
     * @return Les données de l'image sous forme de tableau d'octets.
     */
    public byte[] getDatasFile() {
        return datas;
    }

    /**
     * Récupère le nom du fichier de l'image.
     *
     * @return Le nom du fichier de l'image.
     */
    public String getNameFile() {
        return this.nameFile;
    }

    
    
//METHODE PUBLIC
    /**
     * Redéfinit la méthode paintComponent pour dessiner l'image miniature.
     *
     * @param g L'objet Graphics utilisé pour dessiner.
     */
    @Override
    public void paintComponent(java.awt.Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    
    
//CLASS
    /**
     * Une exception spécifique pour les erreurs liées à la création de miniatures d'images.
     *
     * @author JasonPercus
     * @version 1.0
     */
    public class MiniatureException extends Exception {

        
        
    //CONSTRUCTOR
        /**
         * Construit une nouvelle instance de MiniatureException avec le message d'erreur spécifié.
         *
         * @param message Le message d'erreur associé à cette exception.
         */
        public MiniatureException(String message) {
            super(message);
        }

        
        
    }

    
    
}