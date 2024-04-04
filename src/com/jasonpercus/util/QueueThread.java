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
 * Classe pour la gestion d'une file d'attente de tâches exécutées par un thread unique.
 * @author JasonPercus
 * @version 1.0
 */
public class QueueThread {

    
    
//ATTRIBUT
    /**
     * ExecutorService pour exécuter les tâches dans un thread dédié.
     */
    private final java.util.concurrent.ExecutorService executor;
    
    
    
//CONSTRUCTORS
    /**
     * Constructeur principal qui initialise la QueueThread.
     * @param name Le nom du thread.
     */
    public QueueThread(String name) {
        this(name, Thread.MAX_PRIORITY);
    }

    /**
     * Constructeur qui initialise la QueueThread.
     * @param name Le nom du thread.
     * @param priority La priorité du thread.
     */
    public QueueThread(String name, int priority) {
        this.executor = java.util.concurrent.Executors.newSingleThreadExecutor((Runnable r) -> {
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            thread.setPriority(priority);
            return thread;
        });
        
        // Ajouter un shutdown hook pour fermer le thread personnalisé à la fermeture de l'application
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownNow));
    }
    
    
    
//METHODES PUBLICS
    /**
     * Exécute une tâche asynchrone et renvoie un Future représentant le résultat potentiel de l'exécution.
     * @param task La tâche à exécuter.
     * @param <T> Le type de résultat de la tâche.
     * @return Un Future représentant le résultat potentiel de l'exécution.
     */
    public <T> java.util.concurrent.Future<T> execute(java.util.concurrent.Callable<T> task) {
        return executor.submit(task);
    }
    
    /**
     * Exécute une tâche asynchrone et renvoie un Future représentant le résultat potentiel de l'exécution.
     * @param task La tâche à exécuter.
     * @return Un Future représentant le résultat potentiel de l'exécution.
     */
    public java.util.concurrent.Future<?> execute(Runnable task) {
        return executor.submit(task);
    }
    
    /**
     * Demande la fermeture de l'ExecutorService après l'achèvement de toutes les tâches en cours.
     */
    public void shutdown() {
        executor.shutdown();
    }
    
    /**
     * Tentative d'arrêt en douceur de l'ExecutorService, interrompant immédiatement toutes les tâches en attente et renvoyant une liste des tâches qui n'ont pas été exécutées.
     * @return La liste des tâches en attente qui n'ont pas été exécutées.
     */
    public java.util.List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    
    
}