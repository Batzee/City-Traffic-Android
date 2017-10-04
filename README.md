# City-Traffic-Android
An app that uses crowed source data to show current traffic status. 
People can install this app in their mobile phone and can mark the location of traffic and road blocks. 
The data that are older than 15 minutes in the data base(In this case database is Firebase Database) will be deleted.
User data also can be stored in the Firebase.

## Things to do do before start Developing the App

    1.You have to Add your own Google Map API key in the strings.xml in the res folder
    2.The firebase url of your own Firebase database location in the Constants.java class file
    3.In Your Firebase Database section you should see a Node called 'LiveTraffic' when you have marked a location (This one Automatically gets Created and deleted from the code in Android)
    4.In your Firebase 'Storage' Section you must have a folder hierarchy like this 'city_traffic/profile_images/'
     Will look like this, I have added a screenshot showing the 'city_traffic' folder level, inside it you have to create another folder called 'profile_images'(https://www.dropbox.com/s/ksy4zvnm2dkh44u/GitHub_ReadMe_Firebase_Storage.jpg)
     


![Screenshot](/assets/pic1.jpg?raw=true )




