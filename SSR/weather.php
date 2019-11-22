<?php

	/*
	Function to return location(string) or null from POST request made by client with form
	*/
	function get_location(){
		if (isset($_POST['json_location'])){
			$location = json_decode($_POST['json_location']);
			$str_location = $location->Street.", ".$location->City.", ".$location->State;
			return $str_location;	
		}	
		return null;
	}
	
	/*
	Function to return latitude and longitude for a given address using google geocoding api
	*/
	function get_lat_lng() {	
		$url = "https://maps.googleapis.com/maps/api/geocode/xml?address=".urlencode(get_location())."&key=AIzaSyDvF5eDCmf1fErTVvvD9Robiu7ab2tsOms";
		$str_response = file_get_contents($url);
		$xml = simplexml_load_string($str_response);
		if($xml->status!="OK"){
			return array(null,null);
		}
		$lat = $xml->result->geometry->location->lat;
		$lng = $xml->result->geometry->location->lng;
		return array($lat,$lng);					
	}

	/*
	Function to get weekly weather data for provided latitude and longitude
	*/
	function get_wkly_weather($lat_lng_arry) {	
		
		$url = "https://api.forecast.io/forecast/bd0f0892b1567d694a7531e08de78090/". 
								$lat_lng_arry[0].",".$lat_lng_arry[1]."?exclude=minutely,hourly,alerts,flags";
		$wkly_response = json_decode(file_get_contents($url));
		return $wkly_response;
	}

	/*
	Function to get daily weather data for provided latitude, longitude and datetime
	*/
	function get_daily_weather($lat_lng_time) {	
		
		$url = "https://api.darksky.net/forecast/bd0f0892b1567d694a7531e08de78090/". 
								$lat_lng_time[0].",".$lat_lng_time[1].",".$lat_lng_time[2]."?exclude=minutely";
		$daily_response = json_decode(file_get_contents($url));
		return $daily_response;
	}
?>
		
<!DOCTYPE html>
<html lang="en">

