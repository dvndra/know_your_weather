import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  private url = "http://devendra.us-east-2.elasticbeanstalk.com/api/search/?state=";
  constructor(private http : Http) { }

  getData(query: string){
    return this.http.get(this.url+encodeURI(query));
  }
}


