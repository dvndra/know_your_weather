This repository has been created to fulfill partial requirements for CSCI 571 at USC. The aim is to demonstrate how to <strong>build a Website and Android app.</strong> The topic chosen for this exercise is to enable users do weather search for near future. Also, two separate websites are developed to demonstrate the below two popular approaches used in web development:
* Client-side rendered (CSR) application
* Server-side rendered (SSR) application

Third party APIs as mentioned below are used to gather required data for completion of this exercise:
* Google Geocoding API
* Google AutoComplete
* Google Custom Search API
* Dark Sky API
* IP-API

## Server-side Rendering

In our SSR application, the server compiles PHP script, includes data from third party APIs, and delivers a fully populated HTML page to the client. Please find below the working demonstration of the SSR application.<br>

<img src="http://dswami.freevar.com/git_icons/csci571_hw6.gif"><br>

Click<a href="http://dswami.freevar.com/weather.php"> here </a> to access the website.
The disadvantage with this approach is that every time the user navigate to another route, the server had to do all the work again, leading in delaying the page load. Thus, to overcome this drawback, we will next look into the client-side rendered application. 

## Client-side Rendering

The client-side rendering solution includes combination of HTML5, Bootstrap and Angular to render everything client-side. Here, we redirect the request to a single HTML file and the server will deliver it along with the JavaScript. AJAX requests are then used to request asynchronous data from <strong>NodeJS</strong> server. In addition to it, Bootstrap is used to make website responsive to all screen sizes. The application is hosted at AWS. <br>
Click<a href="http://dvndra.us-east-2.elasticbeanstalk.com/"> here </a> to access the website. <br>

<img src="http://dswami.freevar.com/git_icons/csci571_hw8.gif"><br>



## Android Application
Applied Google’s Material design rules to create Android app using Java. Please find below gif demonstrating some of the functionalities of the app on virtual Android Emulator.<br>

<img src="http://dswami.freevar.com/git_icons/csci571_hw9.gif"><br>
