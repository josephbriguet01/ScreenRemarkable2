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
 * La classe Viewer est une boîte de dialogue modale affichant une miniature d'image.
 * Elle permet à l'utilisateur de visualiser une image dans une fenêtre modale avec une fonctionnalité de fermeture au clic.
 * @author JasonPercus
 * @version 1.0
 */
public class Viewer extends javax.swing.JDialog {

    
    
//ATTRIBUTS
    /**
     * La miniature d'image à afficher dans le visualiseur.
     */
    private final Miniature miniature;

    /**
     * Le composant graphique contenant la vue de l'image.
     */
    private final View view;

    /**
     * Les dimensions de l'écran.
     */
    private final java.awt.Dimension dimensionScreen;
    
    
    
    /**
     * Constructeur de la classe Viewer.
     *
     * @param parent La fenêtre parente du visualiseur.
     * @param miniature La miniature d'image à afficher dans le visualiseur.
     * @throws java.io.IOException Si une erreur d'entrée/sortie survient lors de la création du visualiseur.
     */
    public Viewer(java.awt.Frame parent, Miniature miniature) throws java.io.IOException {
        super(parent, true);
        this.miniature       = miniature;
        this.view            = new View();
        this.dimensionScreen = this.view.getSize();
        initComponents();
    }
    
    
    
//METHODE PRIVATE STATIC
    /**
     * Charge les icônes d'image de différentes tailles pour les utiliser dans l'interface utilisateur.
     *
     * @return Une liste d'objets Image contenant les icônes d'image chargées.
     */
    private static java.util.List<java.awt.Image> loadForms_ImageIcons() {
        int[] sizeImg = {16, 32, 48};
        java.util.List<java.awt.Image> images = new java.util.ArrayList<>();
        for(int size : sizeImg) 
            images.add(new javax.swing.ImageIcon(App.class.getResource(String.format("%sicon_%d.png", "/com/jasonpercus/remarkable/assets/", size))).getImage());
        return images;
    }

    
    
//METHODE PRIVATE
    /**
     * Cette méthode est appelée depuis le constructeur pour initialiser le formulaire.
     * AVERTISSEMENT : Ne modifiez PAS ce code. Le contenu de cette méthode est toujours régénéré par l'éditeur de formulaire.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        content = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Viewer: " + this.miniature.getNameFile());
        setIconImages(loadForms_ImageIcons());
        setResizable(false);

        content.add(view, java.awt.BorderLayout.CENTER);
        content.setSize(this.dimensionScreen);
        content.setPreferredSize(this.dimensionScreen);
        content.setMinimumSize(this.dimensionScreen);
        content.setMaximumSize(this.dimensionScreen);

        javax.swing.GroupLayout contentLayout = new javax.swing.GroupLayout(content);
        content.setLayout(contentLayout);
        contentLayout.setHorizontalGroup(
            contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        contentLayout.setVerticalGroup(
            contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        getContentPane().add(content, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel content;
    // End of variables declaration//GEN-END:variables

    
    
//CLASS
    /**
     * La classe View représente le composant graphique contenant la vue de l'image.
     * @author JasonPercus
     * @version 1.0
     */
    private class View extends javax.swing.JPanel {

        
        
    //ATTRIBUT
        /**
         * L'image à afficher dans la vue.
         */
        private final java.awt.image.BufferedImage image;
        
        
        
    //CONSTUCTOR
        /**
         * Constructeur de la classe View.
         *
         * @throws java.io.IOException Si une erreur d'entrée/sortie survient lors de la création de la vue.
         */
        public View() throws java.io.IOException {
            super.setLayout(null);
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(Viewer.this.miniature.getDatasFile());
            java.awt.image.BufferedImage bi  = javax.imageio.ImageIO.read(bis);

            int w = (int) (bi.getWidth()  / 2.3);
            int h = (int) (bi.getHeight() / 2.3);

            this.image = new java.awt.image.BufferedImage(w, h, bi.getType());
            java.awt.Graphics2D g = this.image.createGraphics();
            g.drawImage(bi, 0, 0, w, h, null);
            g.dispose();
            
            java.awt.Dimension dimension = new java.awt.Dimension(w, h);
            super.setSize(dimension);
            super.setPreferredSize(dimension);
            super.setMinimumSize(dimension);
            super.setMaximumSize(dimension);
            super.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            super.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    Viewer.this.dispose();
                }
            });
        }
        
        
        
    //METHODE PUBLIC
        /**
         * Redessine la vue avec l'image.
         *
         * @param g L'objet Graphics utilisé pour dessiner l'image.
         */
        @Override
        public void paintComponent(java.awt.Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
        
        
        
    }

    

}