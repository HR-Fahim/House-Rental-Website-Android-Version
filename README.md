# House Rental Android Application

This repository contains the Android version of a full-stack house rental application. The web version is maintained in a separate repository. The project aims to provide a platform for users to rent houses and apartments, with the following key features:<br/>

<sub> ****P.S.*** The project was completed under CSE327 (Software Engineering) course offered by North South University.*<sub/>

## Key Features

- **User Authentication:** The app utilizes Firebase Authentication for user sign up and login functionality.
- **Real-time Database:** Firebase Realtime Database is used to store user data and ad information in real-time.
- **Ad Posting:** Registered users can post ads about house rentals, including property details and images.
- **Voice Input:** Users can conveniently enter data using voice input fields.
- **Image Uploads:** The app allows users to upload images of the properties, which are stored in Firebase Storage.
- **Admin-Controlled Scraping:** An admin-only page enables scraping of data from various house rental ad posting sites. The scraped information, including photos and ad details, is saved in Firebase Storage and Realtime Database.
- **Home Page:** Users can browse and view all posted ads by all users, as well as the scraped ads. A search box with name, price, and location filters is available. Voice search functionality is also supported.
- **Augmented Reality (AR):** The application includes AR functionality for enhanced user experiences, leveraging the OpenCV library. Note that AR features are still under development and require further refinement.
- **Virtual Reality (VR):** Users can explore properties in a virtual reality environment using the "Camera VR" button. However, the VR functionality needs additional enhancements to ensure optimal performance and precision.
- **Location:** Within the VR interface, users can access the "Location" button to view their current location on Google Maps. It is seamlessly integrated with the web version of the full-stack website.

## Future Updates

The following features are planned for future implementation:

- **Automated Room Photography using AR:** Users will be able to automatically capture room photos using augmented reality technology.
- **Enhanced VR Functionality:** The VR experience will be further improved to provide more precise and immersive property exploration.
- **Chatting Option:** A built-in chat feature will be added to facilitate communication between sellers and buyers.

## Technologies and Tools Used

- Programming Languages: Java, XML, JavaScript
- Android Development: Android Studio
- Backend Services: Firebase (Authentication, Realtime Database, Storage)
- Image Processing: OpenCV
- Maps Integration: Google Maps API

## Installation and Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Set up the required dependencies and SDKs.
4. Build and run the app on an Android device or emulator.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Acknowledgements

We would like to acknowledge the following technologies and services that have contributed to the development of this project:

- [Firebase](https://firebase.google.com/) - Backend services for authentication, real-time database, and storage.
- [OpenCV](https://opencv.org/) - Library for image processing and augmented reality functionality.
- [Google Maps API](https://developers.google.com/maps/documentation) - API for integrating location and maps functionality.
- [Google VR SDK](https://developers.google.com/vr) - SDK for virtual reality functionality.

Please feel free to contribute to this project by submitting bug reports, feature requests, or pull requests.

