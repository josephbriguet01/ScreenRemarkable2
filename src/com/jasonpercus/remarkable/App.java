/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, 04/2024
 */
package com.jasonpercus.remarkable;



import com.jasonpercus.util.MET;
import com.jasonpercus.util.EDT;
import com.jasonpercus.remarkable.Miniature.MiniatureException;



/**
 * La classe App représente l'application principale de l'interface utilisateur.
 * Elle étend JFrame et gère diverses fonctionnalités de l'application, telles que la connexion à un hôte distant, l'affichage de miniatures d'images, etc.
 * @author JasonPercus
 * @version 1.0
 */
public class App extends javax.swing.JFrame {
    
    
    
//CONSTANTES
    /**
     * Correspond au tooltiptext pour le champ IP
     */
    private final static String TOOLTIP_IP   = "<html>Corresponds to the IP address of the Remarkable 2 tablet.<br><br>\n<p style=\"color: rgb(51, 51, 51)\">\n<i>The ip address is usually found in:</i><br>\n<b>Settings</b> > <b>General</b> > <b>Help</b> > <b>About</b> > <b>Copyrights and licenses</b></p>";
    
    /**
     * Correspond au tooltiptext pour le champ User
     */
    private final static String TOOLTIP_USER = "<html>Corresponds to the user of the Remarkable 2 tablet.<br><br>\n<p style=\"color: rgb(51, 51, 51)\">\n<i>The user is usually found in:</i><br>\n<b>Settings</b> > <b>General</b> > <b>Help</b> > <b>About</b> > <b>Copyrights and licenses</b></p>";
    
    /**
     * Correspond au tooltiptext pour le champ Password
     */
    private final static String TOOLTIP_PASS = "<html>Corresponds to the password of the Remarkable 2 tablet.<br><br>\n<p style=\"color: rgb(51, 51, 51)\">\n<i>The password is usually found in:</i><br>\n<b>Settings</b> > <b>General</b> > <b>Help</b> > <b>About</b> > <b>Copyrights and licenses</b></p>";
    
    
    
    
//ATTRIBUTS
    /**
     * L'instance de la fenêtre d'attente utilisée pour indiquer une tâche en cours.
     */
    private Wait waiter;
    
    /**
     * L'instance de connexion utilisée pour communiquer avec l'hôte distant.
     */
    private Connection connection;
    
    /**
     * La miniature originale "poweroff".
     */
    private Miniature miniaturePowerOff_original;
    
    /**
     * La miniature originale "batteryempty".
     */
    private Miniature miniatureBatteryEmpty_original;
    
    /**
     * La miniature originale "rebooting".
     */
    private Miniature miniatureRebooting_original;
    
    /**
     * La miniature originale "suspended".
     */
    private Miniature miniatureSuspended_original;
    
    /**
     * La nouvelle miniature "poweroff".
     */
    private Miniature miniaturePowerOff;
    
    /**
     * La nouvelle miniature "batteryempty".
     */
    private Miniature miniatureBatteryEmpty;
    
    /**
     * La nouvelle miniature "rebooting".
     */
    private Miniature miniatureRebooting;
    
    /**
     * La nouvelle miniature "suspended".
     */
    private Miniature miniatureSuspended;
    
    /**
     * La miniature affichée sur la gauche et qui représente l'image originale
     */
    private Miniature leftMiniature;
    
    /**
     * La miniature affichée sur la droite et qui représente la nouvelle image
     */
    private Miniature rightMiniature;
    
    
    
//CONSTRUCTOR
    /**
     * Construit une nouvelle instance de l'application.
     * Initialise les composants de l'interface utilisateur.
     */
    public App() {
        initComponents();
        passField.requestFocus();
    }
    
    
    
//MAIN
    /**
     * Lance l'application
     * @param args Correspond aux éventuels arguments (non utilisé dans notre cas)
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        java.awt.EventQueue.invokeLater(() -> {
            new App().setVisible(true);
        });
    }

    
    
//METHODE PUBLIC
    /**
     * Ferme la fenêtre d'application.
     * Si une connexion est établie, tente de se déconnecter en affichant une fenêtre d'attente.
     */
    @Override
    public void dispose() {
        if(this.connection != null && this.connection.isConnected())
            this.<Void>execute("Disconnection in progress...", () -> {
                this.connection.disconnect();
                return null;
            });
        super.dispose();
    }
    
    
    
//METHODES PRIVATES STATICS
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
    
