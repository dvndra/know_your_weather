import { IpapiService } from './../ipapi.service';
import { CurrentService } from './../current.service';
import { DailyService } from './../daily.service';
import { SearchService } from './../search.service';
import { GeocodingService } from './../geocoding.service';
import { Component, OnChanges, OnInit, Input, Output, ViewChild, EventEmitter, ElementRef} from '@angular/core';
import * as CanvasJS from './canvasjs.min';
import {NgbModal, ModalDismissReasons} from '@ng-bootstrap/ng-bootstrap';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'tab-results',
  templateUrl: './tab-results.component.html',
  styleUrls: ['./tab-results.component.css']
})
export class TabResultsComponent implements OnChanges, OnInit {

  @Input('isSearch')isSearch:boolean;
  @Input('isClear')isClear:boolean;
  @Input('isResults')isResults:boolean;
  @Input('isFavorites')isFavorites:boolean;
  @Input('formData')formData:any;
  @Output('changeToResults') changeToResults = new EventEmitter();

  public query:string;
  public seal:string;
  public state:string;
  public city:string;
  public twitterUrl:string;

  public isCurrent=false;
  public isHourly=false;
  public isWeekly=false;

  public currData:any;
  public dailyData:any;
  public dataSource:any;
  
  public selectedLabel="";
  public barChartOptions:any;
  public barChartData: any;
  public barChartLabels = ['0','1','2','3','4','5','6','7','8','9','10',
                          '11','12','13','14','15','16','17','18','19','20','21','22','23'];

  @ViewChild('clickModal',{ static: true }) clickModal: ElementRef<HTMLElement>;
  public closeResult: string;
  public dailyDate:string;
  public dailyIcon:string;
  public invalidAddress:boolean;

  public markedFav = false;
  public favoriteList:any;
  public displayedColumns=['#','Image','City','State','Favorites'];
  
  constructor(private geoService: GeocodingService, private searchService: SearchService, private ipService: IpapiService,
    private dailyService: DailyService, private currService: CurrentService,private modalService: NgbModal) { 
      this.city="";
      this.seal = "";
  }
  ngOnInit(){
      this.loadFavorites();
  }

  ngOnChanges() {
    // console.log("res:",this.isResults);
    // console.log("fav:",this.isFavorites);
    if (this.isFavorites){
      this.isCurrent=false;
      this.isHourly=false;
      this.isWeekly=false;
      this.isResults=false;
      this.invalidAddress=false;
    }
    else if(this.isClear){
      this.isResults=false;
      this.isCurrent=false;
      this.isHourly=false;
      this.isWeekly=false;
      this.invalidAddress=false;
    }
    else if (this.isResults){
      this.markedFav=false;
      this.getCurrData();
      this.isHourly=false;
      this.isWeekly=false;
      this.invalidAddress=false;
    } 
    // console.log("res:",this.isResults);
    // console.log("fav:",this.isFavorites);
  }
  showCurrent(){
    this.isCurrent=true;
    this.isHourly=false;
    this.isWeekly=false;
    this.selectedLabel="";
  }
  showHourly(){
    this.plotHourlyChart();
    this.isCurrent=false;
    this.isHourly=true;
    this.isWeekly=false;
    this.selectedLabel="";
  }
  showWeekly(){
    this.isCurrent=false;
    this.isHourly=false;
    this.isWeekly=true;
    this.selectedLabel="";
    setTimeout(()=>this.plotWeeklyChart(),100);
  }

  /*
  --------------- DEALING WITH FAVORITES -------------------------------
  */
  changeFav(){
    this.markedFav=!this.markedFav;
    var localObjects:any;
    if(this.markedFav){

      if(localStorage.getItem("markedFavorites")=== null){
        localObjects={};
      }
      else{
        localObjects=JSON.parse(localStorage.getItem("markedFavorites"));
      }

      if(!(this.city in localObjects)){
        // store in local storage
        let data:any;
        data={};
        data.image=this.seal;
        data.city=this.city;
        data.state=this.state;
        localObjects[this.city]=data;
        localStorage.setItem("markedFavorites",JSON.stringify(localObjects));
      }  
    }

    else{
      localObjects=JSON.parse(localStorage.getItem("markedFavorites"));
      delete localObjects[this.city];
      localStorage.setItem("markedFavorites",JSON.stringify(localObjects));
    }

    // Compute favoriteList
    localObjects=JSON.parse(localStorage.getItem("markedFavorites"));
    this.favoriteList=new Array(Object.keys(localObjects).length);
    let index=0;
    for (let key of Object.keys(localObjects)) {
      let content = localObjects[key];
      this.favoriteList[index] = {"position":index+1,"image":content.image,"city":content.city,"state":content.state};
      index++;
    }
    this.dataSource = new MatTableDataSource(this.favoriteList);
    // console.log("favList:",this.favoriteList);
  }
 
