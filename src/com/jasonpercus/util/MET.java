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
 * Classe utilitaire pour la gestion des tâches exécutées de manière asynchrone par un thread unique MET (Main Execution Thread).
 * @author JasonPercus
 * @version 1.0
 */
public class MET {
    
    
    
//CONSTANTE
    /**
     * QueueThread utilisé pour exécuter les tâches.
     */
    private final static QueueThread EXECUTOR = new QueueThread("MET");
    
    
    
//CONSTRUCTOR
    /**
     * Constructeur privé pour empêcher l'instanciation de la classe.
     */
    private MET() {
        
    }
    
    
    
//METHODES PUBLICS STATICS
    /**
     * Exécute une tâche asynchrone et renvoie un Future représentant le résultat potentiel de l'exécution.
     * @param task La tâche à exécuter.
     * @param <T> Le type de résultat de la tâche.
     * @return Un Future représentant le résultat potentiel de l'exécution.
     */
    public static <T> java.util.concurrent.Future<T> execute(java.util.concurrent.Callable<T> task) {
        return EXECUTOR.execute(task);
    }
    
    /**
     * Exécute une tâche asynchrone et renvoie un Future représentant le résultat potentiel de l'exécution.
     * @param task La tâche à exécuter.
     * @return Un Future représentant le résultat potentiel de l'exécution.
     */
    public static java.util.concurrent.Future<?> execute(Runnable task) {
        return EXECUTOR.execute(task);
    }

    
    
}