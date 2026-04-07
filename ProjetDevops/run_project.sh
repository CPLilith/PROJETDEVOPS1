#!/bin/bash

# --- CONFIGURATION ---
APP_TITLE="EisenFlow Master Launcher"
MODEL_NAME="tinyllama"

echo -e "\e[1;34m=========================================\e[0m"
echo -e "\e[1;34m   🚀 $APP_TITLE\e[0m"
echo -e "\e[1;34m=========================================\e[0m"

# 1. Lancement d'Ollama Serve en arrière-plan
echo -e "\e[1;32m[1/3]\e[0m 🌐 Lancement du serveur Ollama...."
ollama serve > /dev/null 2>&1 & 
OLLAMA_PID=$!

# On attend que le service soit prêt
sleep 5

# 2. Vérification/Pull du modèle
echo -e "\e[1;32m[2/3]\e[0m 🤖 Vérification du modèle $MODEL_NAME..."
ollama pull $MODEL_NAME

# 3. Lancement de Spring Boot
echo -e "\e[1;32m[3/3]\e[0m 📦 Démarrage de l'application Maven..."
echo ""

# Fonction de nettoyage pour fermer Ollama quand on fait CTRL+C
trap "echo -e '\e[1;31m\nStopping Ollama...\e[0m'; kill $OLLAMA_PID; exit" INT


mvn clean spring-boot:run