  loadFavorites(){
    if(localStorage.getItem("markedFavorites")=== null){
      this.favoriteList=new Array();
      this.dataSource = new MatTableDataSource(this.favoriteList);
      return;
    } 
    let localObjects=JSON.parse(localStorage.getItem("markedFavorites"));
    this.favoriteList=new Array(Object.keys(localObjects).length);
    let index=0;
    for (let key of Object.keys(localObjects)) {
      let content = localObjects[key];
      this.favoriteList[index] = {"position":index+1,"image":content.image,"city":content.city,"state":content.state};
      index++;
    }
    this.dataSource = new MatTableDataSource(this.favoriteList);
  }

  deleteFavorite(i:any){
    // console.log(i);
    this.favoriteList.splice(i,1);
    // console.log(this.favoriteList);
    this.dataSource = new MatTableDataSource(this.favoriteList);
    // update local storage
    let localObjects={};
    for (let i=0;i<this.favoriteList.length;i++) {
      let content = this.favoriteList[i];
      localObjects[content.city] = {"image":content.image,"city":content.city,"state":content.state};
    }
    localStorage.setItem("markedFavorites",JSON.stringify(localObjects));
  }
  test(row:any){
    this.city = row.city;
    this.state = row.state;
    this.isFavorites=false;
    this.isResults=true;
    this.markedFav=true;
    this.formData={manualForm:{"street":"a","city":this.city,"state":this.state}};
    this.changeToResults.emit();
    this.getCurrData();
  }
  /*
  --------DONE WITH FAVORITE HANDLING ---------------------------------
  */
  plotHourlyChart(){
    let value=this.selectedLabel;
    if (value==""){
      value="temperature";
    }
    // console.log("value:",value,typeof(value));
    let labelMap = {"temperature":"Fahrenheit","pressure":"Millibars","humidity":"%",
                      "ozone":"Dobson Units","visibility":"Miles","windSpeed":"Miles per hour"};
    this.barChartOptions = { scaleShowVerticalLines: false, 
      responsive: true, legend: {onClick: (e:any) => e.stopPropagation()},
      scales: {
        xAxes: [{ scaleLabel: { display: true, labelString: "Time difference from current hour"}}],
        yAxes: [{ ticks:{display: true, autoSkip: true, maxTicksLimit: 7}, 
          scaleLabel: { display: true, labelString: labelMap[value]}}]
      }
    };
    let dataLabel=new Array(Math.min(24,this.currData.hourly.data.length));
    for(let i=0;i<dataLabel.length;i++){
      dataLabel[i]=this.currData.hourly.data[i][value]+0.01;
      dataLabel[i] = Math.round(dataLabel[i] * 100) / 100;
    }
    this.barChartData = [
      {data: dataLabel, label: value, backgroundColor: "rgba(183,222,244,1)"},
    ];
  }

  getDate(timeVal:number){
    var date = new Date((timeVal)*1000);
    var day = date.getDate().toString();
    var month = (date.getMonth()+1).toString() ;
    var year = date.getFullYear().toString();
    return day+"/"+month+"/"+year;
  } 

  plotWeeklyChart(){

    let wklyData=new Array(Math.min(8,this.currData.daily.data.length));
    for(let i=0;i<wklyData.length;i++){
      let data:any;
      data={};
      data.x=i;
      let obj= this.currData.daily.data[wklyData.length-i-1];
      data.y=[Math.round(obj.temperatureLow),Math.round(obj.temperatureHigh)];
      data.label=this.getDate(obj.time);
      data.color="#B7DEF4";
      wklyData[i]=data;
    }
    
    var chart = new CanvasJS.Chart("chartContainer", {
      animationEnabled: true,
      title: { text: "Weekly Weather"},
      legend: { verticalAlign: "top"},
      axisX: { title: "Days", gridThickness: 0 },
      axisY: { includeZero: false, title: "Temperature in Fahreheit",
        gridThickness: 0, interval:10 }, 
      data: [{
        type: "rangeBar",
        showInLegend: true,
        indexLabel: "{y[#index]}",
        legendText: "Day wise temperature range ",
        toolTipContent: "<b>{label}</b>: {y[0]} to {y[1]}",       
        dataPoints: wklyData,
        click: (e:any)=>{
          let index=e.dataPointIndex;
          let totLength=Math.min(8,this.currData.daily.data.length);
          let timestamp = this.currData.daily.data[totLength-index-1].time;
          this.fetchDailyWthr(timestamp);
        }
      }]
    });
    chart.render();   
  }

