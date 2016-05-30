# PopularMovies

##Overview

Most of us can relate to kicking back on the couch and enjoying a movie with friends and family. 
In this project, build an app to allow users to discover the most popular movies playing. 
We will split the development of this app in two stages. 

###Stage 1

* Present the user with a grid arrangement of movie posters upon launch.
* Allow your user to change sort order via a setting:
* The sort order can be by most popular or by highest-rated
* Allow the user to tap on a movie poster and transition to a details screen with additional information such as:
original title
* movie poster image thumbnail
* A plot synopsis (called overview in the api)
* user rating (called vote_average in the api)
* release date

###Stage 2

 * add more information to your movie details view:
 * allow users to view and play trailers ( either in the youtube app or a web browser).
 * allow users to read reviews of a selected movie.
 * also allow users to mark a movie as a favorite in the details view by tapping a button(star). 
This is for a local movies collection that you will maintain and does not require an API request.
 * modify the existing sorting criteria for the main view to include an additional pivot to show their favorites collection.
 * Lastly, optimize your app experience for tablet.
 
[Popular Movies App implementation Guide can be found here]
(https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.7sxo8jefdfll)

## Libraries

* [Picasso](http://square.github.io/picasso/), Image downloading and caching library for Android
* [Butterknife](http://jakewharton.github.io/butterknife/), Field and method binding for Android views


## themoviedb.org API key

themoviedb.org API key is defined in the file "security_string.xml" 
under the folder app/src/main/res/values. The API key can't be shared in public.  
Please create the file with the following file template.
File "app/src/main/res/values/security_string.xml" as follows

```
<resources>
    <string name="API_key">[KEY DEFINE HERE]</string>
</resources/>
```

