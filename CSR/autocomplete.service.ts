import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
@Injectable({
  providedIn: 'root'
})
export class AutocompleteService {

  private url = "http://devendra.us-east-2.elasticbeanstalk.com/api/autocomplete/?input=";
  constructor(private http : Http) { }

  getData(query: string){
    return this.http.get(this.url+encodeURI(query));
  }
}