<head>

	<meta charset="utf-8"/>
	<title>Weather App</title>
	<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
	<link rel="shortcut icon" href="">
	<script>

		var wkly_json;
		var daily_json;
		var city;

		/* Function to clear form data and weather cards */
		function clearSite(){
			document.getElementById("error_msg_container").setAttribute("hidden","true");
			document.getElementById("manual").Street.value="";
			document.getElementById("manual").City.value="";
			document.getElementById("manual").State.value="";
			document.getElementsByName("curr_loc")[0].checked = false;
			document.getElementById("manual").Street.disabled=false;
			document.getElementById("manual").City.disabled=false;
			document.getElementById("manual").State.disabled=false;
			document.getElementById("error_msg_container").hidden = true;
			document.getElementById("curr_weather_card").innerHTML="";
			document.getElementById("curr_weather_card").hidden = true;
			document.getElementById("wkly_weather_card").innerHTML="";
			document.getElementById("daily_weather_title").innerHTML="";
			document.getElementById("daily_weather_card").innerHTML="";
			document.getElementById("daily_weather_card").hidden = true;
			document.getElementById("hourly_weather_title").innerHTML="";
			document.getElementById("temp_chart_button").innerHTML="";
			document.getElementById("temp_chart").innerHTML="";
		}

		/* Function to disable form data when current location is (de)selected */
		function changeDisabilityForm(){
			document.getElementById("manual").Street.value="";
			document.getElementById("manual").City.value="";
			document.getElementById("manual").State.value="";

			if (document.getElementsByName("curr_loc")[0].checked==true){
				document.getElementById("manual").Street.disabled=true;
				document.getElementById("manual").City.disabled=true;
				document.getElementById("manual").State.disabled=true;	
			}
			else {
				document.getElementById("manual").Street.disabled=false;
				document.getElementById("manual").City.disabled=false;
				document.getElementById("manual").State.disabled=false;	
			}
		}

		/* 
		Function to validate user provided input (if needed) and submit post request containing form data/ curr location data. 
		Also, extract the CURRENT AND WEEKLY weather data from page sent by server, and 
		SET RESULT IN WKLY_JSON 
		*/
		function validateAndSubmit(){

			document.getElementById("error_msg_container").setAttribute("hidden","true");
			document.getElementById("curr_weather_card").innerHTML="";
			document.getElementById("curr_weather_card").hidden = true;
			document.getElementById("wkly_weather_card").innerHTML="";
			document.getElementById("daily_weather_title").innerHTML="";
			document.getElementById("daily_weather_card").innerHTML="";
			document.getElementById("daily_weather_card").hidden = true;
			document.getElementById("hourly_weather_title").innerHTML="";
			document.getElementById("temp_chart_button").innerHTML="";
			document.getElementById("temp_chart").innerHTML="";

			// REQUESTING DATA IF CURRENT LOCATION IS SELECTED
			if (document.getElementsByName("curr_loc")[0].checked==true) {
				
				function receiveJSON(){
					// sending lat-lng and receiving corresponding wkly_json
					var xhr = new XMLHttpRequest();   // new HttpRequest instance 
					var url = "index.php";
					xhr.open("POST", url, true);
					xhr.onreadystatechange = function() {
						if (this.readyState == 4 && this.status == 200) {
							//console.log("Success");
							var parser = new DOMParser();
							var wkly_doc = parser.parseFromString(this.responseText, "text/html");
							wkly_json = JSON.parse(wkly_doc.getElementById("wkly_data").innerHTML);	
							generateCurrWeatherCard();
						}
					}
					packet_load = "curr_location=" + JSON.stringify(ip_api_json);
					xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
					xhr.send(packet_load);		
				}
				
				// Get lat-lng for current lcoation 
				var xhttp = new XMLHttpRequest();
				xhttp.open("GET", "http://ip-api.com/json", true);
				xhttp.onreadystatechange = function() {
					if (this.readyState == 4 && this.status == 200) {
						ip_api_json = this.responseText;
						city = JSON.parse(ip_api_json).city;
						receiveJSON();
					}
				}
				xhttp.send();							
			}

			// REQUESTING DATA IF MANUAL DETAILS ENTERED IN FORM (AFTER VALIDATING FIELDS)			
			else {
				var street = document.getElementById("manual").Street.value;
				city = document.getElementById("manual").City.value;
				var state = document.getElementById("manual").State.value;
				if (street==""| city==""|state==""){
					document.getElementById("error_msg_container").removeAttribute("hidden");
					document.getElementById("error_msg").innerHTML = "Please check the input address.";
					return;	
				}		
				var xhr = new XMLHttpRequest();   // new HttpRequest instance 
				var url = "index.php";
				xhr.open("POST", url, true);
				xhr.onreadystatechange = function() {
					if (this.readyState == 4 && this.status == 200) {
						//console.log("Success");
						var parser = new DOMParser();
						var wkly_doc = parser.parseFromString(this.responseText, "text/html");
						var wkly_document = wkly_doc.getElementById("wkly_data").innerHTML;
						if (wkly_document==""){ // zero results from google geocoding api
							document.getElementById("error_msg_container").removeAttribute("hidden");
							document.getElementById("error_msg").innerHTML = "Please check the input address.";
							return;	
						}
						wkly_json = JSON.parse(wkly_document);	
						generateCurrWeatherCard();
					}
				}
				packet_load = "json_location=" + JSON.stringify({ "Street": street, "City": city, "State": state });
				xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				xhr.send(packet_load);											
			}
		}

		/*
		FUNCTION TO GENERATE CURRENT WEATHER CARD FROM CURRENTLY OBJECT OF WKLY_JSON DATA
		*/
		function generateCurrWeatherCard(){
			
			document.getElementById("curr_weather_card").innerHTML="";
			document.getElementById("wkly_weather_card").innerHTML="";
			var currently = wkly_json.currently;
            var ele = document.getElementById("curr_weather_card");
            ele.hidden = false;

            var _city = document.createElement("div");
            ele.appendChild(_city);
            _city.setAttribute("style","font-size:35px;font-weight:bold");
            _city.innerHTML += city + "<br>";

            var _timezone = document.createElement("div");
            ele.appendChild(_timezone);
            _timezone.setAttribute("style","font-size:14px");
            _timezone.innerHTML += wkly_json.timezone + "<br>";

            var _temperature = document.createElement("div");
            ele.appendChild(_temperature);
            _temperature.setAttribute("style","font-size:90px;font-weight:bold");
            _temperature.innerHTML += Math.round(currently.temperature) + "<img src = 'https://cdn3.iconfinder.com/data/icons/virtual-notebook/16/button_shape_oval-512.png' style='height:12px;width:12px;vertical-align:55%;margin-left:5px'> " +  "<span style='font-size:50px;margin-left:-20px'>F</span>"+"<br>";

            var _summary = document.createElement("div");
            ele.appendChild(_summary);
            _summary.setAttribute("style","font-size:35px;font-weight:bold; margin-bottom:5px");
            _summary.innerHTML += currently.summary + "<br>";

            // Table with weather icons and values
            var table = document.createElement("Table");
            ele.appendChild(table);
            table.setAttribute("style","margin-left:-15px");
            row1 = table.insertRow(0);
            row2 = table.insertRow(1);
            (row1.insertCell(0)).innerHTML = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-16-512.png' title='humidity'>";
            (row1.insertCell(1)).innerHTML = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-25-512.png' title='pressure'>";
            (row1.insertCell(2)).innerHTML = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-27-512.png' title='windSpeed'>";
            (row1.insertCell(3)).innerHTML = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-30-512.png' title='visibility'>";
            (row1.insertCell(4)).innerHTML = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-28-512.png' title='cloudCover'>";
            (row1.insertCell(5)).innerHTML = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-24-512.png' title='ozone'>";

            (row2.insertCell(0)).innerHTML = currently.humidity;
            (row2.insertCell(1)).innerHTML = currently.pressure;
            (row2.insertCell(2)).innerHTML = currently.windSpeed;
            (row2.insertCell(3)).innerHTML = currently.visibility;
            (row2.insertCell(4)).innerHTML = currently.cloudCover;
            (row2.insertCell(5)).innerHTML = currently.ozone;
         	generateWklyWeatherCard();   
		}

		/*
		Function to return HTML containing img tag corrresponding to the input weather
		in wkly_weather_card
		*/
		function getIcon(weather){
			var src_link;
			switch (weather) {
				case 'clear-day':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-12-512.png'>";
					break;
				case 'clear-night':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-12-512.png'>";
					break;
				case 'rain':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-04-512.png'>";
					break;
				case 'snow':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-19-512.png'>";
					break;
				case 'sleet':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-07-512.png'>";
					break;
				case 'wind':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-27-512.png'>";
					break;
				case 'fog':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-28-512.png'>";
					break;
				case 'cloudy':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-01-512.png'>";
					break;
				case 'partly-cloudy-day':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-02-512.png'>";
					break;
				case 'partly-cloudy-night':
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-02-512.png'>";
					break;
				default:
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-12-512.png'>";
			}
			return src_link;
		}
		
		/*
		FUNCTION TO GENERATE WEEKLY WEATHER CARD FROM DAILY OBJECT OF WKLY_JSON DATA
		*/
		function generateWklyWeatherCard(){
			var ele = document.getElementById("wkly_weather_card");
			var table = document.createElement("Table");
            ele.appendChild(table);
            table.setAttribute("id","wkly_table");
            for (var i=0;i<=wkly_json.daily.data.length;i++){
            	table.insertRow(i);
            }
            header = table.rows[0];
            (header.insertCell(0)).innerHTML = "Date";
            (header.insertCell(1)).innerHTML = "Status";
            (header.insertCell(2)).innerHTML = "Summary";
            (header.insertCell(3)).innerHTML = "TemperatureLow";
            (header.insertCell(4)).innerHTML = "TemperatureHigh";
            (header.insertCell(5)).innerHTML = "WindSpeed";
            for (var i=0;i<wkly_json.daily.data.length;i++){
            	var row = table.rows[i+1];
            	var timeVal = wkly_json.daily.data[i].time
        		var date = new Date((timeVal)*1000);
        		var day = date.getDate().toString();
        		var month = (date.getMonth()+1).toString() ;
        		var year = date.getFullYear().toString();
        		(row.insertCell(0)).innerHTML = year + "-" + month + "-" + day; 
            	(row.insertCell(1)).innerHTML = getIcon(wkly_json.daily.data[i].icon);
            	(row.insertCell(2)).innerHTML = wkly_json.daily.data[i].summary;
            	(row.cells[2]).setAttribute("style","cursor: pointer");
            	(row.cells[2]).setAttribute("id",timeVal.toString());
            	(row.cells[2]).setAttribute("onclick","requestDailyData(wkly_json.latitude, wkly_json.longitude, this)");
            	(row.insertCell(3)).innerHTML = Math.round(wkly_json.daily.data[i].temperatureLow);
            	(row.insertCell(4)).innerHTML = Math.round(wkly_json.daily.data[i].temperatureHigh);
            	(row.insertCell(5)).innerHTML = wkly_json.daily.data[i].windSpeed;	
            }
		}

		/*
		Function to submit post request containing lat/lng and time for which daily weather is requested. 
		Extract the DAILY weather data from page sent by server, and 
		SET RESULT IN DAILY_JSON 
		*/
		function requestDailyData(lat, lng, element){
			var xhr = new XMLHttpRequest();   // new HttpRequest instance 
			var url = "index.php";
			xhr.open("POST", url, true);
			xhr.onreadystatechange = function() {
				if (this.readyState == 4 && this.status == 200) {
					// console.log("Success");
					var parser = new DOMParser();
					var daily_doc = parser.parseFromString(this.responseText, "text/html");
					daily_json = JSON.parse(daily_doc.getElementById("daily_data").innerHTML);	
					// console.log(daily_json);
					document.getElementById("curr_weather_card").innerHTML="";
					document.getElementById("curr_weather_card").hidden = true;
					document.getElementById("wkly_weather_card").innerHTML="";
					generateDailyWeatherCard();
				}
			}
			packet_load = "daily_data_request=" + JSON.stringify({ "lat": lat, "lon": lng, "time": element.id});
			xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xhr.send(packet_load);
		}

		
		/*
		Function to return HTML containing img tag corrresponding to the input weather for daily condition
		*/
		function getDailyIcon(weather){
			var src_link;
			switch (weather) {
				case 'clear-day':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/sun-512.png'>";
					break;
				case 'clear-night':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/sun-512.png'>";
					break;
				case 'rain':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/rain-512.png'>";
					break;
				case 'snow':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/snow-512.png'>";
					break;
				case 'sleet':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/lightning-512.png'>";
					break;
				case 'wind':
					src_link = "<img src='https://cdn4.iconfinder.com/data/icons/the-weather-is-nice-today/64/weather_10-512.png'>";
					break;
				case 'fog':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/cloudy-512.png'>";
					break;
				case 'cloudy':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/cloud-512.png'>";
					break;
				case 'partly-cloudy-day':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/sunny-512.png'>";
					break;
				case 'partly-cloudy-night':
					src_link = "<img src='https://cdn3.iconfinder.com/data/icons/weather-344/142/sunny-512.png'>";
					break;
				default:
					src_link = "<img src='https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-12-512.png'>";
			}
			return src_link;
		}

		/*
		FUNCTION TO GENERATE DAILY WEATHER CARD FROM CURRENTLY OBJECT OF DAILY_JSON DATA
		*/
		function generateDailyWeatherCard(){
			document.getElementById("daily_weather_card").innerHTML="";
			document.getElementById("daily_weather_title").innerHTML = "Daily Weather Detail";

			var currently = daily_json.currently;
			var ele = document.getElementById("daily_weather_card");
			ele.hidden = false;

			var _summary = document.createElement("div");
            ele.appendChild(_summary);
            _summary.setAttribute("id","daily_summary_temp");
            _summary.setAttribute("style","font-size:30px;font-weight:bold; margin-bottom:5px");
            _summary.innerHTML += currently.summary + "<br><div style='font-size:100px;margin-top:10px'>" + Math.round(currently.temperature) + "<img src = 'https://cdn3.iconfinder.com/data/icons/virtual-notebook/16/button_shape_oval-512.png' style='height:10px;width:10px;vertical-align:60%'> " +  "<span style='font-size:75px;margin-left:-15px'>F</span>"+"</div>";

            var _icon = document.createElement("div");
            ele.appendChild(_icon);
            _icon.setAttribute("id","weather_icon");
            _icon.innerHTML = getDailyIcon(currently.icon);

            var table = document.createElement("table");
            ele.appendChild(table);
            table.setAttribute("id","daily_detail_table");

            // function to return description associated with a precipitation value 
			function getPrecipitation(value){
				if (value<=0.001) {
					return "None";
				}
				if (value<=0.015){
					return "Very Light";
				}
				if (value<=0.05){
					return "Light";
				}
				if (value<=0.1){
					return "Moderate";
				}
				return "Heavy";
			}
            table.insertRow(0);
            (table.rows[0].insertCell(0)).innerHTML = "Precipitation:";
            table.rows[0].cells[0].setAttribute("style","text-align:right;font-size:18px");
            (table.rows[0].insertCell(1)).innerHTML = getPrecipitation(currently.precipIntensity);
            table.rows[0].cells[1].setAttribute("style","text-align:left");

            table.insertRow(1);
            (table.rows[1].insertCell(0)).innerHTML = "Chance of Rain:";
            table.rows[1].cells[0].setAttribute("style","text-align:right;font-size:18px");
            (table.rows[1].insertCell(1)).innerHTML = Math.round(currently.precipProbability*100)+ " <span style='font-size:18px'>%</span>";
            table.rows[1].cells[1].setAttribute("style","text-align:left");
            table.insertRow(2);
            (table.rows[2].insertCell(0)).innerHTML = "Wind Speed:";
            table.rows[2].cells[0].setAttribute("style","text-align:right;font-size:18px");
            (table.rows[2].insertCell(1)).innerHTML = currently.windSpeed + " <span style='font-size:18px'>mph</span>";
            table.rows[2].cells[1].setAttribute("style","text-align:left");
            table.insertRow(3);
            (table.rows[3].insertCell(0)).innerHTML = "Humidity:";
            table.rows[3].cells[0].setAttribute("style","text-align:right;font-size:18px");
            (table.rows[3].insertCell(1)).innerHTML = Math.round(currently.humidity*100) + " <span style='font-size:18px'>%</span>";
            table.rows[3].cells[1].setAttribute("style","text-align:left");
            table.insertRow(4);
            (table.rows[4].insertCell(0)).innerHTML = "Visibility:";
            table.rows[4].cells[0].setAttribute("style","text-align:right;font-size:18px");
            (table.rows[4].insertCell(1)).innerHTML = currently.visibility + " <span style='font-size:18px'>mi</span>";
            table.rows[4].cells[1].setAttribute("style","text-align:left");
            table.insertRow(5);

            function getTime(unix_time, timezone){
            	var time_str = new Date(unix_time*1000).toLocaleTimeString("en-US", {timeZone: timezone});
            	var hr = time_str.split(":")[0] + " <span style='font-size:18px'>" + time_str.split(':')[2][3] + "M</span> ";
            	return hr;
            }
            try{
            	getTime(daily_json.daily.data[0].sunriseTime,daily_json.timezone);	
            	getTime(daily_json.daily.data[0].sunsetTime,daily_json.timezone);
            	(table.rows[5].insertCell(0)).innerHTML = "Sunrise/ Sunset:";
	            table.rows[5].cells[0].setAttribute("style","text-align:right;font-size:18px");
	            (table.rows[5].insertCell(1)).innerHTML = getTime(daily_json.daily.data[0].sunriseTime,daily_json.timezone) + " <span style='font-size:18px'>/</span> " + getTime(daily_json.daily.data[0].sunsetTime,daily_json.timezone);
	            table.rows[5].cells[1].setAttribute("style","text-align:left"); 
            }
            catch(err){
            	console.log("Unable to find sunrise or sunset time")
            }

            document.getElementById("hourly_weather_title").innerHTML = "Day's Hourly Weather";   
            document.getElementById("temp_chart_button").innerHTML = "<img src='https://cdn4.iconfinder.com/data/icons/geosm-e-commerce/18/point-down-512.png' onclick='generateTempChart()'>";     
		}

		/*
		FUNCTION TO GENERATE DAILY TEMPERATURE CHART FROM HOURLY OBJECT OF DAILY_JSON DATA
		*/	
		function generateTempChart(){

			document.getElementById("temp_chart_button").innerHTML = "<img src='https://cdn0.iconfinder.com/data/icons/navigation-set-arrows-part-one/32/ExpandLess-512.png' onclick='hideTempChart()'>";
			
			google.charts.load('current', {packages: ['corechart', 'line']});
			google.charts.setOnLoadCallback(drawBasic);

			function drawBasic() {

			      var data = new google.visualization.DataTable();
			      data.addColumn('number', 'X');
			      data.addColumn('number', 'T');
			      for (var i=0;i<daily_json.hourly.data.length;i++){
			      	data.addRows([[i,daily_json.hourly.data[i].temperature]]);
			      }

			      var options = {  colors:['#A7D0D9'], hAxis: {title: 'Time',  gridlines:'grey'}, vAxis: {title: 'Temperature', baseline:'black', textPosition: 'none',gridlines:'grey'}, width: 650,
        height: 200};
			      var chart = new google.visualization.LineChart(document.getElementById('temp_chart'));
			      chart.draw(data, options);
			}
		}

		/*
		Function to hide tenperature chart when clicking on expand less icon
		*/
		function hideTempChart(){
			document.getElementById("temp_chart_button").innerHTML = "<img src='https://cdn4.iconfinder.com/data/icons/geosm-e-commerce/18/point-down-512.png' onclick='generateTempChart()'>";
			document.getElementById('temp_chart').innerHTML = "";	
		}
		
	</script>

	<style>
		
		body{
			margin:0px 0px 0px 0px;
			background-color: white;
		}

		td {
			width:68px;
			text-align: center;
			font-weight:bold;
			border-collapse: collapse;
		}
		img{
			width:25px;
			height:25px;
		}
		
		input, select {
			font-family: serif;
			font-size:13px;
		}
		#site_centered {
			width: 950px;
			margin-left:auto;
			margin-right:auto;
			overflow: hidden;
		}

		#forms_centered {
			width: 800px;
			margin-top: 20px;
			margin-left: auto;
			margin-right: auto;
			background-color: #33AC38;
			color:white;
			border-radius: 25px;
		}

		#weather_search{
			height:50px;
			font-style: italic;
			font-family: serif;
			text-align: center; 
			font-weight: 100;
			font-size: 50px; 
			padding:5px	
		}
		#manual_form{
			width:410px;
			height:150px;
			font-weight: bold;
			font-family: serif;
			font-size: 20px; 
			float: left;
			padding-left:35px;
			margin-top: 10px;
		}
		#auto_form{
			width:300px;
			margin-top: 10px;
			height:150px;
			font-weight: bold;
			font-family: serif;
			font-size: 20px; 
			float:right;
			margin-right:25px;
		}
		#vertical_line {
			float:left;
			border-left: 5px solid white;
			height: 150px;
			border-radius: 1px;
			width: 0px;
		}
		#form_ctrl{
			clear: both;
			padding-top:10px;
			padding-bottom: 25px;	
		}
		#ctrl_button{
			padding-left: 275px;	
		}

		#error_msg_container{
			text-align: center; 	
		}
		#error_msg{
			border: 2px solid grey;
			margin:15px;
			background-color: #F0F0F0;
			font-family: serif;
			display: inline-block;
			font-size: 20px; 
			padding:2px;
			width:400px;
		}
		#curr_weather_card{
			width: 400px;
			margin: 25px auto;
			background-color: #5CC3F3;
			color:white;
			border-radius: 15px;
			padding: 15px;
		}

		#wkly_table{
			table-layout: auto;
			font-size:16px;
			width:900px;
			background-color: #A0C8EF;
			border:1px solid blue;
			margin-top: 20px;
			margin-left: auto;
			margin-right: auto;
			color:white;
			border-collapse: collapse;
		}
		#wkly_table img{
			width:40px;
			height:40px;
		}
		#wkly_table td{
			width:auto;
			border:1px solid blue;
			border-spacing:0px;
			border-collapse: collapse;
			padding:0px;
		}
		#daily_weather_title{
			clear: both;
			font-size:35px;
			color:black;
			font-weight:bold;
			margin-left:auto;
			margin-right:auto;
			margin-top:20px;
			margin-bottom:15px;
			text-align:center;	
		}
		#daily_weather_card{
			width: 500px;
			margin-left: auto;
			margin-right: auto; 
			background-color: #A7D0D9;
			color:white;
			border-radius: 15px;
			padding: 15px;	
		}
		#weather_icon img{
			float: right;
			width:245px;
			height:245px; 
			margin-top: -10px;
			margin-right: -5px;
		}
		#daily_summary_temp{
			width:225px;
			float:left;
			margin-top:60px;
			margin-left:10px;
			margin-right:10px;
		}
		#daily_detail_table{
			font-size:25px;	
			clear:both;
			margin-left:100px;
			border-spacing:0px;
			border-collapse: collapse;
			padding:0px;

		}
		#daily_detail_table td{
			width:200px;
		}
		#hourly_weather_title{
			clear: both;
			font-size:35px;
			color:black;
			font-weight:bold;
			margin-left:auto;
			margin-right:auto;
			margin-top:20px;
			margin-bottom:15px;
			text-align:center;	
		}
		#temp_chart_button{
			margin-left:auto;
			margin-right:auto;
			text-align:center;
		}
		#temp_chart{
			margin:0 auto;
			width:650px;
		}
		
	</style>

