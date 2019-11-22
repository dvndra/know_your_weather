import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'hw-first';
  private isSearch: boolean;
  private isClear: boolean;
  private isResults: boolean;
  private isFavorites:boolean;
  private formData:any;
  private changeToResults:boolean;

  passSearch(eventArgs:any){
    this.isSearch=true;
    this.isClear=false;
    this.isResults=true;
    this.isFavorites=false;
    this.formData=eventArgs;
    this.changeToResults=false;
    // console.log(eventArgs);
  }
  passClear(){
    this.isSearch=false;
    this.isClear=true;
    this.isResults=false;
    this.isFavorites=false
    this.formData=null;
    this.changeToResults=false;
  }
  passResults(){
    this.isSearch=true;
    this.isClear=false;
    this.isResults=true;
    this.isFavorites=false;
    this.changeToResults=false;
  }
  passFavorites(){
    this.isSearch=false;
    this.isClear=false;
    this.isResults=false;
    this.isFavorites=true;
    this.changeToResults=false;
    // console.log("inside app",this.isResults);
    // console.log("app module has fav:",this.isFavorites)
  }
  changeTabResults(){
    this.changeToResults=true;
    this.isFavorites=false;
  }
}
