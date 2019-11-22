import { AutocompleteService } from './../autocomplete.service';
import { Component, ViewChild, ElementRef, AfterViewInit, OnChanges, Output, Input, EventEmitter} from '@angular/core';

@Component({
  selector: 'search-form',
  templateUrl: './search-form.component.html',
  styleUrls: ['./search-form.component.css']
})
export class SearchFormComponent implements AfterViewInit, OnChanges{

  @Output('showRes') showRes = new EventEmitter();
  @Output('showFav') showFav = new EventEmitter();
  @Output('doSearch') doSearch = new EventEmitter();
  @Output('doClear') doClear = new EventEmitter();
  @Input('changeToResults')changeToResults:boolean;
  @ViewChild('cityString',{ static: true }) nameInputRef: ElementRef; 
  // @ViewChild('inputForm',{ static: true }) formInputRef: ElementRef;
  private suggestions: string[];
  public isResult = true;
  public progress=0;

  constructor(private service: AutocompleteService){ }

  ngAfterViewInit() {
    // console.log(this.nameInputRef.nativeElement.value);
  }
  ngOnChanges(){
    if(this.changeToResults){
      this.isResult=true;
    }
  }
  getSuggestions() {
    let query = this.nameInputRef.nativeElement.value;
    this.service.getData(query).
    subscribe((response)=>{     
      let resp=response.json();
      this.suggestions=new Array(Math.min(5,resp.predictions.length));     
      for(let i =0;i<this.suggestions.length;i++){
        this.suggestions[i]=resp.predictions[i].structured_formatting.main_text;
      }
    });
  }
  
  clickResults(){
    this.isResult=true;
    this.showRes.emit();
  }
  clickFavorites(){
    this.isResult = false; 
    this.showFav.emit();
    // console.log("Fav emitted");
  }
  clickSearch(searchForm:any){
    this.isResult=true;
    this.doSearch.emit(searchForm.value);
    this.progress=50;
    setTimeout(()=>{this.progress=90;},1500);
    setTimeout(()=>{this.progress=0;},500);
  }
  clickClear(){
    this.isResult=true;
    this.suggestions=null;
    this.doClear.emit();
  }
}
