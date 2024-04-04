/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, 04/2024
 */
package com.jasonpercus.remarkable;



import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;



/**
 * La classe Connection permet d'établir et de gérer une connexion SFTP (SSH File Transfer Protocol) à un hôte distant.
 * Elle utilise la bibliothèque JSch pour gérer la connexion sécurisée.
 * @author JasonPercus
 * @version 1.0
 */
public class Connection {

    
    
//ATTRIBUTS
    /**
     * Le numéro de port utilisé pour la connexion SFTP.
     */
    private final int port;

    /**
     * Le nom d'utilisateur utilisé pour se connecter à l'hôte distant.
     */
    private final String username;

    /**
     * Le mot de passe associé au nom d'utilisateur pour l'authentification.
     */
    private final String password;

    /**
     * L'adresse de l'hôte distant auquel établir la connexion.
     */
    private final String remoteHost;
    
    /**
     * Correspond à l'objet qui sera mis au courant des déconnexions intempestives de la tablette au client
     */
    private final DisconnectionListener listener;

    /**
     * La session SSH établie avec l'hôte distant.
     */
    private Session session;

    /**
     * Le canal de communication établi dans la session SSH.
     */
    private Channel channel;

    /**
     * Le canal SFTP (SSH File Transfer Protocol) utilisé pour les opérations de transfert de fichiers.
     */
    private ChannelSftp sftp;

    /**
     * Indique si la connexion est établie ou non.
     */
    private boolean connected;
    
    /**
     * Correspond au thread qui joue le rôle de monitor et qui vérifie continuellement si la connexion à la tablette est active ou pas
     */
    private ConnectionMonitor monitor;
    