    /**
     * Charge une image des assets
     * 
     * @param pathResource Correspond au chemin de l'image à charger
     * @return Retourne l'image sous la forme d'un tableau de bytes
     */
    private static byte[] loadOriginalImg(String pathResource) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.BufferedInputStream   bis  = new java.io.BufferedInputStream(App.class.getResourceAsStream(pathResource));
            int value;
            while ((value = bis.read()) > -1) {
                baos.write(value);
            }
            bis.close();
            baos.close();
            return baos.toByteArray();
        } catch (java.io.IOException ex) {
            return null;
        }
    }
    
    
    
//METHODES PRIVATES
    /**
     * Exécute une tâche sur un thread séparé tout en affichant une fenêtre d'attente à l'utilisateur.
     * Une fois la tâche terminée, la fenêtre d'attente est fermée.
     *
     * @param message Le message à afficher dans la fenêtre d'attente.
     * @param code La tâche à exécuter.
     * @param <T> Le type de résultat retourné par la tâche.
     * @return Le résultat de la tâche.
     * @throws ExecutionException Si une erreur se produit pendant l'exécution de la tâche.
     */
    private <T> T execute(String message, java.util.concurrent.Callable<T> code) throws ExecutionException {
        javax.swing.SwingWorker<T, Void> worker = new javax.swing.SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return code.call();
            }

            @Override
            protected void done() {
                if(App.this.waiter != null) {
                    App.this.waiter.dispose();
                    App.this.waiter = null;
                }
            }
        };
        worker.execute();
        if (this.waiter != null) {
            this.waiter.dispose();
            this.waiter = null;
        }
        this.waiter = new Wait(this, message);
        this.waiter.setVisible(true);
        try {
            return worker.get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException ex) {
            if (this.waiter != null) {
                this.waiter.dispose();
                this.waiter = null;
            }
            throw new ExecutionException(ex);
        }
    }
    
    /**
     * Affiche une boîte de dialogue avec le titre, le message et le type de message spécifiés.
     *
     * @param title Le titre de la boîte de dialogue.
     * @param message Le message à afficher dans la boîte de dialogue.
     * @param messageType Le type de message à afficher (par exemple, INFORMATION_MESSAGE, WARNING_MESSAGE, ERROR_MESSAGE, etc.).
     */
    private void showMessageDialog(String title, String message, int messageType) {
        javax.swing.JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    /**
     * Méthode appelée lorsque la connexion à la tablette Remarkable est établie avec succès.
     * Télécharge les miniatures originales des images depuis l'hôte distant et les affiche dans l'interface utilisateur.
     * En cas d'échec, affiche un message d'erreur.
     */
    private void connected() {
        MET.execute(() -> {
            try {
                this.miniatureBatteryEmpty_original = new Miniature(this, this.connection.downloadBatteryempty(), "batteryempty.png");
                EDT.execute(() -> {
                    showLeftMiniature(this.miniatureBatteryEmpty_original);
                });
                this.miniaturePowerOff_original     = new Miniature(this, this.connection.downloadPoweroff(), "poweroff.png");
                this.miniatureRebooting_original    = new Miniature(this, this.connection.downloadRebooting(), "rebooting.png");
                this.miniatureSuspended_original    = new Miniature(this, this.connection.downloadSuspended(), "suspended.png");
            } catch (java.io.IOException | MiniatureException ex) {
                showMessageDialog("Program error", "The program is no longer compatible with the internal version of the Remarkable 2 tablet !", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Méthode appelée lors de la déconnexion de la tablette Remarkable.
     * Supprime les miniatures affichées dans l'interface utilisateur et réinitialise les attributs correspondants.
     */
    private void disconnected() {
        if (this.leftMiniature != null) {
            leftImagePanel.remove(this.leftMiniature);
            leftImagePanel.revalidate();
            leftImagePanel.repaint();
        }

        if (this.rightMiniature != null) {
            rightImagePanel.remove(this.rightMiniature);
            rightImagePanel.revalidate();
            rightImagePanel.repaint();
        }

        this.miniaturePowerOff_original     = null;
        this.miniatureBatteryEmpty_original = null;
        this.miniatureRebooting_original    = null;
        this.miniatureSuspended_original    = null;
        this.miniaturePowerOff              = null;
        this.miniatureBatteryEmpty          = null;
        this.miniatureRebooting             = null;
        this.miniatureSuspended             = null;
        this.leftMiniature                  = null;
        this.rightMiniature                 = null;

        batteryEmptyButton.setSelected(true);
        passField.requestFocus();
    }
    
    /**
     * Lorsque la déconnexion est détectée (l'utilisateur n'a pas touché au logiciel, mais matériellement parlant, il n'y plus de connexion active avec la tablette)
     */
    private void disconnectionDetected() {
        if (this.connection != null) {
            java.awt.CardLayout panel = (java.awt.CardLayout) this.connectionCardPanel.getLayout();
            this.connection = null;
            this.connectionButton.setText("Connect");
            this.connectionButton.setSelected(false);
            this.ipField.setEnabled(true);
            this.userField.setEnabled(true);
            this.passField.setEnabled(true);
            panel.show(this.connectionCardPanel, "NO_CONNECTED");
            disconnected();
        } else {
            this.ipField.setEnabled(true);
            this.userField.setEnabled(true);
            this.passField.setEnabled(true);
        }
    }
    
    /**
     * Affiche la miniature spécifiée dans le panneau d'image de gauche de l'interface utilisateur.
     *
     * @param miniature La miniature à afficher.
     */
    private void showLeftMiniature(Miniature miniature) {
        if (this.leftMiniature != null)
            leftImagePanel.remove(this.leftMiniature);
        this.leftMiniature = miniature;
        if (miniature != null)
            leftImagePanel.add(miniature, java.awt.BorderLayout.CENTER);
        leftImagePanel.revalidate();
        leftImagePanel.repaint();
    }
    
    /**
     * Affiche la miniature spécifiée dans le panneau d'image de droite de l'interface utilisateur.
     *
     * @param miniature La miniature à afficher.
     */
    private void showRightMiniature(Miniature miniature) {
        if (this.rightMiniature != null)
            rightImagePanel.remove(this.rightMiniature);
        this.rightMiniature = miniature;
        if (miniature != null)
            rightImagePanel.add(miniature, java.awt.BorderLayout.CENTER);
        rightImagePanel.revalidate();
        rightImagePanel.repaint();
    }
    
    /**
     * Initialise les composants graphique de la fenêtre
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlButtonSectionGroup = new javax.swing.ButtonGroup();
        ipLabel = new javax.swing.JLabel();
        ipField = new com.jasonpercus.network.swing.JIP();
        userLabel = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        passLabel = new javax.swing.JLabel();
        passField = new javax.swing.JTextField();
        connectionButton = new javax.swing.JToggleButton();
        separator = new javax.swing.JSeparator();
        connectionCardPanel = new javax.swing.JPanel();
        noConnectedPanel = new javax.swing.JPanel();
        noConnectedLabel = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        controlButtonSectionPanel = new javax.swing.JPanel();
        batteryEmptyButton = new javax.swing.JToggleButton();
        powerOffButton = new javax.swing.JToggleButton();
        rebootingButton = new javax.swing.JToggleButton();
        suspendedButton = new javax.swing.JToggleButton();
        contentSectionPanel = new javax.swing.JPanel();
        leftSectionPanel = new javax.swing.JPanel();
        leftActionButtonPanel = new javax.swing.JPanel();
        uploadLeftButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        leftImageSectionPanel = new javax.swing.JPanel();
        leftImagePanel = new javax.swing.JPanel();
        rightSectionPanel = new javax.swing.JPanel();
        rightActionButtonPanel = new javax.swing.JPanel();
        uploadRightButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        defaultButton = new javax.swing.JButton();
        rightImageSectionPanel = new javax.swing.JPanel();
        rightImagePanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Screen Remarkable 2");
        setIconImages(loadForms_ImageIcons());
        setResizable(false);

        ipLabel.setText("IP Address: ");
        ipLabel.setToolTipText(TOOLTIP_IP);

        ipField.setText("10.11.99.1");
        ipField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        ipField.setMaximumSize(new java.awt.Dimension(2147483647, 22));
        ipField.setMinimumSize(new java.awt.Dimension(33, 22));
        ipField.setPreferredSize(new java.awt.Dimension(161, 22));
        ipField.setSize(new java.awt.Dimension(161, 22));
        ipField.setToolTipText(TOOLTIP_IP);

        userLabel.setText("User: ");
        userLabel.setToolTipText(TOOLTIP_USER);

        userField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        userField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        userField.setText("root");
        userField.setToolTipText(TOOLTIP_USER);
        userField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userFieldFocusGained(evt);
            }
        });
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionButtonActionPerformed(evt);
            }
        });

        passLabel.setText("Password: ");
        passLabel.setToolTipText(TOOLTIP_PASS);

        passField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        passField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        passField.setToolTipText(TOOLTIP_PASS);
        passField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passFieldFocusGained(evt);
            }
        });
        passField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionButtonActionPerformed(evt);
            }
        });

        connectionButton.setText("Connect");
        connectionButton.setToolTipText("Allows you to connect with the SFTP protocol to the Remarkable 2 tablet.");
        connectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionButtonActionPerformed(evt);
            }
        });

        connectionCardPanel.setLayout(new java.awt.CardLayout());

        noConnectedPanel.setLayout(new java.awt.BorderLayout());

        noConnectedLabel.setBackground(new java.awt.Color(51, 51, 51));
        noConnectedLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        noConnectedLabel.setForeground(new java.awt.Color(255, 255, 255));
        noConnectedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noConnectedLabel.setText("NO CONNECTED !");
        noConnectedLabel.setToolTipText("<html>You are not connected. Please provide the correct login credentials.<br><br>\n<p style=\"color: rgb(51, 51, 51)\">\n<i>The correct login credentials are usually founds in:</i><br><b>Settings</b> > <b>General</b> > <b>Help</b> > <b>About</b> > <b>Copyrights and licenses</b></p>");
        noConnectedLabel.setOpaque(true);
        noConnectedPanel.add(noConnectedLabel, java.awt.BorderLayout.CENTER);

        connectionCardPanel.add(noConnectedPanel, "NO_CONNECTED");

        content.setLayout(new java.awt.BorderLayout());

        controlButtonSectionPanel.setLayout(new java.awt.GridLayout(1, 4));

        controlButtonSectionGroup.add(batteryEmptyButton);
        batteryEmptyButton.setSelected(true);
        batteryEmptyButton.setText("Battery empty");
        batteryEmptyButton.setToolTipText("Concerns the image that will be displayed when the tablet battery level is critical.");
        batteryEmptyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batteryEmptyButtonActionPerformed(evt);
            }
        });
        controlButtonSectionPanel.add(batteryEmptyButton);

        controlButtonSectionGroup.add(powerOffButton);
        powerOffButton.setText("Power off");
        powerOffButton.setToolTipText("Concerns the image that will be displayed when the tablet is turned off.");
        powerOffButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerOffButtonActionPerformed(evt);
            }
        });
        controlButtonSectionPanel.add(powerOffButton);

        controlButtonSectionGroup.add(rebootingButton);
        rebootingButton.setText("Rebooting");
        rebootingButton.setToolTipText("Concerns the image that will be displayed when the tablet is restarting.");
        rebootingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rebootingButtonActionPerformed(evt);
            }
        });
        controlButtonSectionPanel.add(rebootingButton);

        controlButtonSectionGroup.add(suspendedButton);
        suspendedButton.setText("Suspended");
        suspendedButton.setToolTipText("Concerns the image that will be displayed when the tablet is in standby.");
        suspendedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suspendedButtonActionPerformed(evt);
            }
        });
        controlButtonSectionPanel.add(suspendedButton);

        content.add(controlButtonSectionPanel, java.awt.BorderLayout.PAGE_START);

        contentSectionPanel.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        leftSectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Original"));
        leftSectionPanel.setToolTipText("Corresponds to the section where images from the tablet will be displayed.");
        leftSectionPanel.setLayout(new java.awt.BorderLayout());

        leftActionButtonPanel.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        uploadLeftButton.setText("Upload");
        uploadLeftButton.setToolTipText("<html>Uploads the old tablet image.<br><br>\n<p style=\"color: rgb(255, 0, 0)\"><b>Warning:</b> If the application is closed and the image has not been saved first, then the image will be permanently lost.</p>");
        uploadLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadLeftButtonActionPerformed(evt);
            }
        });
        leftActionButtonPanel.add(uploadLeftButton);

        saveAsButton.setText("Save as...");
        saveAsButton.setToolTipText("Saves the current image of the tablet to the PC.");
        saveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsButtonActionPerformed(evt);
            }
        });
        leftActionButtonPanel.add(saveAsButton);

        leftSectionPanel.add(leftActionButtonPanel, java.awt.BorderLayout.PAGE_END);

        leftImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 0)));
        leftImagePanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        leftImagePanel.setMaximumSize(new java.awt.Dimension(234, 312));
        leftImagePanel.setMinimumSize(new java.awt.Dimension(234, 312));
        leftImagePanel.setPreferredSize(new java.awt.Dimension(234, 312));
        leftImagePanel.setLayout(new java.awt.BorderLayout());
        leftImageSectionPanel.add(leftImagePanel);

        leftSectionPanel.add(leftImageSectionPanel, java.awt.BorderLayout.CENTER);

        contentSectionPanel.add(leftSectionPanel);

        rightSectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("New"));
        rightSectionPanel.setToolTipText("Corresponds to the section where images from the PC will be displayed.");
        rightSectionPanel.setLayout(new java.awt.BorderLayout());

        rightActionButtonPanel.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        uploadRightButton.setText("Upload");
        uploadRightButton.setToolTipText("Uploads the new tablet image.");
        uploadRightButton.setEnabled(false);
        uploadRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadRightButtonActionPerformed(evt);
            }
        });
        rightActionButtonPanel.add(uploadRightButton);

        loadButton.setText("Load");
        loadButton.setToolTipText("Finds and loads a png image to the PC.");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });
        rightActionButtonPanel.add(loadButton);

        defaultButton.setText("Default");
        defaultButton.setToolTipText("Loads the default image of the Remarkable 2 tablet.");
        defaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultButtonActionPerformed(evt);
            }
        });
        rightActionButtonPanel.add(defaultButton);

        rightSectionPanel.add(rightActionButtonPanel, java.awt.BorderLayout.PAGE_END);

        rightImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 204, 0)));
        rightImagePanel.setToolTipText("Finds and loads a png image to the PC.");
        rightImagePanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rightImagePanel.setMaximumSize(new java.awt.Dimension(234, 312));
        rightImagePanel.setMinimumSize(new java.awt.Dimension(234, 312));
        rightImagePanel.setPreferredSize(new java.awt.Dimension(234, 312));
        rightImagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rightImagePanelMouseClicked(evt);
            }
        });
        rightImagePanel.setLayout(new java.awt.BorderLayout());
        rightImageSectionPanel.add(rightImagePanel);

        rightSectionPanel.add(rightImageSectionPanel, java.awt.BorderLayout.CENTER);

        contentSectionPanel.add(rightSectionPanel);

        content.add(contentSectionPanel, java.awt.BorderLayout.CENTER);

        connectionCardPanel.add(content, "CONNECTED");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ipLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionButton, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(connectionCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {ipField, passField, userField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(passLabel)
                        .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(connectionButton))
                    .addComponent(userLabel)
                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ipLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {connectionButton, ipField, ipLabel, passField, passLabel, userField, userLabel});

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Événement déclenché lorsque le champ de saisie de l'utilisateur gagne le focus.
     * Sélectionne tout le texte dans le champ de saisie.
     *
     * @param evt L'événement de focus associé à cet événement.
     */
    private void userFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userFieldFocusGained
        String user = userField.getText();
        userField.setSelectionStart(0);
        userField.setSelectionEnd(user.length());
    }//GEN-LAST:event_userFieldFocusGained

    /**
     * Événement déclenché lorsque le champ de saisie du mot de passe gagne le focus.
     * Sélectionne tout le texte dans le champ de saisie.
     *
     * @param evt L'événement de focus associé à cet événement.
     */
    private void passFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passFieldFocusGained
        String password = passField.getText();
        passField.setSelectionStart(0);
        passField.setSelectionEnd(password.length());
    }//GEN-LAST:event_passFieldFocusGained

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton de connexion.
     * Connecte ou déconnecte l'application de l'hôte distant en fonction de l'état actuel.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void connectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionButtonActionPerformed
        ipField.setEnabled(false);
        userField.setEnabled(false);
        passField.setEnabled(false);
        String ip       = ipField.getIP().getIpv4();
        String user     = userField.getText();
        String password = passField.getText();
        
        if(evt.getSource() instanceof javax.swing.JTextField)
            this.connectionButton.setSelected(true);
        
        if(this.connectionButton.isSelected()) {
            if(this.connection == null) {
                java.awt.CardLayout panel = (java.awt.CardLayout) this.connectionCardPanel.getLayout();
                
                Connection c = new Connection(user, password, ip, () -> {
                    disconnectionDetected();
                });
                
                boolean connected = this.<Boolean>execute("Connecting to the Remarkable 2 tablet...", () -> {
                    return c.connect();
                });
                
                if(connected) {
                    this.connectionButton.setText("Disconnect");
                    this.connectionButton.setSelected(true);
                    this.connection = c;
                    panel.show(this.connectionCardPanel, "CONNECTED");
                    connected();
                } else {
                    this.connectionButton.setText("Connect");
                    this.connectionButton.setSelected(false);
                    this.ipField.setEnabled(true);
                    this.userField.setEnabled(true);
                    this.passField.setEnabled(true);
                    this.connection = null;
                    panel.show(this.connectionCardPanel, "NO_CONNECTED");
                    showMessageDialog("Connection", "The program cannot connect to the Remarkable 2 tablet !\nCheck your settings.", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else {
                this.connection = null;
                this.ipField.setEnabled(true);
                this.userField.setEnabled(true);
                this.passField.setEnabled(true);
            }
        } else {
            if(this.connection != null) {
                java.awt.CardLayout panel = (java.awt.CardLayout) this.connectionCardPanel.getLayout();
                
                boolean disconnected = this.<Boolean>execute("Disconnection in progress...", () -> {
                    return this.connection.disconnect();
                });
                
                if(disconnected) {
                    this.connection = null;
                    this.connectionButton.setText("Connect");
                    this.connectionButton.setSelected(false);
                    this.ipField.setEnabled(true);
                    this.userField.setEnabled(true);
                    this.passField.setEnabled(true);
                    panel.show(this.connectionCardPanel, "NO_CONNECTED");
                    disconnected();
                } else {
                    this.connectionButton.setText("Disconnect");
                    this.connectionButton.setSelected(true);
                    panel.show(this.connectionCardPanel, "CONNECTED");
                    showMessageDialog("Connection", "The program cannot disconnect from the Remarkable 2 tablet !", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else {
                this.ipField.setEnabled(true);
                this.userField.setEnabled(true);
                this.passField.setEnabled(true);
            }
        }
    }//GEN-LAST:event_connectionButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Battery Empty".
     * Affiche la miniature de batterie vide dans l'interface utilisateur.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void batteryEmptyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batteryEmptyButtonActionPerformed
        javax.swing.JToggleButton button = (javax.swing.JToggleButton) evt.getSource();
        if(button.isSelected()) {
            showLeftMiniature(miniatureBatteryEmpty_original);
            showRightMiniature(miniatureBatteryEmpty);
        }
        uploadRightButton.setEnabled(rightMiniature != null);
    }//GEN-LAST:event_batteryEmptyButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Power Off".
     * Affiche la miniature de mise hors tension dans l'interface utilisateur.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void powerOffButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerOffButtonActionPerformed
        javax.swing.JToggleButton button = (javax.swing.JToggleButton) evt.getSource();
        if(button.isSelected()) {
            showLeftMiniature(miniaturePowerOff_original);
            showRightMiniature(miniaturePowerOff);
        }
        uploadRightButton.setEnabled(rightMiniature != null);
    }//GEN-LAST:event_powerOffButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Rebooting".
     * Affiche la miniature de redémarrage dans l'interface utilisateur.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void rebootingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rebootingButtonActionPerformed
        javax.swing.JToggleButton button = (javax.swing.JToggleButton) evt.getSource();
        if(button.isSelected()) {
            showLeftMiniature(miniatureRebooting_original);
            showRightMiniature(miniatureRebooting);
        }
        uploadRightButton.setEnabled(rightMiniature != null);
    }//GEN-LAST:event_rebootingButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Suspended".
     * Affiche la miniature de suspension dans l'interface utilisateur.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void suspendedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suspendedButtonActionPerformed
        javax.swing.JToggleButton button = (javax.swing.JToggleButton) evt.getSource();
        if(button.isSelected()) {
            showLeftMiniature(miniatureSuspended_original);
            showRightMiniature(miniatureSuspended);
        }
        uploadRightButton.setEnabled(rightMiniature != null);
    }//GEN-LAST:event_suspendedButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Upload" de l'image originale.
     * Télécharge la miniature de gauche vers l'hôte distant.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void uploadLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadLeftButtonActionPerformed
        if(this.connection != null && this.connection.isConnected() && leftMiniature != null) {
            boolean uploaded = this.<Boolean>execute("The original image is being uploaded...", () -> {
                return this.connection.upload(leftMiniature.getDatasFile(), "/usr/share/remarkable/" + leftMiniature.getNameFile());
            });
            if(uploaded) {
                showMessageDialog("Uploading", "The original image has been successfully uploaded !", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                showMessageDialog("Uploading", "The original image was not uploaded !", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_uploadLeftButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Upload" de la nouvelle image.
     * Télécharge la miniature de droite vers l'hôte distant.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void uploadRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadRightButtonActionPerformed
        if(this.connection != null && this.connection.isConnected() && rightMiniature != null) {
            boolean uploaded = this.<Boolean>execute("The new image is being uploaded...", () -> {
                return this.connection.upload(rightMiniature.getDatasFile(), "/usr/share/remarkable/" + rightMiniature.getNameFile());
            });
            if(uploaded) {
                showMessageDialog("Uploading", "The new image has been successfully uploaded !", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                showMessageDialog("Uploading", "The new image was not uploaded !", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_uploadRightButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Save As".
     * Enregistre la miniature originale actuellement affichée dans l'interface utilisateur sur le disque local.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsButtonActionPerformed
        if(this.leftMiniature != null) {
            byte[] datas = this.leftMiniature.getDatasFile();
            String name = this.leftMiniature.getNameFile();
            if(datas != null) {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
                chooser.setDialogTitle("Save as...");
                chooser.setApproveButtonText("Save as...");
                chooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
                if(chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    try {
                        try (java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(new java.io.File(chooser.getSelectedFile().getAbsolutePath() + java.io.File.separator + name)))) {
                            bos.write(datas, 0, datas.length);
                        }
                        showMessageDialog("Save as...", "The original image has been saved successfully !", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } catch (java.io.IOException ex) {
                        showMessageDialog("Save as...", "The original image cannot be saved !", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            }
        }
    }//GEN-LAST:event_saveAsButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton "Load".
     * Charge une image à partir du disque local et l'affiche en tant que nouvelle image.
     *
     * @param evt L'événement d'action associé à cet événement.
     */
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle("Load");
        chooser.setApproveButtonText("Load");
        chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        if(chooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            if(chooser.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".png")){
                try {
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    java.io.BufferedInputStream   fis = new java.io.BufferedInputStream(new java.io.FileInputStream(chooser.getSelectedFile()));
                    byte[] buffer = new byte[1024]; // Taille du tampon de lecture
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }   
                    fis.close();
                    bos.close();
                    byte[] datas = bos.toByteArray();

                    if(batteryEmptyButton.isSelected()) {
                        showRightMiniature(this.miniatureBatteryEmpty = new Miniature(this, datas, "batteryempty.png"));
                    } else if (powerOffButton.isSelected()) {
                        showRightMiniature(this.miniaturePowerOff = new Miniature(this, datas, "poweroff.png"));
                    } else if (rebootingButton.isSelected()) {
                        showRightMiniature(this.miniatureRebooting = new Miniature(this, datas, "rebooting.png"));
                    } else if (suspendedButton.isSelected()) {
                        showRightMiniature(this.miniatureSuspended = new Miniature(this, datas, "suspended.png"));
                    }
                } catch (java.io.IOException | MiniatureException ex) {
                    showMessageDialog("Loading", ex.getMessage(), javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else
                showMessageDialog("Loading", "This is not a PNG image !", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        uploadRightButton.setEnabled(rightMiniature != null);
    }//GEN-LAST:event_loadButtonActionPerformed

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le panneau de charge d'une nouvelle image venant du PC
     * Charge une image à partir du disque local et l'affiche en tant que nouvelle image.
     * 
     * @param evt L'événement d'action associé à cet événement.
     */
    private void rightImagePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rightImagePanelMouseClicked
        loadButtonActionPerformed(null);
    }//GEN-LAST:event_rightImagePanelMouseClicked

    /**
     * Événement déclenché lorsqu'un utilisateur clique sur le bouton Default
     * Charge l'image par défaut de la tablette Remarkable 2
     * 
     * @param evt L'événement d'action associé à cet événement.
     */
    private void defaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultButtonActionPerformed
        try {
            if (batteryEmptyButton.isSelected()) {
                byte[] datas = loadOriginalImg("/com/jasonpercus/remarkable/assets/batteryempty_original.png");
                if(datas != null)
                    showRightMiniature(this.miniatureBatteryEmpty = new Miniature(this, datas, "batteryempty.png"));
            } else if (powerOffButton.isSelected()) {
                byte[] datas = loadOriginalImg("/com/jasonpercus/remarkable/assets/poweroff_original.png");
                if(datas != null)
                    showRightMiniature(this.miniaturePowerOff = new Miniature(this, datas, "poweroff.png"));
            } else if (rebootingButton.isSelected()) {
                byte[] datas = loadOriginalImg("/com/jasonpercus/remarkable/assets/rebooting_original.png");
                if(datas != null)
                    showRightMiniature(this.miniatureRebooting = new Miniature(this, datas, "rebooting.png"));
            } else if (suspendedButton.isSelected()) {
                byte[] datas = loadOriginalImg("/com/jasonpercus/remarkable/assets/suspended_original.png");
                if(datas != null)
                    showRightMiniature(this.miniatureSuspended = new Miniature(this, datas, "suspended.png"));
            }
        } catch (java.io.IOException | MiniatureException ex) {}
        uploadRightButton.setEnabled(rightMiniature != null);
    }//GEN-LAST:event_defaultButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton batteryEmptyButton;
    private javax.swing.JToggleButton connectionButton;
    private javax.swing.JPanel connectionCardPanel;
    private javax.swing.JPanel content;
    private javax.swing.JPanel contentSectionPanel;
    private javax.swing.ButtonGroup controlButtonSectionGroup;
    private javax.swing.JPanel controlButtonSectionPanel;
    private javax.swing.JButton defaultButton;
    private com.jasonpercus.network.swing.JIP ipField;
    private javax.swing.JLabel ipLabel;
    private javax.swing.JPanel leftActionButtonPanel;
    private javax.swing.JPanel leftImagePanel;
    private javax.swing.JPanel leftImageSectionPanel;
    private javax.swing.JPanel leftSectionPanel;
    private javax.swing.JButton loadButton;
    private javax.swing.JLabel noConnectedLabel;
    private javax.swing.JPanel noConnectedPanel;
    private javax.swing.JTextField passField;
    private javax.swing.JLabel passLabel;
    private javax.swing.JToggleButton powerOffButton;
    private javax.swing.JToggleButton rebootingButton;
    private javax.swing.JPanel rightActionButtonPanel;
    private javax.swing.JPanel rightImagePanel;
    private javax.swing.JPanel rightImageSectionPanel;
    private javax.swing.JPanel rightSectionPanel;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JToggleButton suspendedButton;
    private javax.swing.JButton uploadLeftButton;
    private javax.swing.JButton uploadRightButton;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables
    
    
    
//CLASS
    /**
     * Une exception qui indique une erreur lors de l'exécution d'une opération.
     * @author JasonPercus
     * @version 1.0
     */
    private class ExecutionException extends RuntimeException {



    //CONSTRUCTOR
        /**
         * Construit une nouvelle instance d'ExecutionException avec la cause spécifiée.
         *
         * @param cause La cause de cette exception.
         */
        public ExecutionException(Throwable cause) {
            super(cause);
        }



    }

    
    
}