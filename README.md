# 📱 Countries App

A sample Android app built with **Kotlin**, following **Clean Architecture** and **MVVM** pattern.  
The app demonstrates modular design, separation of concerns, and best practices for testability and maintainability.

[![Coverage](https://codecov.io/gh/ramprasad441/Countries/branch/master/graph/badge.svg)](https://codecov.io)
---

## 🧱 Architecture

This project follows **Clean Architecture** and **MVVM** with a clear separation of layers:
|-- presentation       // UI Layer (Activities, Fragments, ViewModels)
|-- domain             // Business Logic (UseCases, Entities, Interfaces)
|-- data               // Data Layer (Repositories, Network & Cache sources)
|-- core               // Common utils, base classes


### ✅ Key Principles:
- **Single Responsibility** for each module/layer
- **Dependency Inversion** via interfaces
- **Testability** at every layer
- **Unidirectional flow** of data

---

## 🔧 Tech Stack

- **Kotlin**
- **Jetpack ViewModel & LiveData**
- **Retrofit** – for networking
- **OkHttp** – for logging
- **Coroutines / Flow** – for async & reactive programming
- **JUnit**, **MockK**, **Robolectric** – for unit & UI testing
- **Material Design Components**
- **ViewBinding** – for UI binding

---

## 🚀 Features

- Fetches country data from REST API
- Displays list of countries with a RecyclerView
- Handles loading, success, and error states
- 100% Kotlin + modular and testable

---

## 🧪 Testing

- **Unit Tests**: Domain layer & ViewModels tested using `JUnit`, `MockK`, and `Truth`
- **UI Tests**: Robolectric-based tests for activities & fragments
- **Code Coverage**: Kover (Kotlin-native) with automated reporting, CI enforcement, and integration with Codecov & SonarQube
Run tests:
```view coverage
./gradlew clean testDebugUnitTest koverXmlReport coverageCheck
