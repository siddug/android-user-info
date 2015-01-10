Assumptions before testing the app:

1) App needs KitKat or Lollipop since it uses step sensors
2) The in-transit check functionality doesn’t work for KitKat, but app works fine
3) For determinig age range and gender app asks for google+ authorization
4) App doesn’t take any more info from social auth
5) best friends, interests are all calculated from the available info in the phone itself
6) the intervals to gather data are set small for testing to be easy
7) the app takes few moments at the start to retrieve phone data for analysis
8) the project is setup in eclipse. and it uses google play services library along with general android setup

Requirements to setup the project:

1) GET android-websockets from https://github.com/koush/android-websockets
2) setup eclipse with required gogle play services
3) change the file names and paths accordingly (I used 'hike' since I developed this as a part of hackAthon conducted by hike

What does the app do:

1) Get the primary addresses used to register the device by user
2) Get call logs and give a rating to the contacts. Select three fav contacts
3) Get Location status from GPS or Net whichever is available and estimate the transit status
4) Use Step sensors (from and above Kitkat) to see if the user is walking/jogging/running/idle
5) Get the data usage of apps and display top three apps user uses
6) Get browser history and 1) display top sites, 2) display top key words/possible interests (taken from site's titles)
7) Get google auth and from publicly accessible info like Age, Gender, Name, Birth date, Relationship status and langauge (whichever are public. Generally finds, age group, language and name)

Tutorial:

1) Tutorial will be written at siddharthagunti.com

Credits:

1) https://github.com/koush/android-websockets by Koush.
2) GPS tracker class from http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial/
