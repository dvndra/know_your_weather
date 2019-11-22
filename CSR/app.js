const express = require('express');
var cors = require('cors');

const app = express();
const PORT = process.env.PORT||3000;

// app.use(express.json()); // not needed since we are using get requests and thus no body received
const XMLHttpRequest = require('xhr2');
app.use(cors());

/*
*  Function to perform a get xhr request and send received data to client (acting as middleware)
*/
function xhrRequest(url,callback){
    var xhr = new XMLHttpRequest();   // new HttpRequest instance 
    xhr.open("GET", url, true);
    xhr.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            callback(this.responseText);
        }
    }
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.send();	
}   

// Service to handle google autocomplete requests
app.get('/api/autocomplete/',(req,res)=>{
    let query = req.query.input; 
    let url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" 
                + encodeURI(query) 
                +"&types=(cities)&language=en&key=AIzaSyDvF5eDCmf1fErTVvvD9Robiu7ab2tsOms";
    xhrRequest(url,(result)=>{
        res.send(result);
        console.log(query,result);
    });	
});


// Service to handle google geocoding requests
app.get('/api/geocoding/',(req,res)=>{
    let query = req.query;
    let address = query.Street + ", " + query.City + ", " +query.State;
    let url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodeURI(address)+ "&key=AIzaSyDvF5eDCmf1fErTVvvD9Robiu7ab2tsOms";
    xhrRequest(url,(result)=>{
        res.send(result);
    });	
});


// Service to handle google search engine requests
app.get('/api/search/',(req,res)=>{
    let state = req.query.state;
    let url = "https://www.googleapis.com/customsearch/v1?q="
                + encodeURI(state) + 
                "%20State%20Seal&cx=018166308394853201068:fkeplpnawvt&imgSize=huge&imgType=news&num=1&searchType=image&key=AIzaSyDvF5eDCmf1fErTVvvD9Robiu7ab2tsOms";
    xhrRequest(url,(result)=>{
        res.send(result);
    });
});


// Service to darksky current weather requests
app.get('/api/current/',(req,res)=>{
    let query = req.query;
    let lat_lng = query.latitude + ", " + query.longitude;
    let url = "https://api.darksky.net/forecast/bd0f0892b1567d694a7531e08de78090/" + lat_lng;
    xhrRequest(url,(result)=>{
        res.send(result);
    });
});


// Service to darksky daily weather requests
app.get('/api/daily/',(req,res)=>{
    let query = req.query;
    let lat_lng_time = query.latitude + ", " + query.longitude + ", " + query.time;
    let url = "https://api.darksky.net/forecast/bd0f0892b1567d694a7531e08de78090/" + lat_lng_time;
    var xhr = new XMLHttpRequest();   // new HttpRequest instance 
    xhrRequest(url,(result)=>{
        res.send(result);
    });
});


app.listen(PORT,()=>console.log("Listening",PORT));

