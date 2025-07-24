# UK Carbon Footprint Calculator & Guide

This is an interactive web application designed to help users understand and estimate their carbon footprint based on their daily activities. It provides a personalized dashboard and actionable solutions to reduce environmental impact. The calculations are based on the official 2025 UK Government GHG Conversion Factors.

## Features

* **Interactive Calculator**: Input your weekly activities related to home energy, transport, and consumption.
* **Dynamic Dashboard**: Visualize your estimated carbon footprint with a breakdown by category (Home Energy, Transport, Consumption).
* **Comparison Charts**: See how your choices compare to lower-carbon alternatives.
* **Emission Hotspots**: Learn about activities with a disproportionately high carbon footprint based on UK data.
* **Actionable Solutions**: Discover effective strategies to reduce your footprint, categorized for easy reference (In Your Home, On The Move, Your Consumption Habits).
* **User Data Persistence**: Your input data is saved and loaded automatically using Firebase Firestore, providing a consistent experience.

## Application Structure

The application is structured as a user-centric journey in five parts:
1.  **Introduction**: Engages the user and introduces the purpose of the application.
2.  **Calculator**: An interactive section for personalization, focusing on high-impact areas (Home, Transport, Lifestyle).
3.  **Dashboard**: A dynamic section to visualize the user's results, making the data understandable.
4.  **Hotspots**: An infographic-style section highlighting key emission "hotspots."
5.  **Solutions**: An actionable solutions section to empower the user with reduction strategies.

## Technologies Used

* **HTML5**: For the basic structure and content.
* **Tailwind CSS**: A utility-first CSS framework for rapid UI development.
* **Chart.js**: For creating interactive and dynamic data visualizations (pie and bar charts).
* **Firebase**:
    * **Firebase Authentication**: For anonymous user authentication to enable data persistence.
    * **Cloud Firestore**: A NoSQL cloud database used to store and synchronize user input data in real-time.
* **Google Fonts (Inter)**: For typography.

## Setup and Running Locally

To set up and run this project locally, follow these steps:

1.  **Clone the repository:**
    ```bash
    git clone [Your Repository URL]
    cd uk-carbon-guide
    ```

2.  **Firebase Configuration:**
    This application requires Firebase for data storage and authentication. You will need to set up a Firebase project and obtain its configuration.
    * Go to the [Firebase Console](https://console.firebase.google.com/).
    * Create a new project.
    * Add a web app to your project.
    * Copy your Firebase configuration object. It will look something like this:
        ```javascript
        const firebaseConfig = {
            apiKey: "YOUR_API_KEY",
            authDomain: "YOUR_AUTH_DOMAIN",
            projectId: "YOUR_PROJECT_ID",
            storageBucket: "YOUR_STORAGE_BUCKET",
            messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
            appId: "YOUR_APP_ID"
        };
        ```
    * Enable **Anonymous Authentication** in Firebase Authentication.
    * Enable **Cloud Firestore** in your Firebase project.

3.  **Local Environment Variables (for Canvas Environment compatibility):**
    The script expects `__app_id`, `__firebase_config`, and `__initial_auth_token` to be defined globally. For local development outside of a specific Canvas environment, you would typically replace these with your actual Firebase config or handle them via a build process.

    For a quick local setup, you can manually define these in `public/js/main.js` for testing purposes, but **be careful not to commit sensitive API keys to public repositories**.

    **Option A (For direct local testing - not recommended for production):**
    Directly replace the placeholder variables in `public/js/main.js`:
    ```javascript
    // Global variables provided by the Canvas environment
    const appId = "your-firebase-app-id-from-config"; // Replace with your actual appId
    const firebaseConfig = { /* Your actual Firebase config object here */ }; // Replace with your actual config
    const initialAuthToken = null; // Or your specific custom token if needed
    ```

    **Option B (More robust local development):**
    For a more standard local development setup, you would typically use a development server that can inject environment variables or a build tool like Webpack/Parcel. However, given the provided single HTML/JS file context, direct injection or manual replacement is often the simplest for local testing.

4.  **Open `index.html`:**
    Simply open the `public/index.html` file in your web browser. You can use a simple local HTTP server (e.g., Python's `http.server` or Node.js's `serve`) if you encounter issues with file protocol limitations, especially concerning module imports.

    * **Using Python (if installed):**
        Navigate to the `public` directory in your terminal and run:
        ```bash
        python -m http.server
        ```
        Then open `http://localhost:8000` in your browser.

    * **Using Node.js `serve` (install via `npm i -g serve`):**
        Navigate to the `public` directory in your terminal and run:
        ```bash
        serve
        ```
        Then open the provided URL (e.g., `http://localhost:3000`) in your browser.

## Project Structure
.
├── public/
│   ├── index.html       # Main HTML file for the application
│   ├── css/
│   │   └── style.css    # All custom CSS styles
│   └── js/
│       └── main.js      # All JavaScript logic and Firebase integration
└── README.md            # Project description and setup instructions