  // Modal show (Daily Weather)
  triggerFalseClick() {
    let el: HTMLElement = this.clickModal.nativeElement;
    el.click();
  }
  open(content:any) {
    this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }
  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return  `with: ${reason}`;
    }
  }

  getCurrData(){
    if (this.formData.currLocation){
      // do ip-api call
      this.ipService.getData().
      subscribe((response)=>{     
        let resp=response.json();
        this.state=resp.region;
        this.city=resp.city;
        let lat = resp.lat;
        let long = resp.lon;
        this.query = "latitude=" + lat +"&longitude=" + long;
        this.fetchCurrWthr();
      });      
    }
    else{
      // do geocoding call
      let street = this.formData.manualForm.street;
      this.city = this.formData.manualForm.city;
      this.state = this.formData.manualForm.state;
      this.query = "Street=" + street + "&City=" + this.city + "&State=" + this.state;
      this.geoService.getData(this.query).
      subscribe((response)=>{     
        let resp=response.json();
        if (resp.status!="OK"){
          this.invalidAddress=true;
          return;
        }
        // console.log(resp);
        let lat = resp.results[0].geometry.location.lat;
        let long = resp.results[0].geometry.location.lng;
        this.query = "latitude=" + lat +"&longitude=" + long;
        this.fetchCurrWthr();
      });   
    }
  }

  fetchCurrWthr(){
    //marked favorite logic
    let localObjects=JSON.parse(localStorage.getItem("markedFavorites"));
    if (localObjects!=null&&this.city in localObjects){
      this.markedFav=true;
    }

    this.currService.getData(this.query).
      subscribe((response)=>{     
        this.currData=response.json();
        // console.log(this.currData);
        this.getSeal();
        this.isCurrent=true;
        this.twitterUrl= "The current temperature at " + this.city + " is " + Math.round(this.currData.currently.temperature) 
                          + ". The weather conditions are " + this.currData.currently.summary 
                          + " #CSCI571WeatherSearch.";
        this.twitterUrl="https://twitter.com/intent/tweet?text=" + encodeURIComponent(this.twitterUrl);
        // console.log(this.twitterUrl);
      });
  }

  getSeal(){
    this.searchService.getData(this.state).
      subscribe((response)=>{     
        let resp=response.json();
        this.seal=resp.items[0].link;
        // console.log(this.seal);
      });
  }

  //include def. of this.day
  fetchDailyWthr(timestamp: number){ 
    this.dailyService.getData(this.query+"&time="+timestamp).
      subscribe((response)=>{     
        this.dailyData=response.json();
        this.dailyDate = this.getDate(this.dailyData.currently.time);
        this.dailyIcon=this.getDailyIcon(this.dailyData.currently.icon);
        this.triggerFalseClick();
      });
  }
  getDailyIcon(weather:string){
    let src_link:string;
    switch (weather) {
      case 'clear-day':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/sun-512.png";
        break;
      case 'clear-night':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/sun-512.png";
        break;
      case 'rain':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/rain-512.png";
        break;
      case 'snow':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/snow-512.png";
        break;
      case 'sleet':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/lightning-512.png";
        break;
      case 'wind':
        src_link = "https://cdn4.iconfinder.com/data/icons/the-weather-is-nice-today/64/weather_10-512.png";
        break;
      case 'fog':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/cloudy-512.png";
        break;
      case 'cloudy':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/cloud-512.png";
        break;
      case 'partly-cloudy-day':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/sunny-512.png";
        break;
      case 'partly-cloudy-night':
        src_link = "https://cdn3.iconfinder.com/data/icons/weather-344/142/sunny-512.png";
        break;
      default:
        src_link = "https://cdn2.iconfinder.com/data/icons/weather-74/24/weather-12-512.png";
    }
    return src_link;
  }
}