</head>


<body>

	<div id = "site_centered">	

		<div id = "forms_centered">

			<div id = "weather_search">Weather Search</div>
 
			<div id= "manual_form">

				<form id="manual">
					<span>Street &nbsp</span><input type="text" name="Street" value="" style = "margin:5px">  <br>
					<span>City &nbsp &nbsp</span> <input type="text" name="City" value="" style = "margin:5px"> <br>
					<span>State </span><select name="State" style = "margin:5px; width:230px">
						<option value="" Selected > State </option>
						<option value="AL">Alabama </option>
						<option value="AK"> Alaska </option>
						<option value="AZ"> Arizona</option>
						<option value="AR">Arkansas</option>
						<option value="CA"> California</option>
						<option value="CO"> Colorado</option>
						<option value="CT">Connecticut</option>
						<option value="DE">Delaware</option>
						<option value="DC">District Of Columbia</option>
						<option value="FL">Florida</option>
						<option value="GA">Georgia</option>
						<option value="HI">Hawaii</option>
						<option value="ID">Idaho</option>
						<option value="IL">Illinois</option>
						<option value="IN">Indiana</option>
						<option value="IA">Iowa</option>
						<option value="KS">Kansas</option>
						<option value="KY">Kentucky</option>
						<option value="LA">Louisiana</option>
						<option value="ME">Maine</option>
						<option value="MD">Maryland</option>
						<option value="MA">Massachusetts</option>
						<option value="MI">Michigan</option>
						<option value="MN">Minnesota</option>
						<option value="MS">Mississippi</option>
						<option value="MO">Missouri</option>
						<option value="MT">Montana</option>
						<option value="NE">Nebraska</option>
						<option value="NV">Nevada</option>
						<option value="NH">New Hampshire</option>
						<option value="NJ">New Jersey</option>
						<option value="NM">New Mexico</option>
						<option value="NY">New York</option>
						<option value="NC">North Carolina</option>
						<option value="ND">North Dakota</option>
						<option value="OH">Ohio></option>
						<option value="OK">Oklahoma</option>
						<option value="OR">Oregon</option>
						<option value="RI">Rhode Island</option>
						<option value="SC">South Carolina</option>
						<option value="SD">South Dakota</option>
						<option value="TN">Tennessee</option>
						<option value="TX">Texas</option>
						<option value="UT">Utah</option>
						<option value="VT">Vermont</option>
						<option value="VA">Virginia</option>
						<option value="WA">Washington</option>
						<option value="WV">West Virginia</option>
						<option value="WI">Wisconsin</option>
						<option value="WY">Wyoming</option>
					</select><br>					
				</form>	
			</div>

			<div id = "vertical_line" ></div>

			<div id = "auto_form">
				<form id="auto" style="padding-left: 100px">
					<input type="checkbox" name="curr_loc" value="yes" onchange = "changeDisabilityForm()">Current Location<br>
				</form>	
			</div>

			<div id = "form_ctrl">
				<form id = "ctrl_button">
					<input type="button" name = "search" onclick="validateAndSubmit()" style="font-family: serif; font-size: medium" value="search">
					<input type="button" name = "clear" onclick="clearSite()" style="font-family: serif;font-size: medium" value = "clear">				
				</form>
			</div>
		</div>

		<!-- Screen 1 (weekly view) -->
		<div id="error_msg_container" hidden = "true"><div id="error_msg"></div></div>
		<div id="curr_weather_card" hidden = "true"> </div>
		<div id="wkly_weather_card"></div>

		<!-- Screen 2 (daily view) -->
		<div id="daily_weather_title"></div>
		<div id="daily_weather_card" hidden="true"></div>

		<div id="hourly_weather_title"></div>
		<div id="temp_chart_button"></div>
		<div id="temp_chart"></div>
		
		<!-- Below code runs at the backend to process various xmlhttp requests, 
			call necessary apis and echo results in respective divs -->

		<div id="wkly_data">
			<?php
				if (isset($_POST['json_location'])){
					$lat_lng_arry = get_lat_lng();
					if(is_null($lat_lng_arry)){ // produces Zero_Results from Geocoding api
						echo "invalid";
					} else {
						$wkly_data = get_wkly_weather($lat_lng_arry);
						echo json_encode($wkly_data);	
					}	
				}
				else if(isset($_POST['curr_location'])){
					$curr_location = json_decode(json_decode($_POST['curr_location']));
					$lat_lng_arry = array($curr_location->lat, $curr_location->lon);
					if(is_null($lat_lng_arry[0])){ // produces Zero_Results from Geocoding api
						echo "";
					} else {
						$wkly_data = get_wkly_weather($lat_lng_arry);
						echo json_encode($wkly_data);	
					}						
				}
			?>
		</div>
		<div id="daily_data">
			<?php
				if (isset($_POST['daily_data_request'])){
					$daily_input = json_decode($_POST['daily_data_request']);
					$lat_lng_time= array($daily_input->lat, $daily_input->lon, $daily_input->time);
					$daily_data = get_daily_weather($lat_lng_time);
					echo json_encode($daily_data);
				}
			?>
		</div>
	</div>
	<div id="blank_space" style="margin:50px"></div>
	
</body>

</html>

<!-- 
$output = print_r($curr_location, true);
file_put_contents('test2.txt', $output); 
-->
