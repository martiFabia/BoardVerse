# 🧩 BoardVerse

BoardVerse is a platform developed for the *Large-Scale and Multi-Structured Databases* course project (A.Y. 2024/2025). <br>
It centralizes and explores information about modern board games, integrating data from **BoardGameGeek (BGG)** and **BoardGameArena (BGA)**.

---

## 📌 Features

* 🗂️ **Games Catalog**: Explore metadata of 2,000+ top-ranked board games.
* ⭐ **User Reviews**: Discover user-generated ratings and comments.
* 💬 **Forums**: Dive into community discussions with threads and messages per game.
* 🏆 **Tournaments**: Browse upcoming events and configurations from BGA.
* 🧠 **Recommendations**: Suggest friends and games using graph-based traversal.


## 🧱 Technologies

* **MongoDB**: Stores structured document data (games, reviews, users, tournaments).
* **Neo4j**: Manages graph data for relationships and recommendations.
* **Python**: Custom scripts for scraping, API integration, and dataset generation.
* **Java (Spring Boot)**: RESTful backend exposing services for the application.


## 📊 Dataset Overview

* **\~500 MB of processed data**
* Collected from **BGG (via XML API)** and **BGA (via scraping)**
* Enhanced with realistic synthetic data where unavailable
* Covers: Games, Users, Reviews, Forums, Tournaments


## 🚀 Setup Instructions

1. **Clone the repository**
2. **Set up MongoDB and Neo4j instances**:

   * Ensure MongoDB is running locally or in your environment.
   * Ensure Neo4j is running and accessible for graph-based services.

3. **Run backend services** (/java-backend)

4. **(Optional) Launch dataset generation scripts** (/data-scripts)

## 📄 Documentation

Full instructions and API documentation are available in the [`/docs`](./docs) folder.


## ✨ Credits

Developed by 
* Martina Fabiani
* Tommaso Falaschi 
* Emanuele Respino

