<div align="center">

# Open-Source Software & Repository Explorer

A modern Android application built to discover, explore, and bookmark open-source GitHub repositories using modern Android development practices.

![Android](https://img.shields.io/badge/Android-Kotlin-green)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</div>

---

## Overview

Open-Source Software & Repository Explorer is a developer-focused Android application that enables users to discover trending repositories, search GitHub projects, browse issues and pull requests, read README files natively, and maintain an offline collection of bookmarked repositories.

The project demonstrates modern Android engineering practices including:

* Clean Architecture
* MVVM
* Multi-Module Architecture
* Jetpack Compose
* Kotlin Coroutines & Flow
* Room Database
* Retrofit
* Hilt Dependency Injection
* Offline-First Data Strategy

---

## Features

### Repository Discovery

* Browse trending repositories
* Search public GitHub repositories
* Filter by language and popularity
* Infinite scrolling support

### Repository Details

* Repository statistics
* Contributors
* Languages
* Stars & Forks
* Open Issues
* Pull Requests

### Native Markdown Reader

* README rendering
* Syntax highlighting
* Dark mode support
* GitHub-flavored markdown

### Bookmarks

* Save repositories locally
* Offline access
* Fast retrieval using Room Database

### Error Handling

* Offline support
* API rate-limit handling
* Retry mechanisms
* User-friendly error states

---

## Architecture

```text
Presentation (Compose UI)
        │
        ▼
ViewModel
        │
        ▼
Use Cases
        │
        ▼
Repository
 ┌─────────────┴─────────────┐
 ▼                           ▼
Remote Data Source      Local Data Source
(GitHub API)            (Room Database)
```

---

## Tech Stack

### Language

* Kotlin

### UI

* Jetpack Compose
* Material 3
* Navigation Compose

### Architecture

* Clean Architecture
* MVVM
* Multi-Module

### Networking

* Retrofit
* OkHttp
* Kotlin Serialization

### Local Storage

* Room Database

### Dependency Injection

* Hilt

### Async

* Coroutines
* StateFlow
* SharedFlow

### Image Loading

* Coil

---

## API

GitHub Public API

Example endpoint:

```http
GET https://api.github.com/search/repositories?q=stars:>10000+language:kotlin&sort=stars&order=desc
```

No authentication is required for public repository exploration.

---

## Project Structure

```text
app/

core/
├── common
├── data
├── database
├── domain
├── network
└── designsystem

feature/
├── home
├── search
├── repository
├── issues
├── bookmarks
└── settings
```

---

## Getting Started

### Prerequisites

* Android Studio Hedgehog or newer
* JDK 17+
* Android SDK 35

### Clone Repository

```bash
git clone https://github.com/akashydv04/open-source-repository-explorer.git
```

### Open Project

```bash
Open Android Studio
File → Open
Select Project Directory
```

### Run

```bash
Sync Gradle
Build Project
Run Application
```

---

## Screenshots

> Add application screenshots here

---

## Future Improvements

* Repository contribution insights
* GitHub authentication
* Release tracking
* Repository comparison
* AI-powered repository summaries

---

## Author

Akash Yadav

Senior Software Engineer | Android Developer

LinkedIn: https://linkedin.com/in/akashydv04

GitHub: https://github.com/akashydv04

---

## License

MIT License
