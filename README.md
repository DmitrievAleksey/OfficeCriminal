1. This project uses one activity (MainActivity as host-activity) and two fragments (CrimeListFragment and CrimeFragment).
2. Activity works in portrait mode (one fragment is used) and in landscape mode (two fragments are used).
3. Recycle View is used to display the list. The adapter uses two types of ViewHolder to display different types of data.
4. The Room library database is used for data storage. The database consists of a single table. Live Data is used to access the database and extract data.
5. Data exchange between fragments is implemented.
6. DialogFragment is used to select the date from the calendar, the time from the timer and to enlarge the photo.
7. The application uses the menu located in the app bar.
8. The application uses phone book data and has created a phone call function.
9. The application uses camera data. The data is stored in the storage created by FileProvider.
