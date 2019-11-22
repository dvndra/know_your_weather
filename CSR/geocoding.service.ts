import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

@Injectable({
  providedIn: 'root'
})
export class GeocodingService {

  private url = "http://devendra.us-east-2.elasticbeanstalk.com/api/geocoding/?";
  constructor(private http : Http) { }

  // sample query: Street=428%20Contessa&City=Irvine&State=CA
  getData(query: string){
    return this.http.get(this.url+encodeURI(query));
  }
}

