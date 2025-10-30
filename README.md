# 🌤️ Weather Shopper Automated Tests

[![Java](https://img.shields.io/badge/Java-17+-blue)](https://www.java.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-green)](https://maven.apache.org/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-orange)](https://testng.org/doc/)
[![Allure](https://img.shields.io/badge/Allure-2.21.0-purple)](https://docs.qameta.io/allure/)

Automated **Selenium 4 + TestNG** test suite for [Weather Shopper](https://weathershopper.pythonanywhere.com/).  
Supports **parallel execution**, **retry on failures**, and **remote execution** via **Docker Selenium Grid**.

---

## ⚙️ Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose** (for remote grid)
- **Chrome, Firefox, Edge** (for local execution)

---

## 🧱 Project Structure
    WeatherShopperTest/
    ├─ src/
    │  ├─ main/java/
    │  ├─ test/java/
    │  │  ├─ DriverFactory.java
    │  │  ├─ WeatherShopperTest.java
    │  │  ├─ WeatherShopperActions.java
    │  │  ├─ RetryAnalyzer.java
    │  │  └─ RetryTransformer.java
    ├─ testng.xml
    ├─ pom.xml
    ├─ docker-compose.yml
    ├─ README.md
    └─ .gitignore

## 🚀 Setup Instructions

1. **Clone the repository:**


    git clone <repository-url>
    cd WeatherShopperTest


2. **Build project and download dependencies:**

        mvn clean compile


3. **Optional: Start Selenium Grid via Docker:**
   http://localhost:4444/ui/

        docker-compose up -d
        docker ps   # check if services are up and running


## 🏃‍♂️ Running Tests
**Local Execution**
  
    mvn clean test   # Runs all tests locally sequentially on chrome, firefox and edge by default

**Remote Execution on Selenium Grid**

    mvn clean test -Dremote=true    # Runs all tests on Selenium Grid 

**Parallel Execution**

    mvn clean test -Dparallel=tests -Dthread-count=3    #Run all tests in parallel with 3 threads

## 💳 Retry Failed Tests

    Automatically retries failed tests once using RetryAnalyzer + RetryTransformer.

    Handles flaky scenarios like 5% Stripe payment failures.

    @Test(description = "Shop based on temperature", retryAnalyzer = RetryAnalyzer.class)
    public void shopBasedOnTemperature() { ... }

## 📊 Allure Reports

    mvn allure taget/allure-results     #Generate and view Allure report after tests finish


**Features:**

    Step-wise logging

    Screenshots on failures

    Retry attempts clearly visible

## 🏁 Author

💼 Created by raithdw(Mihai Constantin)

🔧 Built with Java + Selenium + TestNG + Allure + Docker


