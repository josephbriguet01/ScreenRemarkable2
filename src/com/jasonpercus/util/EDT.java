/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, 04/2024
 */
package com.jasonpercus.util;



/**
 * Classe utilitaire pour l'exécution de tâches sur l'Event Dispatch Thread (EDT) de Swing.
 * Permet d'exécuter des tâches de manière sûre sur le thread d'interface utilisateur de Swing.
 * @author JasonPercus
 * @version 1.0
 */
public class EDT {
    
    
    
//CONSTRUCTOR
    /**
     * Constructeur privé pour empêcher l'instanciation de la classe.
     */
    private EDT() {
        
    }

    
    
//METHODE PUBLIC
    /**
     * Exécute la tâche spécifiée sur l'Event Dispatch Thread (EDT) de Swing.
     * @param task La tâche à exécuter.
     */
    public static void execute(Runnable task) {
        javax.swing.SwingUtilities.invokeLater(task);
    }

    
    
}