    /**
     * Détermine qui a détecté la déconnexion de la tablette au programme 1 = Le moniteur détecte la déconnexion, 0 = l'utilisateur se déconnecte manuellement
     */
    private int who;

    
    
//CONSTRUCTOR
    /**
     * Construit une nouvelle instance de Connection.
     *
     * @param username Le nom d'utilisateur pour l'authentification.
     * @param password Le mot de passe pour l'authentification.
     * @param remoteHost L'adresse de l'hôte distant auquel se connecter.
     * @param listener Correspond au listener qui sera mis au courant d'une déconnexion
     */
    public Connection(String username, String password, String remoteHost, DisconnectionListener listener) {
        this.port       = 22;
        this.username   = username;
        this.password   = password;
        this.remoteHost = remoteHost;
        this.connected  = false;
        this.listener   = listener;
    }

    
    
//GETTER
    /**
     * Vérifie si la connexion est établie.
     *
     * @return True si la connexion est établie, sinon false.
     */
    public boolean isConnected() {
        return this.connected;
    }

    
    
//METHODES PUBLICS
    /**
     * Établit une connexion SFTP avec l'hôte distant.
     *
     * @return True si la connexion est établie avec succès, sinon false.
     */
    public boolean connect() {
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");

            JSch jsch = new JSch();
            this.session = jsch.getSession(this.username, this.remoteHost, this.port);
            this.session.setPassword(this.password);
            this.session.setConfig(config);
            this.session.connect(2000); //2 secondes max d'attente
            
            this.channel = session.openChannel("sftp");
            this.channel.connect();
            
            this.sftp = (ChannelSftp) channel;

            this.monitor = new ConnectionMonitor();
            this.monitor.start();
            
            return this.connected = true;
        } catch (JSchException ex) {
            return this.connected = false;
        }
    }

    /**
     * Déconnecte la session SFTP de l'hôte distant.
     *
     * @return True si la déconnexion est réussie, sinon false.
     */
    public boolean disconnect() {
        this.who = 1;
        if(this.sftp != null)
            this.sftp.disconnect();
        if(this.channel != null)
            this.channel.disconnect();
        if(this.session != null)
            this.session.disconnect();
        return !(this.connected = false);
    }

    /**
     * Télécharge l'image "poweroff.png" depuis l'hôte distant.
     *
     * @return Les données du fichier téléchargé sous forme de tableau d'octets.
     */
    public byte[] downloadPoweroff() {
        return download("/usr/share/remarkable/poweroff.png");
    }

    /**
     * Télécharge l'image "batteryempty.png" depuis l'hôte distant.
     *
     * @return Les données du fichier téléchargé sous forme de tableau d'octets.
     */
    public byte[] downloadBatteryempty() {
        return download("/usr/share/remarkable/batteryempty.png");
    }

    /**
     * Télécharge l'image "rebooting.png" depuis l'hôte distant.
     *
     * @return Les données du fichier téléchargé sous forme de tableau d'octets.
     */
    public byte[] downloadRebooting() {
        return download("/usr/share/remarkable/rebooting.png");
    }

    /**
     * Télécharge l'image "sleeping.png" depuis l'hôte distant.
     *
     * @return Les données du fichier téléchargé sous forme de tableau d'octets.
     */
    public byte[] downloadSleeping() {
        return download("/usr/share/remarkable/sleeping.png");
    }

    /**
     * Télécharge l'image "suspended.png" depuis l'hôte distant.
     *
     * @return Les données du fichier téléchargé sous forme de tableau d'octets.
     */
    public byte[] downloadSuspended() {
        return download("/usr/share/remarkable/suspended.png");
    }

    /**
     * Télécharge le fichier spécifié depuis l'hôte distant.
     *
     * @param path Le chemin du fichier sur l'hôte distant.
     * @return Les données du fichier téléchargé sous forme de tableau d'octets.
     */
    public byte[] download(String path) {
        if (isConnected()) {
            try {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                this.sftp.get(path, baos);
                return baos.toByteArray();
            } catch (SftpException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Téléverse un tableau d'octets vers l'hôte distant à l'emplacement spécifié.
     * Si une connexion est établie et que les données sont valides, le téléversement est effectué.
     * Si le téléversement réussit, il est vérifié en téléchargeant à nouveau les données et en les comparant.
     * En cas d'échec, les données originales sont restaurées.
     *
     * @param datas Les données à téléverser sous forme de tableau d'octets.
     * @param path  Le chemin de destination sur l'hôte distant.
     * @return True si le téléversement est réussi, sinon false.
     */
    public boolean upload(byte[] datas, String path) {
        if (isConnected() && datas != null && datas.length > 0) {
            String oldPath = path + ".old";
            try {
                this.sftp.rename(path, oldPath);
                this.sftp.put(new java.io.ByteArrayInputStream(datas), path);
                byte[] newDatas = download(path);
                if (newDatas == null || datas.length != newDatas.length) {
                    this.sftp.rm(path);
                    this.sftp.rename(oldPath, path);
                    return false;
                } else {
                    for (int i = 0; i < datas.length; i++) {
                        if (datas[i] != newDatas[i]) {
                            this.sftp.rm(path);
                            this.sftp.rename(oldPath, path);
                            return false;
                        }
                    }
                    this.sftp.rm(oldPath);
                    return true;
                }
            } catch (SftpException ex) {
                return false;
            }
        }
        return false;
    }
    
    
    
//INTERFACE
    /**
     * Cette interface permet à un objet d'être mis au courant des déconnexions intempestives de l'application à la tablette
     * @author JasonPercus
     * @version 1.0
     */
    public interface DisconnectionListener {
        
        
        
    //METHODE PUBLIC
        /**
         * Lorsque la tablette n'est plus connecté au client
         */
        public void disconnected();
        
        
        
    }
    
    
    
//CLASS PRIVATE
    /**
     * Cette classe représente un thread qui joue le rôle de monitor et qui vérifie continuellement si la connexion à la tablette est active ou pas
     * @author JasonPercus
     * @version 1.0
     */
    private class ConnectionMonitor extends Thread {

        
        
    //CONSTRUCTOR
        /**
         * Crée un objet ConnectionMonitor
         */
        public ConnectionMonitor() {
            super("Connection SFTP Monitor");
            super.setDaemon(true);
            super.setPriority(MIN_PRIORITY);
        }
        
        
        
    //METHODE PUBLIC
        /**
         * Méthode qui vérifie en permanence si oui ou non la tablette est connecté au programme
         */
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            try {
                while (Connection.this.session.isConnected()) {
                    Thread.sleep(1000);
                }
                if(Connection.this.who == 0){
                    Connection.this.sftp      = null;
                    Connection.this.channel   = null;
                    Connection.this.session   = null;
                    Connection.this.connected = false;
                    if(Connection.this.monitor != null)
                        Connection.this.listener.disconnected();
                }
                Connection.this.who = 0;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        
        
        
    }
    
    
    
}